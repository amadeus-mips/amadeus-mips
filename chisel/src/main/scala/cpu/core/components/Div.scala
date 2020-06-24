// See README.md for license details.

package cpu.core.components

import chisel3._
import chisel3.util.HasBlackBoxResource
import cpu.CPUConfig

/**
  * Simple implement, return in current cycle
  */
class Div(implicit conf: CPUConfig) extends Module {
  val io  = IO(new MultDivIO)
  val div = Module(new DIVBlackBox())
  div.io.rst          := reset
  div.io.clk          := clock
  div.io.signed_div_i := io.signed
  div.io.opdata1_i    := io.op1
  div.io.opdata2_i    := io.op2
  div.io.start_i      := io.enable
  div.io.annul_i      := io.flush
  io.result.hi.bits   := div.io.result_o(63, 32)
  io.result.lo.bits   := div.io.result_o(31, 0)
  io.result.hi.valid  := div.io.ready_o
  io.result.lo.valid  := div.io.ready_o

}

class DIVBlackBox extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val rst          = Input(Reset())
    val clk          = Input(Clock())
    val signed_div_i = Input(Bool())
    val opdata1_i    = Input(UInt(32.W))
    val opdata2_i    = Input(UInt(32.W))
    val start_i      = Input(Bool())
    val annul_i      = Input(Bool())
    val result_o     = Output(UInt(64.W))
    val ready_o      = Output(Bool())
  })
  addResource("/div.v")

  override def desiredName: String = "div"
}
