package cpu.mmu

import chisel3._
import chisel3.util._
import cpu.CPUConfig

class FakeMMU(implicit conf: CPUConfig) extends BaseMMU {
  io.out                 <> io.in
  io.dataUncached        := io.in.memReq.tag(19, 17) === "b101".U
  io.out.rInst.addr.bits := Cat(0.U(3.W), io.in.rInst.addr.bits(28, 0))
  io.out.memReq.tag      := Cat(0.U(3.W), io.in.memReq.tag(16, 0))

  io.core        := DontCare
  io.core.except := 0.U.asTypeOf(io.core.except)
}
