package cpu.pipelinedCache.instCache

import axi.AXIIO
import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.{AXIReadPort, MissComparator, ReFillBuffer, ReadHolder}
import cpu.pipelinedCache.instCache.fetch.FetchQueryBundle
import shared.Constants.INST_ID
import shared.LRU.PLRUMRUNM

@chiselName
class QueryTop(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    // control path IO
    val flush = Input(Bool())
    val ready = Output(Bool())
    // datapath IO
    val fetchQuery = Input(new FetchQueryBundle)
    val bankData   = Input(Vec(cacheConfig.numOfWays, UInt((cacheConfig.bankWidth * 8).W)))
    val data       = Decoupled(UInt(32.W))
    // also data path, just write to instruction banks
    val writeBundle = Output(new Bundle {
      val writeEnable = Bool()
      val writeWay    = UInt(log2Ceil(cacheConfig.numOfWays).W)
      val writeIndex  = UInt(log2Ceil(cacheConfig.numOfSets).W)
      val writeTag    = UInt(cacheConfig.tagLen.W)
      val writeData   = Vec(cacheConfig.numOfBanks, UInt((cacheConfig.bankWidth * 8).W))
    })
    /** is query handling a miss and preparing for write back? */
    val inAMiss = Output(Bool())
    // axi port wiring
    val axi = AXIIO.master()
  })
  // declare all the modules
  val mshr         = Module(new MSHR)
  val comparator   = Module(new MissComparator)
  val axi          = Module(new AXIReadPort(addrReqWidth = 32, AXIID = INST_ID, burstLen = 16))
  val refillBuffer = Module(new ReFillBuffer(false))
  val lru          = PLRUMRUNM(numOfSets = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)
  val readHolder   = Module(new ReadHolder)

  /** do nothing to this query, proceed to next */
  val passThrough = WireDefault(!io.fetchQuery.valid || io.flush)

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

  /** io parts */

  io.ready      := io.data.fire || passThrough
  io.data.valid := validData
  io.data.bits := MuxCase(
    io.bankData(comparator.io.bankHitWay.bits),
    Seq(
      readHolder.io.output.valid      -> readHolder.io.output.bits,
      comparator.io.hitInRefillBuffer -> refillBuffer.io.queryResult.bits
    )
  )

  io.inAMiss := !mshr.io.missAddr.ready

  io.writeBundle.writeEnable := mshr.io.writeBack
  io.writeBundle.writeWay    := lru.getLRU(mshr.io.mshrInfo.index)
  io.writeBundle.writeIndex  := mshr.io.mshrInfo.index
  io.writeBundle.writeTag    := mshr.io.mshrInfo.tag
  io.writeBundle.writeData   := refillBuffer.io.allData

  io.axi := DontCare
  io.axi <> axi.io.axi

  comparator.io.tagValid          := io.fetchQuery.tagValid
  comparator.io.phyTag            := io.fetchQuery.phyTag
  comparator.io.index             := io.fetchQuery.index
  comparator.io.mshr.bits         := mshr.io.mshrInfo
  // is the state in a miss
  comparator.io.mshr.valid        := !mshr.io.missAddr.ready
  comparator.io.refillBufferValid := refillBuffer.io.queryResult.valid

  refillBuffer.io.addr.valid := newMiss
  refillBuffer.io.addr.bits  := io.fetchQuery.bankIndex
  refillBuffer.io.inputData  := axi.io.transferData
  refillBuffer.io.finish     := axi.io.finishTransfer

  readHolder.io.input.valid := validData && !io.data.ready
  readHolder.io.input.bits := MuxCase(
    readHolder.io.output.bits,
    Seq(
      comparator.io.hitInRefillBuffer                                 -> refillBuffer.io.queryResult.bits,
      (comparator.io.bankHitWay.valid && !readHolder.io.output.valid) -> io.bankData(comparator.io.bankHitWay.bits)
    )
  )

  axi.io.addrReq.bits := Mux(
    newMiss,
    Cat(
      io.fetchQuery.phyTag,
      io.fetchQuery.index,
      io.fetchQuery.bankIndex,
      0.U(cacheConfig.bankOffsetLen.W)
    ),
    Cat(mshr.io.mshrInfo.asUInt, 0.U(cacheConfig.bankOffsetLen.W))
  )
  axi.io.addrReq.valid := newMiss

  /** if there is a legitimate miss, i.e., valid request, didn't hit, and is not flushed */
  mshr.io.missAddr.valid          := !queryHit && !passThrough
  mshr.io.missAddr.bits.tag       := io.fetchQuery.phyTag
  mshr.io.missAddr.bits.index     := io.fetchQuery.index
  mshr.io.missAddr.bits.bankIndex := io.fetchQuery.bankIndex
  mshr.io.readyForWB              := axi.io.finishTransfer

  // update the LRU when there is a hit in the banks, don't update otherwise
  when(hitInBank) {
    lru.update(index = io.fetchQuery.index, way = comparator.io.bankHitWay.bits)
  }
}
