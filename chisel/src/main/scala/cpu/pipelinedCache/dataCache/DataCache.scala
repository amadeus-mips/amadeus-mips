package cpu.pipelinedCache.dataCache

import axi.AXIIO
import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.pipelineRegister.CachePipelineStage
import cpu.pipelinedCache.dataCache.fetch.FetchTop
import cpu.pipelinedCache.dataCache.query.QueryTop

//TODO: freshess of tag and valid
//TODO: hit in writing entry in write queue
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

  //-----------------------------------------------------------------------------
  //------------------instantiate all the modules--------------------------------
  //-----------------------------------------------------------------------------
  val fetch        = Module(new FetchTop)
  val fetch_query  = Module(new CachePipelineStage(new DCacheFetchQueryBundle))
  val query        = Module(new QueryTop)
  val dataBanks    = Module(new DataBanks)
  val query_commit = Module(new CachePipelineStage(new DCacheCommitBundle))
  val controller   = Module(new DCacheController)

  val readDataWire = Wire(Vec(cacheConfig.numOfWays, UInt((cacheConfig.bankWidth * 8).W)))
  val dirtyData    = Wire(Vec(cacheConfig.numOfBanks, UInt((cacheConfig.bankWidth * 8).W)))

  io.request.ready := controller.io.inputReady
  io.axi           <> query.io.axi
  io.commit        := query.io.hit
  io.readData := Mux(
    query_commit.io.out.readDataValid,
    query_commit.io.out.readData,
    readDataWire(query_commit.io.out.waySel)
  )

  fetch.io.addr  := io.request.bits.address
  fetch.io.write := query.io.writeBack

  //-----------------------------------------------------------------------------
  //------------------pipeline register seperating fetch and query---------------
  //-----------------------------------------------------------------------------

  fetch_query.io.stall        := !controller.io.stage2Ready
  fetch_query.io.in.valid     := io.request.fire && !query.io.writeBack.valid
  fetch_query.io.in.tagValid  := fetch.io.tagValid
  fetch_query.io.in.index     := fetch.io.addrResult.index
  fetch_query.io.in.phyTag    := fetch.io.addrResult.phyTag
  fetch_query.io.in.bankIndex := fetch.io.addrResult.bankIndex
  fetch_query.io.in.writeData := io.request.bits.writeData
  fetch_query.io.in.writeMask := io.request.bits.writeMask

  /** update the tag and valid information when there is a write back
    * don't touch the valid signal */
  when(
    query.io.writeBack.valid && (query.io.writeBack.bits.tagValid.tag === fetch_query.io.out.phyTag) && (query.io.writeBack.bits.addr.index === fetch_query.io.out.index)
      && fetch_query.io.out.valid
  ) {
    fetch_query.io.stall                                            := false.B
    fetch_query.io.in.valid                                         := io.request.fire && !query.io.writeBack.valid
    fetch_query.io.in.tagValid                                      := fetch_query.io.out.tagValid
    fetch_query.io.in.tagValid(query.io.writeBack.bits.addr.waySel) := query.io.writeBack.bits.tagValid
    fetch_query.io.in.index                                         := fetch_query.io.out.index
    fetch_query.io.in.phyTag                                        := fetch_query.io.out.phyTag
    fetch_query.io.in.bankIndex                                     := fetch_query.io.out.bankIndex
    fetch_query.io.in.writeData                                     := fetch_query.io.out.writeData
    fetch_query.io.in.writeMask                                     := fetch_query.io.out.writeMask
  }

  //-----------------------------------------------------------------------------
  //------------------query stage--------------------------------------
  //-----------------------------------------------------------------------------

  query.io.fetchQuery := fetch_query.io.out
  query.io.dirtyData  := dirtyData

  query_commit.io.stall := false.B
  query_commit.io.in    := query.io.queryCommit

  // when query is not write back, it is not ready
  controller.io.stage2Ready := query.io.ready && !query.io.writeBack.valid

  val isWriteQuery = query.io.queryCommit.writeEnable

  for (i <- 0 until cacheConfig.numOfWays) {
    for (k <- 0 until cacheConfig.numOfBanks) {
      dataBanks.io.way_bank(i)(k).addr := Mux(
        query.io.writeBack.valid,
        query.io.writeBack.bits.addr.index,
        query.io.queryCommit.indexSel
      )
      dataBanks.io.way_bank(i)(k).writeMask := MuxCase(
        0.U,
        Array(
          (query.io.writeBack.valid && query.io.writeBack.bits.addr.waySel === i.U)                                              -> "b1111".U(4.W),
          (query.io.queryCommit.writeEnable && query.io.queryCommit.waySel === i.U && query.io.queryCommit.bankIndexSel === k.U) -> query.io.queryCommit.writeMask.asUInt
        )
      )
      dataBanks.io.way_bank(i)(k).writeData := Mux(
        query.io.writeBack.valid,
        query.io.writeBack.bits.data(k),
        query.io.queryCommit.writeData
      )
      dirtyData(k) := dataBanks.io.way_bank(query.io.dirtyWay)(k).readData
    }
    readDataWire(i) := dataBanks.io.way_bank(i)(query_commit.io.out.bankIndexSel).readData
  }

}
