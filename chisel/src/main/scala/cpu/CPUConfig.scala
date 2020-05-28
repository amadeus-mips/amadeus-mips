package cpu

import chisel3._

class CPUConfig(val build: Boolean) {

}

object CPUConfig{
  val Build = new CPUConfig(build = true)
}
