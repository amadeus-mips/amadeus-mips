// See README.md for license details.

package cpu.core.components

import chisel3._

/**
 * Simple implement, return in current cycle
 */
class Div extends Module {
  val io = IO(new MultDivIO)
  io.result.hi.valid := true.B
  io.result.lo.valid := true.B
  io.result.hi.bits := Mux(io.signed,
    (io.op1.asSInt() % io.op2.asSInt()).asUInt(),
    io.op1 % io.op2
  )
  io.result.lo.bits := Mux(io.signed,
    (io.op1.asSInt() / io.op2.asSInt()).asUInt(),
    io.op1 / io.op2
  )

}
