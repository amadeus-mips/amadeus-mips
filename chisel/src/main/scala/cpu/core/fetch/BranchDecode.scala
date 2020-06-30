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
  val predictor = Module(new TwoBitPredictor())

  predictor.io.pc := io.pc
  predictor.io.update := io.predUpdate

  io.predict.valid := predictor.io.prediction.valid
  io.predict.bits := predictor.io.prediction.bits
  io.isBr := isBr
}
