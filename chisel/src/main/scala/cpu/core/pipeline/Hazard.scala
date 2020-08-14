// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._

class Hazard extends Module {
  val io = IO(new Bundle {
    val except         = Input(Vec(exceptAmount, Bool()))
    val exceptJumpAddr = Input(UInt(addrLen.W))
    val EPC            = Input(UInt(dataLen.W))

    val flushAll = Output(Bool())
    val flushPC  = Output(UInt(dataLen.W))

    val frontend = new Bundle {
      val stallReqFromIf0 = Input(Bool())
      val stallReqFromIf1 = Input(Bool())
      val stall           = Output(UInt(cpuStallLen.W))
    }
    val backend = new Bundle {
      val stallReqFromId   = Input(Bool())
      val stallReqFromExe  = Input(Bool())
      val stallReqFromMem0 = Input(Bool())
      val stallReqFromMem1 = Input(Bool())

      val stall = Output(UInt(cpuStallLen.W))
    }
  })

  val hasExcept = io.except.asUInt().orR()

  io.flushAll := hasExcept
  io.flushPC  := Mux(io.except(EXCEPT_ERET), io.EPC, io.exceptJumpAddr)

  io.frontend.stall := MuxCase(
    0.U,
    Seq(
      io.frontend.stallReqFromIf1 -> "b111".U,
      io.frontend.stallReqFromIf0 -> "b11".U
    )
  )

  io.backend.stall := MuxCase(
    0.U,
    Seq(
      io.backend.stallReqFromMem1 -> "b11111".U,
      io.backend.stallReqFromMem0 -> "b1111".U,
      io.backend.stallReqFromExe  -> "b111".U,
      io.backend.stallReqFromId   -> "b11".U
    )
  )

}
