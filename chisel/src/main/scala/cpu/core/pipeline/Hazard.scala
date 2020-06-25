// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._

class Hazard extends Module {
  val io = IO(new Bundle {
    val except              = Input(Vec(exceptAmount, Bool()))
    val EPC                 = Input(UInt(dataLen.W))
    val stallReqFromFetch   = Input(Bool())
    val stallReqFromDecode  = Input(Bool())
    val stallReqFromExecute = Input(Bool())
    val stallReqFromMemory  = Input(Bool())

    val flush   = Output(Bool())
    val flushPC = Output(UInt(dataLen.W))
    val stall   = Output(UInt(cpuStallLen.W))
  })

  val hasExcept = io.except.asUInt().orR()

  io.flush   := hasExcept
  io.flushPC := Mux(io.except(EXCEPT_ERET), io.EPC, exceptPC)

  io.stall := MuxCase(
    0.U,
    Array(
      hasExcept              -> 0.U,
      io.stallReqFromMemory  -> "b011111".U,
      io.stallReqFromExecute -> "b001111".U,
      io.stallReqFromDecode  -> "b000111".U,
      io.stallReqFromFetch   -> "b000011".U
    )
  )

}
