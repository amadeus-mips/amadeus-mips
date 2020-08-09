package cpu.pipelinedCache.dataCache.query

import axi.AXIIO
import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.AXIPorts.{AXIReadPort, AXIWritePort}
import cpu.pipelinedCache.components.metaBanks.TagValidBundle
import cpu.pipelinedCache.components.{MSHR, MaskedRefillBuffer, MissComparator, WriteQueue}
import cpu.pipelinedCache.dataCache.{DCacheCommitBundle, DCacheFetchQueryBundle}
import shared.Constants.DATA_ID
import shared.LRU.{PLRUMRUNM, TrueLRUNM}

//TODO: Don't accept query when writeBack back
class QueryTop(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val fetchQuery = Input(new DCacheFetchQueryBundle)
    val writeBack = Valid(new Bundle {
      val addr = new Bundle {
        val index  = UInt(cacheConfig.indexLen.W)
        val waySel = UInt(log2Ceil(cacheConfig.numOfWays).W)
      }
      val tagValid = new TagValidBundle
      val data     = Vec(cacheConfig.numOfBanks, UInt(32.W))
    })
    val axi = AXIIO.master()

    /** dirty data is connected to [[cpu.pipelinedCache.dataCache.DataBanks]]
      * and is queried when writeBack queue is not full */
    val dirtyData = Input(Vec(cacheConfig.numOfBanks, UInt(32.W)))

    /** query commit will *only* carry data for the query */
    val queryCommit = Output(new DCacheCommitBundle())

    /** hit reports to pipeline whether next cycle's returned data is valid */
    val hit = Output(Bool())

    /** ready reflects whether last stage's data need to be re-fetched */
    val ready = Output(Bool())

    val readyForInvalidate = Output(Bool())

    /** invalidate all lines at index */
    val invalidateAllValid = Output(Bool())

    val dirtyWay = Output(UInt(log2Ceil(cacheConfig.numOfWays).W))
  })

  //-----------------------------------------------------------------------------
  //------------------declare all the modules------------------------------------
  //-----------------------------------------------------------------------------
  val comparator   = Module(new MissComparator)
  val mshr         = Module(new MSHR)
  val refillBuffer = Module(new MaskedRefillBuffer)
  val axiRead      = Module(new AXIReadPort(addrReqWidth = 32, AXIID = DATA_ID))
  val axiWrite     = Module(new AXIWritePort(AXIID = DATA_ID))
  val lru =
    if (cacheConfig.numOfWays > 2) PLRUMRUNM(numOfSets = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)
    else TrueLRUNM(numOfSets                           = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)
  val writeQueue = Module(new WriteQueue)

  /** keep all the dirty information, dirty(way)(index) */
  val dirtyBanks = RegInit(
    VecInit(Seq.fill(cacheConfig.numOfWays)(VecInit(Seq.fill(cacheConfig.numOfSets)(CPUConfig.verification.B))))
  )

  val invalidateCounter = RegInit(0.U(log2Ceil((cacheConfig.numOfWays) + 1).W))

  val dirtyForInvalidateWire = (0 until cacheConfig.numOfWays)
    .map(dirtyBanks(_)(io.fetchQuery.index))

  /** do nothing to this query, proceed to next */
  val passThrough = WireDefault(!io.fetchQuery.valid)

  /** is the query a hit in the bank
    * This is to simply test if there is a hit
    * it is irrelevant to whether io_hit would be high*/
  val hitInBank = Wire(Bool())

  /** is the query hit in the refill buffer
    * this requires that the data has not been in bank yet */
  val hitInRefillBuffer = Wire(Bool())

  val hitInWriteQueue = writeQueue.io.resp.valid

  /** a successful handshake with the write queue */
  val dispatchToWriteQSucessful = writeQueue.io.enqueue.ready

  /** is a new miss generated, but is not guarateed to be accepted */
  val newMiss = Wire(Bool())

  /** the query ( valid or not ) has a hit, and may pass through */
  val queryHit = hitInBank || hitInRefillBuffer || hitInWriteQueue

  /** lru way records the way to evict, as eviction could last serveral cycles if
    * the write queue if full */
  val lruWayReg = Reg(UInt(log2Ceil(cacheConfig.numOfWays).W))

  /** if the way to evict at current cycle is dirty */
  val evictWayDirty = WireDefault(dirtyBanks(lruWayReg)(mshr.io.extractMiss.addr.index))

  /** corner case: write back is also in the idle stage */
  val qIdle :: qRefill :: qEvict :: qWriteBack :: qInvalidating :: qWaitToDrain :: Nil = Enum(6)
  val qState                                                         = RegInit(qIdle)
  /** this is for symbiyosys tool, otherwise it will assume it could be any value during cycle of reset */
  val qStateNext = RegInit(qIdle)
  qStateNext := qState
  switch(qState) {
    is(qIdle) {
      when(newMiss) {
        qState := qRefill
      }.elsewhen(io.readyForInvalidate && io.fetchQuery.invalidate) {
        invalidateCounter := (cacheConfig.numOfWays - 1).U
        qState := qInvalidating
      }
    }
    is(qRefill) {
      when(axiRead.io.finishTransfer) {

        qState := Mux(evictWayDirty, qEvict, qWriteBack)
      }.otherwise {
        lruWayReg := lru.getLRU(mshr.io.extractMiss.addr.index)
      }
    }
    is(qEvict) {
      when(dispatchToWriteQSucessful) {
        qState := qWriteBack
      }
    }
    is(qWriteBack) {
      //TODO: make this faster
      qState := qIdle
    }
    //VERI: invalidating: invalidate counter === 0 or next cycle is wait for drain
    is(qInvalidating) {
      when(invalidateCounter === 0.U) {
        qState := qWaitToDrain
      }
    }
    // wait to drain
    is(qWaitToDrain) {
      when(writeQueue.io.size === 0.U) {
        qState := qIdle
      }
    }
  }

  /** the cache is in the idle state */
  val isIdle = qState === qIdle

  /** the cache is in refill */
  val isRefill = qState === qRefill

  /** the cache is evicting data from banks to writeBack queue */
  val isEvict = qState === qEvict

  /** the cache is writing back */
  val isWriteBack = qState === qWriteBack

  /** when there is no structural hazard for data banks and tag/valid banks */
  val resourceFree = isIdle || (isRefill && !axiRead.io.finishTransfer)

  when(qState === qInvalidating) {
    invalidateCounter := invalidateCounter - 1.U
  }


  /** new miss can only be generated when the state is idle and query is not hit
    * and query is valid
    * and the write miss is not a hit in the line being written */
  newMiss := !queryHit && !passThrough && isIdle && !writeQueue.io.holdOffNewMiss

  /** can only hit in the refill buffer during idle, refill, evict and writeBack back */
  hitInRefillBuffer :=
    comparator.io.addrHitInRefillBuffer && refillBuffer.io.queryResult.valid && qState =/= qIdle

  /** can only hit in bank when it's not reading( q evict ) and not writing (q writeBack back)  */
  hitInBank := comparator.io.bankHitWay.valid

  axiRead.io.axi  <> DontCare
  axiWrite.io.axi <> DontCare
  io.axi.ar       <> axiRead.io.axi.ar
  io.axi.r        <> axiRead.io.axi.r

  io.axi.aw <> axiWrite.io.axi.aw
  io.axi.w  <> axiWrite.io.axi.w
  io.axi.b  <> axiWrite.io.axi.b

  /** index selection also holds true for icache invalidate */
  io.queryCommit.indexSel := Mux(
    (isRefill && axiRead.io.finishTransfer) || isEvict,
    mshr.io.extractMiss.addr.index,
    io.fetchQuery.index
  )
  io.queryCommit.waySel        := comparator.io.bankHitWay.bits
  io.queryCommit.bankIndexSel  := io.fetchQuery.bankIndex
  io.queryCommit.writeData     := io.fetchQuery.writeData
  io.queryCommit.writeMask     := io.fetchQuery.writeMask
  io.queryCommit.writeEnable   := hitInBank && io.fetchQuery.writeMask =/= 0.U && resourceFree && !passThrough
  //FIXME
  when(io.queryCommit.writeEnable) {
    dirtyBanks(comparator.io.bankHitWay.bits)(io.queryCommit.indexSel) := true.B
  }
  io.queryCommit.readData      := Mux(writeQueue.io.resp.valid, writeQueue.io.resp.bits, refillBuffer.io.queryResult.bits)
  io.queryCommit.readDataValid := hitInRefillBuffer || hitInWriteQueue

  io.writeBack.valid               := isWriteBack
  io.writeBack.bits.addr.index     := mshr.io.extractMiss.addr.index
  io.writeBack.bits.addr.waySel    := lruWayReg
  io.writeBack.bits.tagValid.tag   := mshr.io.extractMiss.addr.tag
  io.writeBack.bits.tagValid.valid := true.B
  io.writeBack.bits.data           := refillBuffer.io.allData

  io.hit   := (((hitInBank && resourceFree) || hitInRefillBuffer || hitInWriteQueue) && !passThrough && io.fetchQuery.writeMask === 0.U)
  io.ready := (passThrough || (hitInBank && resourceFree) || hitInRefillBuffer || hitInWriteQueue) && qState =/= qInvalidating && qState =/= qWaitToDrain

  io.dirtyWay := Mux(qStateNext === qInvalidating, RegNext(invalidateCounter),lruWayReg)

  /** when there is nothing in the write queue and state is idle and there is no pending request */
  io.readyForInvalidate := RegNext(writeQueue.io.size === 0.U && qState === qIdle && !io.fetchQuery.valid)

  // invalidate all ways at the last cycle of putting everything into queue
  io.invalidateAllValid := qState === qInvalidating && invalidateCounter === 0.U

  comparator.io.tagValid := io.fetchQuery.tagValid
  comparator.io.phyTag   := io.fetchQuery.phyTag
  comparator.io.index    := io.fetchQuery.index
  comparator.io.mshr     := mshr.io.extractMiss.addr

  /** request valid doesn't mean if the query is valid
    * it is only asserted when there is a new miss */
  refillBuffer.io.request.valid          := newMiss
  refillBuffer.io.request.bits.bankIndex := io.fetchQuery.bankIndex
  refillBuffer.io.request.bits.writeData := io.fetchQuery.writeData
  refillBuffer.io.request.bits.writeMask := Mux(comparator.io.addrHitInRefillBuffer && io.fetchQuery.valid, io.fetchQuery.writeMask, 0.U)
  refillBuffer.io.inputData              := axiRead.io.transferData
  refillBuffer.io.finish                 := axiRead.io.finishTransfer

  axiRead.io.addrReq.bits := Mux(
    newMiss,
    Cat(
      io.fetchQuery.phyTag,
      io.fetchQuery.index,
      io.fetchQuery.bankIndex,
      0.U(cacheConfig.bankOffsetLen.W)
    ),
    Cat(mshr.io.extractMiss.addr.asUInt, 0.U(cacheConfig.bankOffsetLen.W))
  )
  axiRead.io.addrReq.valid := newMiss

  writeQueue.io.enqueue.valid           := ((qState === qEvict) && dirtyBanks(lruWayReg)(mshr.io.extractMiss.addr.index))
  writeQueue.io.enqueue.bits.addr.tag   := mshr.io.extractMiss.tagValidAtIndex(lruWayReg).tag
  writeQueue.io.enqueue.bits.addr.index := mshr.io.extractMiss.addr.index
  writeQueue.io.enqueue.bits.data       := io.dirtyData
  //FIXME: discuss if this is correct
  // starting from the second cycle of invalidating, ending in the first cycle during waiting to drain
  when(qStateNext === qInvalidating ) {
    // when entry is dirty and valid
    writeQueue.io.enqueue.valid := RegNext(dirtyBanks(invalidateCounter)(io.fetchQuery.index) && io.fetchQuery.tagValid(invalidateCounter).valid, false.B)
    writeQueue.io.enqueue.bits.addr.tag := RegNext(io.fetchQuery.tagValid(invalidateCounter).tag)
    writeQueue.io.enqueue.bits.addr.index := io.fetchQuery.index
    writeQueue.io.enqueue.bits.data := io.dirtyData
  }
  writeQueue.io.query.addr := Cat(io.fetchQuery.phyTag, io.fetchQuery.index, io.fetchQuery.bankIndex)
    .asTypeOf(writeQueue.io.query.addr)
  // if the query is valid, then the query could issue to writeBack queue any cycle
  writeQueue.io.query.writeMask := Mux(!io.fetchQuery.valid, 0.U(4.W), io.fetchQuery.writeMask)
  writeQueue.io.query.data      := io.fetchQuery.writeData

  writeQueue.io.writeHandshake := axiWrite.io.writeHandshake
  writeQueue.io.writeCommit := axiWrite.io.writeCommit

  axiWrite.io.addrRequest <> writeQueue.io.dequeueAddr
  axiWrite.io.data        <> writeQueue.io.dequeueData
  axiWrite.io.dataLast    := writeQueue.io.dequeueLast
  // axi writeBack .io .axi has been moved to former place

  //TODO: make it into a FIFO
  mshr.io.recordMiss.valid                := newMiss
  mshr.io.recordMiss.bits.addr.tag        := io.fetchQuery.phyTag
  mshr.io.recordMiss.bits.addr.index      := io.fetchQuery.index
  mshr.io.recordMiss.bits.addr.bankIndex  := io.fetchQuery.bankIndex
  mshr.io.recordMiss.bits.tagValidAtIndex := io.fetchQuery.tagValid

  when(writeQueue.io.enqueue.fire && qState =/= qInvalidating && qState =/= qWaitToDrain) {
    dirtyBanks(lruWayReg)(mshr.io.extractMiss.addr.index) := false.B
  }
  when(qState === qWriteBack) {
    dirtyBanks(lruWayReg)(mshr.io.extractMiss.addr.index) := refillBuffer.io.dataDirty
  }
  when(writeQueue.io.enqueue.fire && qStateNext === qInvalidating) {
    dirtyBanks(RegNext(invalidateCounter))(io.fetchQuery.index) := false.B
  }
  // update the LRU when there is a hit in the banks, don't update otherwise
  when(hitInBank) {
    lru.update(index = io.fetchQuery.index, way = comparator.io.bankHitWay.bits)
  }

  //VERI:
  assert(!(io.fetchQuery.valid && io.fetchQuery.invalidate))
}
