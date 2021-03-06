package cpu.mmu

import chisel3._
import chisel3.util.Cat
import cpu.CPUConfig

class MMU(implicit conf: CPUConfig) extends BaseMMU {
  val tlb = Module(if(conf.useMemTLB) new MemTLB(numOfReadPorts = 2, tlbSize = conf.tlbSize) else new FullTLB(numOfReadPorts = 2, tlbSize = conf.tlbSize))

  tlb.io.asid          := io.core.asid
  tlb.io.kseg0Uncached := io.core.kseg0Uncached
  tlb.io.instrReq      := io.core.instrReq
  tlb.io.probeReq      := io.core.probeReq

  tlb.io.query(0).vAddr := io.in.rInst.addr.bits(31, 12)
  tlb.io.query(1).vAddr := io.in.memReq.tag

  io.core.readResp  := tlb.io.readResp
  io.core.probeResp := tlb.io.probeResp

  io.out <> io.in

  io.out.rInst.addr.bits := Cat(0.U(3.W), tlb.io.result(0).pageInfo.pfn(16, 0), io.in.rInst.addr.bits(11, 0))
  io.out.rInst.addr.valid := io.in.rInst.addr.valid &&
    (!tlb.io.result(0).mapped || tlb.io.result(0).hit && tlb.io.result(0).pageInfo.valid)

  io.out.memReq     := io.in.memReq
  io.out.memReq.tag := Cat(0.U(3.W), tlb.io.result(1).pageInfo.pfn(16, 0))

  io.dataUncached := tlb.io.result(1).uncached

  io.core.except.inst.refill  := tlb.io.result(0).mapped && !tlb.io.result(0).hit
  io.core.except.inst.invalid := tlb.io.result(0).mapped && tlb.io.result(0).hit && !tlb.io.result(0).pageInfo.valid

  io.core.except.data.refill  := tlb.io.result(1).mapped && !tlb.io.result(1).hit
  io.core.except.data.invalid := tlb.io.result(1).mapped && tlb.io.result(1).hit && !tlb.io.result(1).pageInfo.valid
  io.core.except.data.modified := tlb.io.result(1).mapped &&
    tlb.io.result(1).hit && tlb.io.result(1).pageInfo.valid && !tlb.io.result(1).pageInfo.dirty
}
