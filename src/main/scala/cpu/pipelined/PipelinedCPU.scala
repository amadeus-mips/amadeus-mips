package cpu.pipelined

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.components.BaseCPU

class PipelinedCPU(implicit val conf: CPUConfig) extends BaseCPU {
  // instruction fetch fetches memory from memory[PC]
  // io bundle from instruction fetch to instruction decode
  class IFIDIO extends Bundle {
    val instruction = UInt(32.W)
    val pc = UInt(32.W)
    val pcPlusFour = UInt(32.W)
  }

  // decode read from reg file
  // io bundle from instruction decode to execute
  class IDEXBundle extends Bundle {
    // from the fetch stage pass through
    val pc = UInt(32.W)
    val pcPlusFour = UInt(32.W)

    // the values from RS and RT
    val valRs = UInt(32.W)
    val valRt = UInt(32.W)
  }

}
