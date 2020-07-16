package cpu.pipelinedCache.dataCache.query

import axi.AXIIO
import chisel3._
import chisel3.util.Cat
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.AXIPorts.{AXIReadPort, AXIWritePort}
import cpu.pipelinedCache.components.{MSHR, MissComparator, ReFillBuffer}
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

  val comparator   = Module(new MissComparator)
  val mshr         = Module(new MSHR)
  val refillBuffer = Module(new ReFillBuffer)
  val axiRead      = Module(new AXIReadPort(addrReqWidth = 32, AXIID = DATA_ID))
  val axiWrite     = Module(new AXIWritePort(AXIID = DATA_ID))
  val lru          = PLRUMRUNM(numOfSets = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)

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

  io.axi <> axiRead.io.axi
  io.axi <> axiWrite.io.axi

  comparator.io.tagValid          := io.fetchQuery.tagValid
  comparator.io.phyTag            := io.fetchQuery.phyTag
  comparator.io.index             := io.fetchQuery.index
  comparator.io.mshr.bits         := mshr.io.mshrInfo
  comparator.io.mshr.valid        := !mshr.io.missAddr.ready
  comparator.io.refillBufferValid := refillBuffer.io.queryResult.valid

  refillBuffer.io.bankIndex.valid := newMiss
  refillBuffer.io.bankIndex.bits  := io.fetchQuery.bankIndex
  refillBuffer.io.inputData  := axiRead.io.transferData
  refillBuffer.io.finish     := axiRead.io.finishTransfer

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

  // update the LRU when there is a hit in the banks, don't update otherwise
  when(hitInBank) {
    lru.update(index = io.fetchQuery.index, way = comparator.io.bankHitWay.bits)
  }
}
