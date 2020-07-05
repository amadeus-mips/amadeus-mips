package cpu.core.pipeline

import chisel3._
import chisel3.util.Decoupled
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.stages.{If1IdBundle, IfIf1Bundle}
import shared.ValidBundle

class Fetch1Top(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle() {
    val in      = Input(new IfIf1Bundle)
    val itReady = Input(Bool())
    val inst    = Flipped(Decoupled(UInt(dataLen.W)))

    val out = Output(new If1IdBundle)

    val stallReq = Output(Bool())

    val predict = Output(new ValidBundle) // to Fetch

    val nextInstInDelaySlot = Output(Bool()) // to Fetch
  })

  val inst = Mux(io.in.except.asUInt().orR() || !io.in.instValid, 0.U, io.inst.bits)

  io.predict := io.in.brPredict

  io.out.pc          := io.in.pc
  io.out.inDelaySlot := io.in.inDelaySlot
  io.out.except      := io.in.except
  io.out.instValid   := io.in.instValid
  io.out.inst        := inst
  io.out.brPredict   := io.in.brPredict

  io.inst.ready := io.itReady

  io.stallReq := io.in.instValid && !io.inst.valid

  io.nextInstInDelaySlot := isBranchInst(inst) && io.inst.fire()
}
