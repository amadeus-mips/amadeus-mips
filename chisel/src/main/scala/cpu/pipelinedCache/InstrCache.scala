package cpu.pipelinedCache

import axi.AXIIO
import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.pipelinedCache.components._
import cpu.pipelinedCache.instCache.fetch.FetchQueryBundle
import cpu.pipelinedCache.instCache.{FetchTop, MSHR}
import firrtl.options.TargetDirAnnotation
import shared.Constants._
import shared.LRU.PLRUMRUNM

//TODO: refactor non-module to objects
//TODO: optional enable for most banks

@chiselName
class InstrCache(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    val addr = Flipped(Decoupled(UInt(32.W)))
    val data = Valid(UInt(32.W))

    /** flush the stage 2 information */
    val flush = Input(Bool())
    val axi = AXIIO.master()
  })

  val fetch = Module(new FetchTop)
  val mshr = Module(new MSHR)
  val comparator = Module(new MissComparator)
  val axi = Module(new AXIReadPort(addrReqWidth = 32, AXIID = INST_ID, burstLen = 16))
  val refillBuffer = Module(new ReFillBuffer(false))
  val lru = PLRUMRUNM(numOfSets = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)
  val fetch_query = Module(new CachePipelineStage(new FetchQueryBundle))

  /** if there is a hit in either bank or refill buffer */
  val hit = Wire(Bool())

  /** if there is a hit in the bank */
  val hitInBank = Wire(Bool())
  val newMiss = Wire(Bool())

  /** pass through control signal */
  val passThrough = Wire(Bool())

  // when there is a stall caused by a miss or the cache is performing writeback
  io.addr.ready := (hit || passThrough) && !mshr.io.writeBack

  io.axi <> axi.io.axi

  fetch.io.addr := io.addr.bits
  fetch.io.writeTagValid.valid := mshr.io.writeBack
  fetch.io.writeTagValid.bits.tagValid.tag := mshr.io.mshrInfo.tag
  fetch.io.writeTagValid.bits.tagValid.valid := true.B
  fetch.io.writeTagValid.bits.waySelection := lru.getLRU(mshr.io.mshrInfo.index)

  val instrFetchData = Wire(Vec(4, UInt((cacheConfig.bankWidth * 8).W)))

  val instrBanks = Module(new InstBanks)
  for (i <- 0 until cacheConfig.numOfWays) {
    for (j <- 0 until cacheConfig.numOfBanks) {
      instrBanks.io.way_bank(i)(j).addr := Mux(mshr.io.writeBack, mshr.io.mshrInfo.index, fetch.io.index)
      instrBanks.io.way_bank(i)(j).writeEnable :=
        mshr.io.writeBack && i.U === lru.getLRU(mshr.io.mshrInfo.index)
      instrBanks.io.way_bank(i)(j).writeData := refillBuffer.io.allData(j)
      when(j.U === fetch_query.io.out.bankIndex) {
        instrFetchData(i) := instrBanks.io.way_bank(i)(j).readData
      }
    }
  }

  //-----------------------------------------------------------------------------
  //-------------pipeline register seperating metadata fetching and query--------
  //-----------------------------------------------------------------------------

  fetch_query.io.stall := !hit && !passThrough
  fetch_query.io.in.index := fetch.io.index
  fetch_query.io.in.tagValid := fetch.io.tagValid
  fetch_query.io.in.phyTag := fetch.io.phyTag
  fetch_query.io.in.bankIndex := fetch.io.bankIndex
  fetch_query.io.in.invalid := !io.addr.valid

  //-----------------------------------------------------------------------------
  //------------------modules and connections for query--------------------------
  //-----------------------------------------------------------------------------
  hit := hitInBank & comparator.io.hitInRefillBuffer
  hitInBank := comparator.io.bankHitWay.valid
  newMiss := mshr.io.missAddr.fire
  passThrough := fetch_query.io.out.invalid || io.flush

  comparator.io.tagValid := fetch_query.io.out.tagValid
  comparator.io.phyTag := fetch_query.io.out.phyTag
  comparator.io.index := fetch_query.io.out.index
  comparator.io.mshr.bits := mshr.io.mshrInfo
  comparator.io.mshr.valid := !mshr.io.missAddr.ready
  comparator.io.refillBufferValid := refillBuffer.io.queryResult.valid

  axi.io.addrReq.bits := Mux(
    newMiss,
    Cat(fetch_query.io.out.phyTag, fetch_query.io.out.index, fetch_query.io.out.bankIndex),
    mshr.io.mshrInfo.asUInt
  )
  axi.io.addrReq.valid := newMiss || !mshr.io.missAddr.ready

  refillBuffer.io.addr.valid := newMiss
  refillBuffer.io.addr.bits := fetch_query.io.out.bankIndex
  refillBuffer.io.inputData := axi.io.transferData
  refillBuffer.io.finish := axi.io.finishTransfer

  /** if there is a legitimate miss, i.e., valid request, didn't hit, and is not flushed */
  mshr.io.missAddr.valid := !hit && !passThrough
  mshr.io.missAddr.bits.tag := fetch_query.io.out.phyTag
  mshr.io.missAddr.bits.index := fetch_query.io.out.index
  mshr.io.missAddr.bits.bankIndex := fetch_query.io.out.bankIndex
  mshr.io.readyForWB := axi.io.finishTransfer

  // update the LRU when there is a hit in the banks, don't update otherwise
  when(hitInBank) {
    lru.update(index = fetch_query.io.out.index, way = comparator.io.bankHitWay.bits)
  }

  io.data.valid := hit && !passThrough
  io.data.bits := Mux(
    comparator.io.hitInRefillBuffer,
    refillBuffer.io.queryResult.bits,
    instrFetchData(comparator.io.bankHitWay.bits)
  )
}

object ICacheElaborate extends App {
  implicit val cacheConfig = new CacheConfig
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new InstrCache()), TargetDirAnnotation("generation"))
  )
}
