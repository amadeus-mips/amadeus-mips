package cpu.pipelinedCache

import axi.AXIIO
import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.components._
import cpu.pipelinedCache.components.pipelineRegister.CachePipelineStage
import cpu.pipelinedCache.instCache.ICacheController
import cpu.pipelinedCache.instCache.fetch.{FetchTop, ICacheFetchQueryBundle}
import cpu.pipelinedCache.instCache.query.QueryTop

//TODO: refactor non-module to objects
//TODO: optional enable for most banks

@chiselName
class InstrCache(cacheConfig: CacheConfig)(implicit CPUConfig: CPUConfig) extends Module {
  implicit val config = cacheConfig
  val io = IO(new Bundle {
    val addr = Flipped(Decoupled(UInt(32.W)))
    val data = Decoupled(UInt(32.W))

    /** invalidate the icache at index */
    val invalidateIndex = Flipped(Decoupled(UInt(cacheConfig.indexLen.W)))

    /** flush the stage 2 information */
    val flush = Input(Bool())
    val axi   = AXIIO.master()
  })

  val fetch       = Module(new FetchTop)
  val fetch_query = Module(new CachePipelineStage(new ICacheFetchQueryBundle))
  val query       = Module(new QueryTop)
  val instrBanks  = Module(new InstBanks)
  val controller  = Module(new ICacheController)

  io.addr.ready            := controller.io.reqReady && !io.invalidateIndex.fire
  io.invalidateIndex.ready := controller.io.invalidateReady

  fetch.io.addr                      := io.addr.bits
  fetch.io.write.valid               := controller.io.writeEnable || io.invalidateIndex.fire
  fetch.io.write.bits.tagValid.tag   := query.io.write.bits.tagValid.tag
  fetch.io.write.bits.tagValid.valid := !io.invalidateIndex.fire
  fetch.io.write.bits.indexSelection := Mux(
    io.invalidateIndex.fire,
    io.invalidateIndex.bits,
    query.io.write.bits.indexSelection
  )
  fetch.io.write.bits.waySelection := query.io.write.bits.waySelection
  fetch.io.invalidateAllWays       := io.invalidateIndex.fire

  val instrFetchData = Wire(Vec(cacheConfig.numOfWays, UInt((cacheConfig.bankWidth * 8).W)))

  for (i <- 0 until cacheConfig.numOfWays) {
    for (j <- 0 until cacheConfig.numOfBanks) {
      instrBanks.io.way_bank(i)(j).addr := Mux(
        controller.io.writeEnable,
        query.io.write.bits.indexSelection,
        fetch.io.index
      )
      instrBanks.io.way_bank(i)(j).writeEnable :=
        controller.io.writeEnable && i.U === query.io.write.bits.waySelection
      instrBanks.io.way_bank(i)(j).writeData := query.io.instructionWriteBack(j)
    }
    instrFetchData(i) := instrBanks.io.way_bank(i)(fetch_query.io.out.bankIndex).readData
  }

  //-----------------------------------------------------------------------------
  //-------------pipeline register seperating metadata fetching and query--------
  //-----------------------------------------------------------------------------

  fetch_query.io.stall        := !controller.io.stage2Free
  fetch_query.io.in.index     := fetch.io.index
  fetch_query.io.in.tagValid  := fetch.io.tagValid
  fetch_query.io.in.phyTag    := fetch.io.phyTag
  fetch_query.io.in.bankIndex := fetch.io.bankIndex
  fetch_query.io.in.valid     := io.addr.fire

  //-----------------------------------------------------------------------------
  //------------------modules and connections for query--------------------------
  //-----------------------------------------------------------------------------

  query.io.flush      := controller.io.flush
  query.io.fetchQuery := fetch_query.io.out
  query.io.bankData   := instrFetchData
  query.io.data       <> io.data
  query.io.axi        <> io.axi

  controller.io.flushReq   := io.flush
  controller.io.stage2Free := query.io.ready
  controller.io.writeBack  := query.io.write.valid
  controller.io.inMiss     := query.io.inAMiss

}

