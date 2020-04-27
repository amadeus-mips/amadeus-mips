// See README.md for license details.

package gs132

import chisel3._
import chisel3.util.HasBlackBoxResource

/**
  * Can't work
  */
@deprecated
class GS132 extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val intr = Input(UInt(6.W))
    val aclk = Input(Clock())
    val reset = Input(Reset())

    val arid = Output(UInt(4.W))
    val araddr = Output(UInt(32.W))
    val arlen = Output(UInt(4.W))
    val arsize = Output(UInt(3.W))
    val arburst = Output(UInt(2.W))
    val arlock = Output(UInt(2.W))
    val arcache = Output(UInt(4.W))
    val arprot = Output(UInt(3.W))
    val arvalid = Output(Bool())
    val arready = Input(Bool())

    val rid = Input(UInt(4.W))
    val rdata = Input(UInt(32.W))
    val rresp = Input(UInt(2.W))
    val rlast = Input(Bool())
    val rvalid = Input(Bool())
    val rready = Output(Bool())

    val awid = Output(UInt(4.W))
    val awaddr = Output(UInt(32.W))
    val awlen = Output(UInt(4.W))
    val awsize = Output(UInt(3.W))
    val awburst = Output(UInt(2.W))
    val awlock = Output(UInt(2.W))
    val awcache = Output(UInt(4.W))
    val awprot = Output(UInt(3.W))
    val awvalid = Output(Bool())
    val awready = Input(Bool())

    val wid = Output(UInt(4.W))
    val wdata = Output(UInt(32.W))
    val wstrb = Output(UInt(4.W))
    val wlast = Output(Bool())
    val wvalid = Output(Bool())
    val wready = Input(Bool())

    val bid = Input(UInt(4.W))
    val bresp = Input(UInt(2.W))
    val bvalid = Input(Bool())
    val bready = Output(Bool())

    val debug_wb_pc = Output(UInt(32.W))
    val debug_wb_rf_wen = Output(UInt(4.W))
    val debug_wb_rf_wnum = Output(UInt(5.W))
    val debug_wb_rf_wdata = Output(UInt(32.W))
  })

  override def desiredName = "mycpu_top"

  addResource("/gs132_axi_verilog.v")
}
