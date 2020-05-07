package cpu.core.decode

import chisel3._
import cpu.CPUConfig
import cpu.core.Constants._

class BranchPredictIO extends Bundle {
  val pc = Input(UInt(addrLen.W))
  val update = Input(Bool())
  val taken = Input(Bool())

  val prediction = Output(Bool())
}

abstract class BranchPredict(val c: CPUConfig) extends Module{
  val io = IO(new BranchPredictIO)

//  val defaultCounter =
}

class AlwaysTakenPredictor(implicit val conf: CPUConfig) extends BranchPredict(conf) {
  io.prediction := true.B
}

class AlwaysNotTakenPredictor(implicit val conf: CPUConfig) extends BranchPredict(conf) {
  io.prediction := false.B
}