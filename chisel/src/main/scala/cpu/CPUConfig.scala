package cpu

import chisel3._
import cpu.common.WriteMask

class CPUConfig(val build: Boolean, val memoryFile: String = "", val tlbSize: Int = 32) {
  WriteMask.tlbSize = tlbSize

  val branchPredictorAddrLen = 20
  val branchPredictorTableEntryNum = 64
  require(build && memoryFile.isEmpty || !build && !memoryFile.isEmpty)
}

object CPUConfig{
  val Build = new CPUConfig(build = true)
}
