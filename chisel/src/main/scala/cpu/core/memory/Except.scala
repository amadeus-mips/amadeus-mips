// See README.md for license details.

package cpu.core.memory

import chisel3._
import chisel3.util.{Cat, MuxCase}
import cpu.common.{CauseBundle, EBaseBundle, StatusBundle}
import cpu.core.Constants._

class Except extends Module {
  val io = IO(new Bundle {
    val pc   = Input(UInt(addrLen.W))
    val addr = Input(UInt(addrLen.W))
    val op   = Input(UInt(opLen.W))

    val instValid = Input(Bool())

    val cp0Status = Input(new StatusBundle)
    val cp0Cause  = Input(new CauseBundle)

    val inExcept = Input(Vec(exceptAmount, Bool()))

    val tlbExcept = Input(new Bundle {
      val refill   = Bool()
      val invalid  = Bool()
      val modified = Bool()
    })

    val outExcept  = Output(Vec(exceptAmount, Bool()))
    val badAddr    = Output(UInt(addrLen.W))
  })

  /** interrupt happen */
  val intrExcept = (!io.cp0Status.exl && io.cp0Status.ie) &&
    ((io.cp0Status.IM & Cat(io.cp0Cause.ipHard, io.cp0Cause.ipSoft)) =/= 0.U) && io.instValid
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
