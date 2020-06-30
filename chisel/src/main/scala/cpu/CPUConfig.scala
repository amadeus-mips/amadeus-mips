package cpu

import chisel3._

class CPUConfig(val build: Boolean) {
  val branchPredictorAddrLen = 20
  val branchPredictorTableEntryNum = 64
}

object CPUConfig{
  val Build = new CPUConfig(build = true)
}
