package uart

import chisel3._

class UartTop extends BlackBox {
  val io = IO(new Bundle {
    val PCLK        = Input(Clock())
    val PRST_       = Input(Reset())
    val PSEL        = Input(Bool())
    val PENABLE     = Input(Bool())
    val PWRITE      = Input(Bool())
    val PADDR       = Input(UInt(8.W))
    val PWDATA      = Input(UInt(8.W))
    val URT_PRDATA  = Output(UInt(8.W))
    val INT         = Output(Bool())
    val clk_carrier = Input(Bool())
    val TXD_i       = Input(Bool())
    val TXD_o       = Output(Bool())
    val TXD_oe      = Output(Bool())
    val RXD_i       = Input(Bool())
    val RXD_o       = Output(Bool())
    val RXD_oe      = Output(Bool())
    val RTS         = Output(Bool())
    val CTS         = Input(Bool())
    val DSR         = Input(Bool())
    val DCD         = Input(Bool())
    val DTR         = Output(Bool())
    val RI          = Input(Bool())
  })

  val pRst = WireDefault(!io.PRST_.asBool)
  val we   = WireDefault(io.PSEL && io.PENABLE && io.PWRITE)
  val re   = WireDefault(io.PSEL && io.PENABLE && !io.PWRITE)


}
