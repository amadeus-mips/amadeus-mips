package gs132

import _root_.common._
import chisel3._

/**
  * Can't work
  */
@deprecated
class GS132Wrap extends Module {
  val io = IO(new Bundle {
    val intr = Input(UInt(6.W))
    val axi = AXIIO.master()
    val debug = Output(new DebugBundle)
  })

  val g = Module(new GS132)

  g.io.intr := io.intr
  g.io.aclk := clock
  g.io.reset := reset

  io.axi.ar.bits.id := g.io.arid
  io.axi.ar.bits.addr := g.io.araddr
  io.axi.ar.bits.len := g.io.arlen
  io.axi.ar.bits.size := g.io.arsize
  io.axi.ar.bits.burst := g.io.arburst
  io.axi.ar.bits.lock := g.io.arlock
  io.axi.ar.bits.cache := g.io.arcache
  io.axi.ar.bits.prot := g.io.arprot
  io.axi.ar.valid := g.io.arvalid
  g.io.arready := io.axi.ar.ready

  g.io.rid := io.axi.r.bits.id
  g.io.rdata := io.axi.r.bits.data
  g.io.rresp := io.axi.r.bits.resp
  g.io.rlast := io.axi.r.bits.last
  g.io.rvalid := io.axi.r.valid
  io.axi.r.ready := g.io.rready

  io.axi.aw.bits.id := g.io.awid
  io.axi.aw.bits.addr := g.io.awaddr
  io.axi.aw.bits.len := g.io.awlen
  io.axi.aw.bits.size := g.io.awsize
  io.axi.aw.bits.burst := g.io.awburst
  io.axi.aw.bits.lock := g.io.awlock
  io.axi.aw.bits.cache := g.io.awcache
  io.axi.aw.bits.prot := g.io.awprot
  io.axi.aw.valid := g.io.awvalid
  g.io.awready := io.axi.aw.ready

  io.axi.w.bits.id := g.io.wid
  io.axi.w.bits.data := g.io.wdata
  io.axi.w.bits.strb := g.io.wstrb
  io.axi.w.bits.last := g.io.wlast
  io.axi.w.valid := g.io.wvalid
  g.io.wready := io.axi.w.ready

  g.io.bid := io.axi.b.bits.id
  g.io.bresp := io.axi.b.bits.resp
  g.io.bvalid := io.axi.b.valid
  io.axi.b.ready := g.io.bready

  io.debug.wbPC := g.io.debug_wb_pc
  io.debug.wbRegFileWEn := g.io.debug_wb_rf_wen
  io.debug.wbRegFileWNum := g.io.debug_wb_rf_wnum
  io.debug.wbRegFileWData := g.io.debug_wb_rf_wdata
}
