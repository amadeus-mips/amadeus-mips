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

    val predictFail = Input(Bool())

    val flushAll = Output(Bool())
    val flushPC  = Output(UInt(dataLen.W))

    val branchValid   = Output(Bool())
    val predictUpdate = Output(Bool())

    val frontend = new Bundle {
      val stallReqFromIf0 = Input(Bool())
      val stallReqFromIf1 = Input(Bool())
      val stall           = Output(UInt(cpuStallLen.W))
      val flushIf1        = Output(Bool())
    }
    val fifo = new Bundle {
      val deqReady = Output(Bool())
      val flush    = Output(Bool())
    }
    val backend = new Bundle {
      val stallReqFromId   = Input(Bool())
      val stallReqFromExe  = Input(Bool())
      val stallReqFromMem0 = Input(Bool())
      val stallReqFromMem1 = Input(Bool())

      val exeIsBranch  = Input(Bool())
      val exeWaitingDS = Input(Bool())

      val stall = Output(UInt(cpuStallLen.W))
    }
  })

  val hasExcept = io.except.asUInt().orR()

  io.flushAll := hasExcept
  io.flushPC  := Mux(io.except(EXCEPT_ERET), io.EPC, io.exceptJumpAddr)

  val branchValid = !io.backend.stall(1) && io.predictFail

  io.frontend.stall := MuxCase(
    0.U,
    Seq(
      io.frontend.stallReqFromIf1 -> "b111".U,
      io.frontend.stallReqFromIf0 -> "b11".U
    )
  )
  io.frontend.flushIf1 := branchValid

  io.fifo.flush    := branchValid
  io.fifo.deqReady := !io.backend.stall(0)

  io.backend.stall := MuxCase(
    0.U,
    Seq(
      io.backend.stallReqFromMem1 -> "b11111".U,
      io.backend.stallReqFromMem0 -> "b1111".U,
      io.backend.stallReqFromExe  -> "b111".U,
      io.backend.exeWaitingDS     -> "b110".U,
      io.backend.stallReqFromId   -> Mux(io.backend.exeIsBranch, "b111".U, "b11".U)
    )
  )

  assert(!(io.backend.exeWaitingDS && io.backend.stallReqFromId))

  io.branchValid   := branchValid
  io.predictUpdate := !io.backend.stall(1) && io.backend.exeIsBranch
}
