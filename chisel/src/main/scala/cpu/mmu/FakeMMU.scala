package cpu.mmu

import chisel3._
import cpu.CPUConfig

class FakeMMU(implicit conf: CPUConfig) extends Module {
  val io = IO(new MMUIO)
  io.out <> io.in
  io.dataUncached := false.B

  io.core := DontCare
}
