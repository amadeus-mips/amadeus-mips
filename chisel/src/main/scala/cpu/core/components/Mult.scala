// See README.md for license details.

package cpu.core.components

import chisel3._
import cpu.CPUConfig

/**
  * Simple implement, return in current cycle
  */
class Mult(implicit conf: CPUConfig) extends Module {
  val io = IO(new MultDivIO)
  io.result.hi.valid := RegNext(io.enable, false.B)
  io.result.lo.valid := RegNext(io.enable, false.B)
  val result = RegNext(Mux(io.signed, (io.op1.asSInt() * io.op2.asSInt()).asUInt(), io.op1 * io.op2), 0.U(32.W))
  io.result.hi.bits := result(63, 32)
  io.result.lo.bits := result(31, 0)
}
