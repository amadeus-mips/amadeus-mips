package cpu.core.fetch

import chisel3._
import shared.ValidBundle

class Hazard extends Module {
  val io = IO(new Bundle() {
    val flush = Input(Bool())
    val stall = Input(Bool())

    val in = Input(new Bundle() {
      val predict     = new ValidBundle()
      val branch      = new ValidBundle()
      val inDelaySlot = Bool()
    })
    val out = Flipped(in)
  })

  val predictBuffer = RegInit(0.U.asTypeOf(new ValidBundle()))
  when(io.flush) {
    predictBuffer.valid := false.B
  }.elsewhen(io.in.predict.valid && io.stall) {
      predictBuffer := io.in.predict
    }
    .elsewhen(!io.stall) {
      predictBuffer.valid := false.B
    }
  def predictValid = io.in.predict.valid || predictBuffer.valid

  val inDSBuffer = RegInit(false.B)
  when(io.in.inDelaySlot && io.stall) {
    inDSBuffer := io.in.inDelaySlot
  }.elsewhen(!io.stall) {
    inDSBuffer := false.B
  }

  def inDelaySlot = io.in.inDelaySlot || inDSBuffer

  val branchBuffer = RegInit(0.U.asTypeOf(new ValidBundle()))
  when(io.flush) {
    branchBuffer.valid := false.B
  }.elsewhen(io.stall && inDelaySlot && io.in.branch.valid) {
      branchBuffer <> io.in.branch
    }
    .elsewhen(!io.stall) {
      branchBuffer.valid := false.B
    }

  def branchValid = io.in.branch.valid || branchBuffer.valid

  io.out.predict.valid := predictValid
  io.out.predict.bits  := Mux(io.in.predict.valid, io.in.predict.bits, predictBuffer.bits)
  io.out.branch.valid  := (!inDelaySlot && io.in.branch.valid) || (!io.stall && branchValid)
  io.out.branch.bits   := Mux(io.in.branch.valid, io.in.branch.bits, branchBuffer.bits)
  io.out.inDelaySlot   := inDelaySlot
}
