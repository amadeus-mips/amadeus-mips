package cpu.core.fetch

import chisel3._
import cpu.CPUConfig
import cpu.core.Constants._

class BranchPredictorIO extends Bundle {
  val pc     = Input(UInt(addrLen.W))
  val update = Input(Bool())
  val taken  = Input(Bool())

  val prediction = Output(Bool())
}

abstract class BranchPredictor(val c: CPUConfig) extends Module {
  val io = IO(new BranchPredictorIO)

//  val defaultCounter =
}

abstract class StaticPredictor(c: CPUConfig) extends BranchPredictor(c) {}

abstract class DynamicPredictor(c: CPUConfig) extends BranchPredictor(c) {

}

class AlwaysTakenPredictor(implicit val conf: CPUConfig) extends StaticPredictor(conf) {
  io.prediction := true.B
}

class AlwaysNotTakenPredictor(implicit val conf: CPUConfig) extends StaticPredictor(conf) {
  io.prediction := false.B
}
