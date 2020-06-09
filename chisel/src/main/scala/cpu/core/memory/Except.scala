// See README.md for license details.

package cpu.core.memory

import chisel3._
import cpu.core.Constants._

class Except extends Module {
  val io = IO(new Bundle {
    val pc   = Input(UInt(addrLen.W))
    val addr = Input(UInt(addrLen.W))
    val op   = Input(UInt(opLen.W))

    val cp0Status = Input(UInt(dataLen.W))
    val cp0Cause  = Input(UInt(dataLen.W))

    val inExcept = Input(Vec(exceptAmount, Bool()))

    val tlbExcept = Input(new Bundle {
      val refill   = Bool()
      val invalid  = Bool()
      val modified = Bool()
    })

    val outExcept = Output(Vec(exceptAmount, Bool()))
    val badAddr   = Output(UInt(addrLen.W))
  })

  /** interrupt happen */
  val intrExcept = (io.cp0Status(1, 0) === "b01".U) &&
    (io.cp0Status(15, 8) =/= 0.U) && (io.cp0Cause(15, 8) =/= 0.U)
  io.outExcept := io.inExcept
  when(intrExcept) { io.outExcept(EXCEPT_INTR) := true.B }
  io.outExcept(EXCEPT_DATA_TLB_R_REFILL)   := opIsLoad(io.op) && io.tlbExcept.refill
  io.outExcept(EXCEPT_DATA_TLB_R_INVALID)  := opIsLoad(io.op) && io.tlbExcept.invalid
  io.outExcept(EXCEPT_DATA_TLB_W_REFILL)   := opIsStore(io.op) && io.tlbExcept.refill
  io.outExcept(EXCEPT_DATA_TLB_W_INVALID)  := opIsStore(io.op) && io.tlbExcept.invalid
  io.outExcept(EXCEPT_DATA_TLB_W_MODIFIED) := opIsStore(io.op) && io.tlbExcept.modified

  io.badAddr := Mux(
    io.inExcept(EXCEPT_FETCH) || io.outExcept(EXCEPT_INST_TLB_REFILL) || io.outExcept(EXCEPT_INST_TLB_INVALID),
    io.pc,
    io.addr
  )

}
