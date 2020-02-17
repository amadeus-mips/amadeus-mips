// See README.md for license details.

package cpu.core.memory

import chisel3._
import cpu.core.Constants._

class Except extends Module {
  val io = IO(new Bundle {
    val pc =  Input(UInt(addrLen.W))
    val addr = Input(UInt(addrLen.W))

    val cp0Status = Input(UInt(dataLen.W))
    val cp0Cause = Input(UInt(dataLen.W))

    val inExcept = Input(Vec(exceptAmount, Bool()))

    val outExcept = Output(Vec(exceptAmount, Bool()))
    val badAddr = Output(UInt(addrLen.W))
  })

  /** interrupt happen */
  val intrExcept = (io.cp0Status(1,0) === "b10".U) &&
    (io.cp0Status(15,8) =/= 0.U) && (io.cp0Cause(15,8) =/= 0.U)
  io.outExcept <> io.inExcept
  when(intrExcept) { io.outExcept(EXCEPT_INTR) := true.B }

  io.badAddr := Mux(io.inExcept(EXCEPT_FETCH), io.pc, io.addr)

}
