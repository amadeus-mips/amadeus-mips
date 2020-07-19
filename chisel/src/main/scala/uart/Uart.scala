package uart

import chisel3._

class UartIO extends Bundle {
  val rx_i = Input(Bool())
  val rx_o = Output(Bool())
  val tx_i = Input(Bool())
  val tx_o = Output(Bool())
}

class Uart extends Module{
  val io = IO(new UartIO)
}
