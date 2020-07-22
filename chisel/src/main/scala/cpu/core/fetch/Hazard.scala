package cpu.core.fetch

import chisel3._
import shared.ValidBundle

class Hazard extends Module {
  val io = IO(new Bundle() {
    val flush = Input(Bool())
    val stall = Input(Bool())

    val in = Input(new Bundle() {
      val predict       = ValidBundle(32)
      val branch        = ValidBundle(32)
      val inDelaySlot   = Bool()
      val predictWithDS = Bool()
    })
    val out = Output(new Bundle() {
      val predict       = ValidBundle(32)
      val inDelaySlot   = Bool()
      val predictWithDS = Bool()
    })
  })

  val predictBuffer       = RegInit(0.U.asTypeOf(ValidBundle(32)))
  val predictWithDSBuffer = RegInit(false.B)

  when(io.flush || io.in.branch.valid) {
    predictBuffer.valid := false.B
  }.elsewhen(io.in.predict.valid && io.stall) {
      predictBuffer       := io.in.predict
      predictWithDSBuffer := io.in.predictWithDS
    }
    .elsewhen(!io.stall) {
      predictBuffer.valid := false.B
    }
  val predictValid = io.in.predict.valid || predictBuffer.valid

  val inDSBuffer = RegInit(false.B)
  when(io.flush || io.in.branch.valid) {
    inDSBuffer := false.B
  }.elsewhen(io.in.inDelaySlot && io.stall) {
      inDSBuffer := io.in.inDelaySlot
    }
    .elsewhen(!io.stall) {
      inDSBuffer := false.B
    }

  val inDelaySlot = io.in.inDelaySlot || inDSBuffer

  io.out.predict.valid := predictValid
  io.out.predict.bits  := Mux(io.in.predict.valid, io.in.predict.bits, predictBuffer.bits)
  io.out.predictWithDS := Mux(io.in.predict.valid, io.in.predictWithDS, predictWithDSBuffer)
  io.out.inDelaySlot   := inDelaySlot
}
