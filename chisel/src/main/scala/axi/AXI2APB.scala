package axi

import chisel3._
import soc.Constants._

class AXI2APB(
  APBAddress: Int = 20,
  APBData:    Int = 8,
  L_ADDR:     Int = 64,
  L_ID:       Int = 8,
  L_Data:     Int = 128,
  L_MASK:     Int = 16
) extends BlackBox(
      Map(
        "ADDR_APB" -> APBAddress,
        "DATA_APB" -> APBData,
        "L_ADDR"   -> L_ADDR,
        "L_ID"     -> L_ID,
        "L_DATA"   -> L_Data,
        "L_MASK"   -> L_MASK
      )
    ) {
  val io = IO(new Bundle {
    val clk           = Input(Clock())
    val rst_n         = Input(Reset())
    val axi_s_awid    = Input(UInt(LID.W))
    val axi_s_awaddr  = Input(UInt(Lawaddr.W))
    val axi_s_awlen   = Input(UInt(Lawlen.W))
    val axi_s_awsize  = Input(UInt(Lawsize.W))
    val axi_s_awburst = Input(UInt(Lawburst.W))
    val axi_s_awlock  = Input(UInt(Lawlen.W))
    val axi_s_awcache = Input(UInt(Lawcache.W))
    val axi_s_awprot  = Input(UInt(Lawprot.W))
    val axi_s_awvalid = Input(Bool())
    val axi_s_awready = Output(Bool())
    val axi_s_wid     = Input(UInt(LID.W))
    val axi_s_wdata   = Input(UInt(Lwdata.W))
    val axi_s_wstrb   = Input(UInt(Lwstrb.W))
    val axi_s_wlast   = Input(Bool())
    val axi_s_wvalid  = Input(Bool())
    val axi_s_wready  = Output(Bool())
    val axi_s_bid     = Output(UInt(LID.W))
    val axi_s_bresp   = Output(UInt(Lbresp.W))
    val axi_s_bvalid  = Output(Bool())
    val axi_s_bready  = Input(Bool())
    val axi_s_arid    = Input(UInt(LID.W))
    val axi_s_araddr  = Input(UInt(Laraddr.W))
    val axi_s_arlen   = Input(UInt(Larlen.W))
    val axi_s_arsize  = Input(UInt(Larsize.W))
    val axi_s_arburst = Input(UInt(Larburst.W))
    val axi_s_arlock  = Input(UInt(Larlock.W))
    val axi_s_arcache = Input(UInt(Larcache.W))
    val axi_s_arprot  = Input(UInt(Larprot.W))
    val axi_s_arvalid = Input(Bool())
    val axi_s_arready = Output(Bool())
    val axi_s_rid     = Output(UInt(LID.W))
    val axi_s_rdata   = Output(UInt(Lrdata.W))
    val axi_s_rresp   = Output(UInt(Lrresp.W))
    val axi_s_rlast   = Output(Bool())
    val axi_s_rvalid  = Output(Bool())
    val axi_s_rready  = Input(Bool())
    val uart0_txd_i   = Input(Bool())
    val uart0_txd_o   = Output(Bool())
    val uart0_txd_oe  = Output(Bool())
    val uart0_rxd_i   = Input(Bool())
    val uart0_rxd_o   = Output(Bool())
    val uart0_rxd_oe  = Output(Bool())
    val uart0_rts_o   = Output(Bool())
    val uart0_dtr_o   = Output(Bool())
    val uart0_cts_i   = Input(Bool())
    val uart0_dsr_i   = Input(Bool())
    val uart0_dcd_i   = Input(Bool())
    val uart0_ri_i    = Input(Bool())
    val uart0_int     = Output(Bool())
  })
}
