package cpu

import chisel3._
import cpu.common.WriteMask

class CPUConfig(val build: Boolean, val tlbSize: Int = 32) {
  WriteMask.tlbSize = tlbSize
}

object CPUConfig{
  val Build = new CPUConfig(build = true)
}
