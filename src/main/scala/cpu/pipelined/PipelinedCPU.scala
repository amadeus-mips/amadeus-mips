package cpu.pipelined

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.components.BaseCPU

class PipelinedCPU(implicit val conf: CPUConfig) extends BaseCPU {
  class IFIDBundle extends Bundle {
    val instruction = UInt(32.W)
  }

}
