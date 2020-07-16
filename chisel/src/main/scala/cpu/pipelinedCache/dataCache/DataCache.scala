package cpu.pipelinedCache.dataCache

import axi.AXIIO
import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.pipelineRegister.CachePipelineStage
import cpu.pipelinedCache.dataCache.fetch.FetchTop
import cpu.pipelinedCache.dataCache.query.QueryTop

class DataCache(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {

    /** input request io, ready decitates whether it's ready to
      * accecpt next request query */
    val request = Flipped(Decoupled(new Bundle {
      val address   = UInt(32.W)
      val writeMask = UInt(4.W)
      val writeData = UInt(32.W)
    }))

    /** denotes whether a read or a write has successfully been executed */
    val commit = Output(Bool())

    /** the data read from last cycle's commit */
    val readData = Output(UInt(32.W))

    /** axi IO */
    val axi = AXIIO.master()
  })

  val fetch       = Module(new FetchTop)
  val fetch_query = Module(new CachePipelineStage(new DCacheFetchQueryBundle))
  val query       = Module(new QueryTop)
  val dataBanks   = Module(new DataBanks)
  val controller  = Module(new DCacheController)

  io.request.ready := controller.io.inputReady
  io.axi           <> query.io.axi

  fetch.io.addr  := io.request.bits.address
  fetch.io.write := query.io.write

  //-----------------------------------------------------------------------------
  //------------------pipeline register seperating fetch and query---------------
  //-----------------------------------------------------------------------------
  fetch_query.io.stall        := !controller.io.stage2Free
  fetch_query.io.in.valid     := io.request.fire
  fetch_query.io.in.tagValid  := fetch.io.tagValid
  fetch_query.io.in.index     := fetch.io.addrResult.index
  fetch_query.io.in.phyTag    := fetch.io.addrResult.phyTag
  fetch_query.io.in.bankIndex := fetch.io.addrResult.bankIndex
  fetch_query.io.in.writeData := io.request.bits.writeData
  fetch_query.io.in.writeMask := io.request.bits.writeMask

  //-----------------------------------------------------------------------------
  //------------------query stage--------------------------------------
  //-----------------------------------------------------------------------------
  query.io.fetchQuery := fetch_query.io.out



  val readData = Wire(Vec(cacheConfig.numOfWays, UInt((cacheConfig.bankWidth * 8).W)))

  for (i <- 0 until cacheConfig.numOfWays) {
    for (k <- 0 until cacheConfig.numOfBanks) {
      dataBanks.io.way_bank(i)(k).addr      := fetch_query.io.out.index
      dataBanks.io.way_bank(i)(k).writeMask := fetch_query.io.out.writeMask
      dataBanks.io.way_bank(i)(k).writeData := fetch_query.io.out.writeData
    }
//    readData(i) := dataBanks.io.way_bank(i)(TBD_bankSel).readData
  }


}
