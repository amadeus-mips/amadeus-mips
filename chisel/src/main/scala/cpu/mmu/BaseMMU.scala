package cpu.mmu

import chisel3._
import cpu.CPUConfig

class BaseMMU(implicit conf: CPUConfig) extends Module{
   val io = IO(new MMUIO)
}
