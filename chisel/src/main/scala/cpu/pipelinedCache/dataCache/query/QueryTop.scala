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

class QueryTop(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    val fetchQuery  = Input(new DCacheFetchQueryBundle)
    val write       = Output(new WriteTagValidBundle)
    val axi         = AXIIO.master()
    val queryCommit = Output(new DCacheCommitBundle())
  })

  val comparator         = Module(new MissComparator)
  val mshr               = Module(new MSHR)
  val refillBuffer       = Module(new MaskedRefillBuffer)
  val axiRead            = Module(new AXIReadPort(addrReqWidth = 32, AXIID = DATA_ID))
  val axiWrite           = Module(new AXIWritePort(AXIID = DATA_ID))
  val lru                = PLRUMRUNM(numOfSets = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)
  val writeQueue         = Module(new WriteQueue)
  val missTagValidHolder = Module(new MissTagValidHolder)

  val qIdle :: qRefill :: qEvict :: qWriteBack :: Nil = Enum(4)
  val qState = RegInit(qIdle)

  /** do nothing to this query, proceed to next */
  val passThrough = WireDefault(!io.fetchQuery.valid)

  /** is the query a hit in the bank */
  val hitInBank = WireDefault(comparator.io.bankHitWay.valid)

  /** is the query hit in the refill buffer */
  val hitInRefillBuffer = WireDefault(comparator.io.hitInRefillBuffer)

  /** is a new miss generated and *accepted* */
  val newMiss = WireDefault(mshr.io.missAddr.fire)

  /** is the query a hit in either places */
  val queryHit = WireDefault(hitInBank || hitInRefillBuffer)

  /** is the data.valid output high? */
  val validData = WireDefault(queryHit && !passThrough)

  /** select lru way for eviction */
  val lruWay = WireDefault(lru.getLRU(mshr.io.mshrInfo.index))

  /** corner case: write back is also in the idle stage */
  val inIdle      = WireDefault(mshr.io.missAddr.ready)
  val inRefill    = WireDefault(!mshr.io.missAddr.ready && !mshr.io.writeBack && !refillBuffer.io.finish)
  val inEvict     = WireDefault(!mshr.io.missAddr.ready && refillBuffer.io.finish)
  val inWriteBack = WireDefault(mshr.io.writeBack)

  io.axi <> axiRead.io.axi
  io.axi <> axiWrite.io.axi

  comparator.io.tagValid          := io.fetchQuery.tagValid
  comparator.io.phyTag            := io.fetchQuery.phyTag
  comparator.io.index             := io.fetchQuery.index
  comparator.io.mshr.bits         := mshr.io.mshrInfo
  comparator.io.mshr.valid        := !mshr.io.missAddr.ready
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
    Cat(mshr.io.mshrInfo.asUInt, 0.U(cacheConfig.bankOffsetLen.W))
  )
  axiRead.io.addrReq.valid := newMiss

  writeQueue.io.enqueue.valid     := inEvict && dirty
  writeQueue.io.enqueue.bits.addr := missTagValidHolder.io.extractTagValid(lruWay)
  writeQueue.io.enqueue.bits.data :=

  writeQueue.io.query.addr := Cat(io.fetchQuery.phyTag, io.fetchQuery.index, io.fetchQuery.bankIndex)
    .asTypeOf(writeQueue.io.query.addr)
  // if the query is valid, then the query could issue to write queue any cycle
  writeQueue.io.query.writeMask := Mux(io.fetchQuery.valid, 0.U, io.fetchQuery.writeMask)
  writeQueue.io.query.data      := io.fetchQuery.writeData

  axiWrite.io.addrRequest <> writeQueue.io.dequeueAddr
  axiWrite.io.data        <> writeQueue.io.dequeueData
  axiWrite.io.dataLast    := writeQueue.io.dequeueLast
  // axi write has been moved to former place

  missTagValidHolder.io.insertTagValid.valid := newMiss
  missTagValidHolder.io.insertTagValid.bits  := io.fetchQuery.tagValid

  // update the LRU when there is a hit in the banks, don't update otherwise
  when(hitInBank) {
    lru.update(index = io.fetchQuery.index, way = comparator.io.bankHitWay.bits)
  }
}
