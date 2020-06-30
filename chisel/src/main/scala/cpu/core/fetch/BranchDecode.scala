package cpu.core.fetch

import chisel3._
import chisel3.util.{Cat, ValidIO}
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
    val predUpdate = Flipped(ValidIO(new BrPrUpdateBundle))
    val predict = Output(new ValidBundle)
    val isBr = Output(Bool())
  })

  val isBr = isBranchInst(io.inst)
  val isJ  = io.inst === J || io.inst === JAL
  val isJR = io.inst === JR || io.inst === JALR
  val imm16 = io.inst(15, 0)
  val imm26 = io.inst(25, 0)

  val predictor = Module(new TwoBitPredictor())

  val pcPlus4 = io.pc + 4.U
  val BTarget = pcPlus4 + Cat(Util.signedExtend(imm16, to = 30), 0.U(2.W))
  val JTarget = Cat(pcPlus4(31, 28), imm26, 0.U(2.W))

  predictor.io.pc := io.pc
  predictor.io.update := io.predUpdate

  io.predict.valid := predictor.io.prediction.valid
  io.predict.bits := predictor.io.prediction.bits
  io.isBr := isBr
}
