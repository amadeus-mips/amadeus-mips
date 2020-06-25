package cpu.core.fetch

import chisel3._
import chisel3.util.Cat
import cpu.CPUConfig
import cpu.core.Constants._
import shared.{Util, ValidBundle}

class PredictResBundle extends Bundle {
  val taken = Bool()
  val update = Bool()
}

class BranchDecode(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(dataLen.W))
    val pc   = Input(UInt(addrLen.W))
    val predictTaken = Input(Bool())
    val predictUpdate = Input(Bool())
    val predict = Output(new ValidBundle)
    val isBr = Output(Bool())
  })

  val isBr = isBranchInst(io.inst)
  val isJ  = io.inst === J || io.inst === JAL
  val isJR = io.inst === JR || io.inst === JALR
  val imm16 = io.inst(15, 0)
  val imm26 = io.inst(25, 0)

  val predictor = Module(new AlwaysTakenPredictor())

  val pcPlus4 = io.pc + 4.U
  val BTarget = pcPlus4 + Cat(Util.signedExtend(imm16, to = 30), 0.U(2.W))
  val JTarget = Cat(pcPlus4(31, 28), imm26, 0.U(2.W))

  predictor.io.pc := io.pc
  predictor.io.taken := io.predictTaken
  predictor.io.update := io.predictUpdate

  io.predict.valid := predictor.io.prediction && !isJR && isBr
  io.predict.bits := Mux(isJ, JTarget, BTarget)
  io.isBr := isBr
}
