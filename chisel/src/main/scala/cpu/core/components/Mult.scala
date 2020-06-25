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
  val op1 = RegNext(io.op1, 0.U(32.W))
  val op2 = RegNext(io.op2, 0.U(32.W))
  val result = Mux(io.signed, (op1.asSInt() * op2.asSInt()).asUInt(), op1 * op2)
  io.result.hi.bits := result(63, 32)
  io.result.lo.bits := result(31, 0)
}
