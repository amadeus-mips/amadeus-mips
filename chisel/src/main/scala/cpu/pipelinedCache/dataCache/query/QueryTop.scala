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
import shared.LRU.PLRUMRUNM

//TODO: Don't accept query when write back
class QueryTop(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    val fetchQuery = Input(new DCacheFetchQueryBundle)
    val write      = Output(new WriteTagValidBundle)
    val axi        = AXIIO.master()

    /** dirty data is connected to [[cpu.pipelinedCache.dataCache.DataBanks]]
      * and is queried when write queue is not full */
    val dirtyData   = Input(Vec(cacheConfig.numOfBanks, UInt(32.W)))
    val queryCommit = Output(new DCacheCommitBundle())
  })

  val comparator   = Module(new MissComparator)
  val mshr         = Module(new MSHR)
  val refillBuffer = Module(new MaskedRefillBuffer)
  val axiRead      = Module(new AXIReadPort(addrReqWidth = 32, AXIID = DATA_ID))
  val axiWrite     = Module(new AXIWritePort(AXIID = DATA_ID))
  val lru          = PLRUMRUNM(numOfSets = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)
  val writeQueue   = Module(new WriteQueue)

  val qIdle :: qRefill :: qEvict :: qWriteBack :: Nil = Enum(4)
  val qState                                          = RegInit(qIdle)

  /** do nothing to this query, proceed to next */
  val passThrough = WireDefault(!io.fetchQuery.valid)

  /** is the query a hit in the bank */
  val hitInBank = WireDefault(comparator.io.bankHitWay.valid)

  /** is the query hit in the refill buffer */
  val hitInRefillBuffer = WireDefault(comparator.io.hitInRefillBuffer)

  //TODO: use this
  val hitInWriteQueue = WireDefault(false.B)

  //TODO: use this
  val writeQueueAvailable = WireDefault(false.B)

  /** is a new miss generated, but is not guarateed to be accepted */
  val newMiss = WireDefault(!queryHit && !passThrough)

  /** is the query a hit in either places */
  val queryHit = WireDefault(hitInBank || hitInRefillBuffer || hitInWriteQueue)

  /** is the data.valid output high? */
  val validData = WireDefault(queryHit && !passThrough)

  /** select lru way for eviction */
  val lruWay = WireDefault(lru.getLRU(mshr.io.mshrInfo.index))

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

        /** this is a bit of combinational logic. When rlast comes,
          * and write queue is valid, then evict directly. */
        qState := Mux(writeQueueAvailable, qWriteBack, qEvict)
      }
    }
    is(qEvict) {
      when(writeQueueAvailable) {
        qState := qWriteBack
      }
    }
    is(qWriteBack) {
      //TODO: check this logic
      qState := Mux(newMiss, qRefill, qIdle)
    }
  }

  io.axi <> axiRead.io.axi
  io.axi <> axiWrite.io.axi

  comparator.io.tagValid          := io.fetchQuery.tagValid
  comparator.io.phyTag            := io.fetchQuery.phyTag
  comparator.io.index             := io.fetchQuery.index
  comparator.io.mshr              := mshr.io.extractMiss.addr
  comparator.io.refillBufferValid := refillBuffer.io.queryResult.valid

  refillBuffer.io.request.valid  := newMiss
  refillBuffer.io.bankIndex.bits := io.fetchQuery.bankIndex
  refillBuffer.io.inputData      := axiRead.io.transferData
  refillBuffer.io.finish         := axiRead.io.finishTransfer

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

  writeQueue.io.enqueue.valid     := qState === qWriteBack && dirty
  writeQueue.io.enqueue.bits.addr := mshr.io.extractMiss.tagValidAtIndex(lruWay)
  writeQueue.io.enqueue.bits.data := io.dirtyData

  writeQueue.io.query.addr := Cat(io.fetchQuery.phyTag, io.fetchQuery.index, io.fetchQuery.bankIndex)
    .asTypeOf(writeQueue.io.query.addr)
  // if the query is valid, then the query could issue to write queue any cycle
  writeQueue.io.query.writeMask := Mux(io.fetchQuery.valid, 0.U, io.fetchQuery.writeMask)
  writeQueue.io.query.data      := io.fetchQuery.writeData

  axiWrite.io.addrRequest <> writeQueue.io.dequeueAddr
  axiWrite.io.data        <> writeQueue.io.dequeueData
  axiWrite.io.dataLast    := writeQueue.io.dequeueLast
  // axi write .io .axi has been moved to former place

  mshr.io.recordMiss.valid                := newMiss && (qState === qIdle || qState === qWriteBack)
  mshr.io.recordMiss.bits.addr.tag        := io.fetchQuery.phyTag
  mshr.io.recordMiss.bits.addr.index      := io.fetchQuery.index
  mshr.io.recordMiss.bits.addr.bankIndex  := io.fetchQuery.bankIndex
  mshr.io.recordMiss.bits.tagValidAtIndex := io.fetchQuery.tagValid


  // update the LRU when there is a hit in the banks, don't update otherwise
  when(hitInBank) {
    lru.update(index = io.fetchQuery.index, way = comparator.io.bankHitWay.bits)
  }
}
