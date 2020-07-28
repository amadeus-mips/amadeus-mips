package cpu.pipelinedCache.instCache.query

import axi.AXIIO
import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.AXIPorts.{AXIARPort, AXIRPort}
import cpu.pipelinedCache.components._
import cpu.pipelinedCache.instCache.fetch.{ICacheFetchQueryBundle, WriteTagValidBundle}
import shared.Constants.INST_ID
import shared.LRU.{PLRUMRUNM, TrueLRUNM}

@chiselName
//TODO: merge mshr and queryQueue
//TODO: optimize redundant query address
class QueryTop(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    // control path IO
    val flush = Input(Bool())
    val ready = Output(Bool())
    // datapath IO
    val fetchQuery = Input(new ICacheFetchQueryBundle)
    val bankData   = Input(Vec(cacheConfig.numOfWays, UInt((cacheConfig.bankWidth * 8).W)))
    val data       = Decoupled(UInt(32.W))
    // also data path, just write to instruction banks
    val write = Valid(new WriteTagValidBundle)

    val instructionWriteBack = Output(Vec(cacheConfig.numOfBanks, UInt((cacheConfig.bankWidth * 8).W)))

    /** is query handling a miss and preparing for write back? */
    val inAMiss = Output(Bool())
    // axi port wiring
    val axi = AXIIO.master()
  })
  // declare all the modules
  val mshr         = Module(new MSHRQ)
  val comparator   = Module(new MissComparator)
  val axiAR        = Module(new AXIARPort(addrReqWidth = 32, AXIID = INST_ID))
  val axiR         = Module(new AXIRPort(addrReqWidth = 32, AXIID = INST_ID))
  val refillBuffer = Module(new ReFillBuffer)
  val queryQueue   = Module(new AddressQueryQueue)
  val lru =
    if (cacheConfig.numOfWays > 2) PLRUMRUNM(numOfSets = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)
    else TrueLRUNM(numOfSets                           = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)
  val readHolder = Module(new ReadHolder)
  val prefetcher = Module(new PrefetchAddressGenerator)

  /** do nothing to this query, proceed to next */
  val passThrough = WireDefault(!io.fetchQuery.valid || io.flush)

  /** is the query a hit in the bank */
  val hitInBank = Wire(Bool())

  /** is the query hit in the refill buffer */
  val hitInRefillBuffer =  queryQueue.io.dequeue.valid && refillBuffer.io.queryResult.valid && comparator.io.addrHitInRefillBuffer

  /** if the read holder value is valid, don't generate a new miss  */
  val hitInReadHolder = WireDefault(readHolder.io.output.valid)

  /** is the query a hit in either places */
  val queryHit = WireDefault(hitInBank || hitInRefillBuffer || hitInReadHolder)

  /** is a new miss generated, and is guaranteed to be accepted */
  val newMiss = !queryHit && !passThrough && !queryQueue.io.queryResult

  /** is the data.valid output high? */
  val validData = WireDefault(queryHit && !passThrough)

  /** this is to benefit the integration of cache prefetch
    * In this way, all axi requests are treated equal. If there is a hit in the refill buffer, then
    * a prefetch request is issued, and the line will be written back
    * otherwise the line will be discarded, and there will be no further prefetch requests from that
    * line */
  val writeBackThisLineReg = RegInit(false.B)

  /** record if the prefeched address has been hit */
  val hitPrefecherValid      = RegInit(false.B)
  val hitPrefetcherFirstTime = RegInit(true.B)
  val hitPrefetcherAddress   = Reg(new MSHREntry())

  //FIXME: this is wrong, there could be a miss at rlast
  when(queryQueue.io.dequeue.fire) {
    writeBackThisLineReg   := false.B
    hitPrefecherValid      := false.B
    hitPrefetcherFirstTime := true.B
  }.elsewhen(hitInRefillBuffer) {
    writeBackThisLineReg := true.B
    hitPrefecherValid    := queryQueue.io.dequeue.bits.isPrefetch
    hitPrefetcherAddress := queryQueue.io.dequeue.bits.addr
  }

  val prefetchContinueQueue = Module(new Queue(new MSHREntry(), 4, true, true))
  //TODO: back pressure query queue
  prefetchContinueQueue.io.enq.bits  := queryQueue.io.dequeue.bits.addr
  prefetchContinueQueue.io.enq.valid := hitPrefecherValid && hitPrefetcherFirstTime && writeBackThisLineReg
  prefetchContinueQueue.io.deq <> prefetcher.io.continuePrefetch

  when(prefetchContinueQueue.io.enq.fire) {
    hitPrefetcherFirstTime := false.B
  }

  /** if we write back this cycle */
  val writeBackThisCycle = RegNext(axiR.io.finishTransfer && writeBackThisLineReg)

  /** the next cycle of write back, bank data is invalid, so is tag and valid data */
  hitInBank := comparator.io.bankHitWay.valid && !RegNext(writeBackThisCycle)

  /** io parts */

  io.ready      := io.data.fire || passThrough
  io.data.valid := validData
  io.data.bits := MuxCase(
    io.bankData(comparator.io.bankHitWay.bits),
    Seq(
      readHolder.io.output.valid -> readHolder.io.output.bits,
      hitInRefillBuffer          -> refillBuffer.io.queryResult.bits
    )
  )

  io.inAMiss := mshr.io.pendingMiss

  io.write.valid               := writeBackThisCycle
  io.write.bits.waySelection   := lru.getLRU(RegNext(queryQueue.io.dequeue.bits.addr.index))
  io.write.bits.indexSelection := RegNext(queryQueue.io.dequeue.bits.addr.index)
  io.write.bits.tagValid.tag   := RegNext(queryQueue.io.dequeue.bits.addr.tag)
  io.write.bits.tagValid.valid := true.B
  io.instructionWriteBack      := refillBuffer.io.allData

  io.axi    := DontCare
  io.axi.ar <> axiAR.io.ar
  io.axi.r  <> axiR.io.r

  comparator.io.tagValid := io.fetchQuery.tagValid
  comparator.io.phyTag   := io.fetchQuery.phyTag
  comparator.io.index    := io.fetchQuery.index
  comparator.io.mshr     := queryQueue.io.dequeue.bits.addr

  // when is in miss
  refillBuffer.io.queryBankIndex     := io.fetchQuery.bankIndex
  refillBuffer.io.newRefillBankIndex := queryQueue.io.dequeue.bits.addr.bankIndex
  refillBuffer.io.inputData          <> axiR.io.transferData
  refillBuffer.io.finish             := axiR.io.finishTransfer

  readHolder.io.input.valid := validData && !io.data.ready
  readHolder.io.input.bits := MuxCase(
    readHolder.io.output.bits,
    Seq(
      hitInRefillBuffer                                               -> refillBuffer.io.queryResult.bits,
      (comparator.io.bankHitWay.valid && !readHolder.io.output.valid) -> io.bankData(comparator.io.bankHitWay.bits)
    )
  )

  val arIdle :: arNewMiss :: arPrefetch :: Nil = Enum(3)
  val arSel                                    = RegInit(arIdle)

  //TODO: less state overhead
  // seperate query queue into miss address queue and prefetch queue, use another queue to track
  switch(arSel) {
    is(arIdle) {
      when(newMiss) {
        arSel := arNewMiss
      }.elsewhen(prefetcher.io.nextFetchAddress.valid) {
        arSel := arPrefetch
      }
    }
    is(arNewMiss) {
      when(axiAR.io.addrReq.fire) {
        arSel := arIdle
      }
    }
    is(arPrefetch) {
      when(axiAR.io.addrReq.fire) {
        arSel := arIdle
      }
    }
  }

  val prefetcherSelected = arSel === arPrefetch
  val missSelected       = arSel === arNewMiss

  axiAR.io.addrReq.bits := Mux(
    prefetcherSelected,
    Cat(
      prefetcher.io.nextFetchAddress.bits.tag,
      prefetcher.io.nextFetchAddress.bits.index,
      0.U((cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen).W)
    ),
    Cat(
      io.fetchQuery.phyTag,
      io.fetchQuery.index,
      io.fetchQuery.bankIndex,
      0.U(cacheConfig.bankOffsetLen.W)
    )
  )
  axiAR.io.addrReq.valid := arSel =/= arIdle && queryQueue.io.enqueue.ready

  //FIXME: this newMiss is wrong
  prefetcher.io.newPrefetchRequest.valid := newMiss && arSel === arIdle
  prefetcher.io.newPrefetchRequest.bits := Cat(
    io.fetchQuery.phyTag,
    io.fetchQuery.index,
    0.U(cacheConfig.bankIndexLen.W)
  ).asTypeOf(prefetcher.io.newPrefetchRequest.bits)
  //TODO: prefetcher queue release item at the the average speed of ar handshake,
  // enqueue at the speed of r
  prefetcher.io.nextFetchAddress.ready := prefetcherSelected && axiAR.io.addrReq.fire

  /** if there is a legitimate miss, i.e., valid request, didn't hit, and is not flushed */
  mshr.io.recordMiss.valid          := newMiss && arSel === arIdle
  mshr.io.recordMiss.bits.tag       := io.fetchQuery.phyTag
  mshr.io.recordMiss.bits.index     := io.fetchQuery.index
  mshr.io.recordMiss.bits.bankIndex := io.fetchQuery.bankIndex

  //FIXME
  mshr.io.extractMiss.ready := axiAR.io.addrReq.fire && missSelected

  //FIXME: what if query queue is empty
  queryQueue.io.enqueue.bits.isPrefetch := prefetcherSelected
  queryQueue.io.enqueue.valid           := axiAR.io.addrReq.fire
  queryQueue.io.enqueue.bits.addr.tag := Mux(
    prefetcherSelected,
    prefetcher.io.nextFetchAddress.bits.tag,
    io.fetchQuery.phyTag
  )
  queryQueue.io.enqueue.bits.addr.index := Mux(
    prefetcherSelected,
    prefetcher.io.nextFetchAddress.bits.index,
    io.fetchQuery.index
  )
  queryQueue.io.enqueue.bits.addr.bankIndex := Mux(
    prefetcherSelected,
    prefetcher.io.nextFetchAddress.bits.bankIndex,
    io.fetchQuery.bankIndex
  )
  queryQueue.io.queryAddress.tag := Mux(
    prefetcherSelected,
    prefetcher.io.nextFetchAddress.bits.tag,
    io.fetchQuery.phyTag
  )
  queryQueue.io.queryAddress.index := Mux(
    prefetcherSelected,
    prefetcher.io.nextFetchAddress.bits.index,
    io.fetchQuery.index
  )
  queryQueue.io.queryAddress.bankIndex := Mux(
    prefetcherSelected,
    prefetcher.io.nextFetchAddress.bits.bankIndex,
    io.fetchQuery.bankIndex
  )
  // query queue will dequeue every time a transfer has finished
  queryQueue.io.dequeue.ready := axiR.io.finishTransfer

  // update the LRU when there is a hit in the banks, don't update otherwise
  when(hitInBank) {
    lru.update(index = io.fetchQuery.index, way = comparator.io.bankHitWay.bits)
  }
}
