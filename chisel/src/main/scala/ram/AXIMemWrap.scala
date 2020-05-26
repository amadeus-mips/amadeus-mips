package ram

import chisel3._
import chisel3.util._
import axi.AXIIO

@deprecated
class AXIMemWrap(
  dataWidth:      Int = 32,
  addrWidth:      Int = 32,
  strbWidth:      Int = 4,
  idWidth:        Int = 3,
  pipelineOutput: Int = 0,
  fileName:       String
) extends Module {
  val io = IO(new Bundle() {
    val axi = AXIIO.slave()
  })

  val axiMem = Module(
    new AXIMemBlackBox(
      dataWidth,
      addrWidth,
      strbWidth,
      idWidth,
      pipelineOutput,
      fileName
    )
  )
  axiMem.io.clk := clock
  axiMem.io.rst := reset

  axiMem.io.s_axi_awid := io.axi.aw.bits.id
  axiMem.io.s_axi_awaddr := io.axi.aw.bits.addr
  axiMem.io.s_axi_awlen := io.axi.aw.bits.len
  axiMem.io.s_axi_awsize := io.axi.aw.bits.size
  axiMem.io.s_axi_awburst := io.axi.aw.bits.burst
  axiMem.io.s_axi_awlock := io.axi.aw.bits.lock
  axiMem.io.s_axi_awcache := io.axi.aw.bits.cache
  axiMem.io.s_axi_awprot := io.axi.aw.bits.prot
  axiMem.io.s_axi_awvalid := io.axi.aw.valid
  io.axi.aw.ready := axiMem.io.s_axi_awready
  axiMem.io.s_axi_wdata := io.axi.w.bits.data
  axiMem.io.s_axi_wstrb := io.axi.w.bits.strb
  axiMem.io.s_axi_wlast := io.axi.w.bits.last
  axiMem.io.s_axi_wvalid := io.axi.w.valid
  io.axi.w.ready := axiMem.io.s_axi_wready
  io.axi.b.bits.id := axiMem.io.s_axi_bid
  io.axi.b.bits.resp := axiMem.io.s_axi_bresp
  io.axi.b.valid := axiMem.io.s_axi_bvalid
  axiMem.io.s_axi_bready := io.axi.b.ready
  axiMem.io.s_axi_arid := io.axi.ar.bits.id
  axiMem.io.s_axi_araddr := io.axi.ar.bits.addr
  axiMem.io.s_axi_arlen := io.axi.ar.bits.len
  axiMem.io.s_axi_arsize := io.axi.ar.bits.size
  axiMem.io.s_axi_arburst := io.axi.ar.bits.burst
  axiMem.io.s_axi_arlock := io.axi.ar.bits.lock
  axiMem.io.s_axi_arcache := io.axi.ar.bits.cache
  axiMem.io.s_axi_arprot := io.axi.ar.bits.prot
  axiMem.io.s_axi_arvalid := io.axi.ar.valid
  io.axi.ar.ready := axiMem.io.s_axi_arready
  io.axi.r.bits.id := axiMem.io.s_axi_rid
  io.axi.r.bits.data := axiMem.io.s_axi_rdata
  io.axi.r.bits.resp := axiMem.io.s_axi_rresp
  io.axi.r.bits.last := axiMem.io.s_axi_rlast
  io.axi.r.valid := axiMem.io.s_axi_rvalid
  axiMem.io.s_axi_rready := io.axi.r.ready
}
