package cpu.pipelinedCache.dataCache.query

import axi.AXIIO
import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.AXIPorts.{AXIReadPort, AXIWritePort}
import cpu.pipelinedCache.components.{MSHR, MaskedRefillBuffer, MissComparator, WriteQueue}
import cpu.pipelinedCache.dataCache.{DCacheCommitBundle, DCacheFetchQueryBundle}
import cpu.pipelinedCache.instCache.fetch.WriteTagValidBundle
import shared.Constants.DATA_ID
import shared.LRU.{PLRUMRUNM, TrueLRUNM}

//TODO: Don't accept query when write back
class QueryTop(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    val fetchQuery = Input(new DCacheFetchQueryBundle)
    val write      = Valid(new WriteTagValidBundle)
    val axi        = AXIIO.master()

    /** dirty data is connected to [[cpu.pipelinedCache.dataCache.DataBanks]]
      * and is queried when write queue is not full */
    val dirtyData = Input(Vec(cacheConfig.numOfBanks, UInt(32.W)))

    /** data path for a read hit */
    val queryCommit = Output(new DCacheCommitBundle())

    /** hit reports to pipeline whether next cycle's returned data is valid */
    val hit = Output(Bool())

    /** ready reflects whether last stage's data need to be re-fetched */
    val ready = Output(Bool())

    val dirtyWay = Output(UInt(log2Ceil(cacheConfig.numOfWays).W))
  })

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
  val dirtyBanks = RegInit(VecInit(Seq.fill(cacheConfig.numOfWays)(VecInit(Seq.fill(cacheConfig.numOfSets)(false.B)))))

  /** do nothing to this query, proceed to next */
  val passThrough = WireDefault(!io.fetchQuery.valid)

  /** is the query a hit in the bank */
  val hitInBank = WireDefault(comparator.io.bankHitWay.valid)

  /** is the query hit in the refill buffer */
  val hitInRefillBuffer = WireDefault(comparator.io.addrHitInRefillBuffer && refillBuffer.io.queryResult.valid)

  val hitInWriteQueue = WireDefault(writeQueue.io.resp.valid)

  /** a successful handshake with the write queue */
  val dispatchToWriteQSucessful = WireDefault(writeQueue.io.enqueue.ready)

  /** is a new miss generated, but is not guarateed to be accepted */
  val newMiss = Wire(Bool())

  /** is the query a hit in either places */
  val queryHit = WireDefault(hitInBank || hitInRefillBuffer || hitInWriteQueue)

  /** is the data.valid output high? */
  val validData = WireDefault(queryHit && !passThrough)

  /** lru way records the way to evict, as eviction could last serveral cycles if
    * the write queue if full */
  val lruWayReg = Reg(UInt(log2Ceil(cacheConfig.numOfWays).W))

  /** if the way to evict at current cycle is dirty */
  val evictWayDirty = WireDefault(dirtyBanks(lruWayReg)(mshr.io.extractMiss.addr.index))

  /** corner case: write back is also in the idle stage */
  val qIdle :: qRefill :: qEvict :: qWriteBack :: Nil = Enum(4)
  val qState                                          = RegInit(qIdle)
  switch(qState) {
    is(qIdle) {
      when(newMiss) {
        qState := qRefill
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
  }

  newMiss := !queryHit && !passThrough && qState === qIdle

  val isQueryMissAddress = WireDefault(
    qState === qWriteBack || qState === qEvict || (qState === qRefill && axiRead.io.finishTransfer && evictWayDirty)
  )
  axiRead.io.axi  <> DontCare
  axiWrite.io.axi <> DontCare
  io.axi.ar       <> axiRead.io.axi.ar
  io.axi.r        <> axiRead.io.axi.r

  io.axi.aw <> axiWrite.io.axi.aw
  io.axi.w  <> axiWrite.io.axi.w
  io.axi.b  <> axiWrite.io.axi.b

  io.queryCommit.indexSel := Mux(
    isQueryMissAddress,
    mshr.io.extractMiss.addr.index,
    io.fetchQuery.index
  )
  io.queryCommit.waySel := Mux(isQueryMissAddress, lruWayReg, comparator.io.bankHitWay.bits)
  io.queryCommit.bankIndexSel := Mux(
    isQueryMissAddress,
    mshr.io.extractMiss.addr.bankIndex,
    io.fetchQuery.bankIndex
  )
  //FIXME
  io.queryCommit.writeData   := io.fetchQuery.writeData
  io.queryCommit.refillData  := refillBuffer.io.allData
  io.queryCommit.isWriteBack := qState === qWriteBack
  io.queryCommit.writeMask   := Mux(qState === qWriteBack, "b1111".U(4.W), io.fetchQuery.writeMask)
  io.queryCommit.writeEnable := MuxCase(
    hitInBank,
    Array(
      (qState === qWriteBack)                                            -> true.B,
      (qState === qEvict)                                                -> false.B,
      (qState === qRefill && axiRead.io.finishTransfer && evictWayDirty) -> false.B
    )
  )
  io.queryCommit.readData      := Mux(writeQueue.io.resp.valid, writeQueue.io.resp.bits, refillBuffer.io.queryResult.bits)
  io.queryCommit.readDataValid := hitInRefillBuffer || hitInWriteQueue

  io.write.valid               := qState === qWriteBack
  io.write.bits.indexSelection := mshr.io.extractMiss.addr.index
  io.write.bits.waySelection   := lruWayReg
  io.write.bits.tagValid.tag   := mshr.io.extractMiss.addr.tag
  io.write.bits.tagValid.valid := true.B

  //FIXME: not sure if this is correct
  io.hit   := validData && (qState === qIdle || (qState === qRefill && !(evictWayDirty && axiRead.io.finishTransfer)))
  io.ready := ((validData || passThrough) && (qState === qIdle || (qState === qRefill && !(evictWayDirty && axiRead.io.finishTransfer))))

  io.dirtyWay := lruWayReg

  comparator.io.tagValid := io.fetchQuery.tagValid
  comparator.io.phyTag   := io.fetchQuery.phyTag
  comparator.io.index    := io.fetchQuery.index
  comparator.io.mshr     := mshr.io.extractMiss.addr
  //TODO: check logic here
  comparator.io.isCacheInMiss := qState =/= qIdle && qState =/= qWriteBack

  /** request valid doesn't mean if the query is valid
    * it is only asserted when there is a new miss */
  refillBuffer.io.request.valid          := newMiss
  refillBuffer.io.request.bits.bankIndex := io.fetchQuery.bankIndex
  refillBuffer.io.request.bits.writeData := io.fetchQuery.writeData
  refillBuffer.io.request.bits.writeMask := Mux(comparator.io.addrHitInRefillBuffer, io.fetchQuery.writeMask, 0.U)
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

  writeQueue.io.enqueue.valid           := (qState === qEvict) && dirtyBanks(lruWayReg)(mshr.io.extractMiss.addr.index)
  writeQueue.io.enqueue.bits.addr.tag   := mshr.io.extractMiss.tagValidAtIndex(lruWayReg).tag
  writeQueue.io.enqueue.bits.addr.index := mshr.io.extractMiss.addr.index
  writeQueue.io.enqueue.bits.data       := io.dirtyData

  when(writeQueue.io.enqueue.fire) {
    dirtyBanks(lruWayReg)(mshr.io.extractMiss.addr.index) := false.B
  }
  when(qState === qWriteBack) {
    dirtyBanks(lruWayReg)(mshr.io.extractMiss.addr.index) := refillBuffer.io.dataDirty
  }

  writeQueue.io.query.addr := Cat(io.fetchQuery.phyTag, io.fetchQuery.index, io.fetchQuery.bankIndex)
    .asTypeOf(writeQueue.io.query.addr)
  // if the query is valid, then the query could issue to write queue any cycle
  writeQueue.io.query.writeMask := Mux(io.fetchQuery.valid, 0.U(4.W), io.fetchQuery.writeMask)
  writeQueue.io.query.data      := io.fetchQuery.writeData

  axiWrite.io.addrRequest <> writeQueue.io.dequeueAddr
  axiWrite.io.data        <> writeQueue.io.dequeueData
  axiWrite.io.dataLast    := writeQueue.io.dequeueLast
  // axi write .io .axi has been moved to former place

  //TODO: don't accept miss when write back
  mshr.io.recordMiss.valid                := newMiss
  mshr.io.recordMiss.bits.addr.tag        := io.fetchQuery.phyTag
  mshr.io.recordMiss.bits.addr.index      := io.fetchQuery.index
  mshr.io.recordMiss.bits.addr.bankIndex  := io.fetchQuery.bankIndex
  mshr.io.recordMiss.bits.tagValidAtIndex := io.fetchQuery.tagValid

  // update the LRU when there is a hit in the banks, don't update otherwise
  when(hitInBank) {
    lru.update(index = io.fetchQuery.index, way = comparator.io.bankHitWay.bits)
  }
}
