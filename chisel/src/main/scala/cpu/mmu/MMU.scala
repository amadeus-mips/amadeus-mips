package cpu.mmu

import chisel3._
import chisel3.util.{Cat, Decoupled}
import cpu.CPUConfig
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import cpu.core.InstFetchIO
import cpu.core.bundles.TLBOpIO

class MMU(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    class TempIO extends Bundle {
      val rInst = new InstFetchIO
      val rChannel = new NiseSramReadIO
      val wChannel = new NiseSramWriteIO
    }
    val in           = Flipped(new TempIO)
    val out          = new TempIO
    val dataUncached = Output(Bool())
    val core         = Flipped(new TLBOpIO(conf.tlbSize))
  })
  assert(io.in.rChannel.addr === io.in.wChannel.addr)

  val tlb = Module(new FullTLB(numOfReadPorts = 2, TLBSize = conf.tlbSize))

  tlb.io.asid          := io.core.asid
  tlb.io.kseg0Uncached := io.core.kseg0Uncached
  tlb.io.instrReq      := io.core.instrReq
  tlb.io.probeReq      := io.core.probeReq

  tlb.io.query(0).vAddr := io.in.rInst.addr.bits(31, 12)
  tlb.io.query(1).vAddr := io.in.rChannel.addr(31, 12)

  io.core.readResp  := tlb.io.readResp
  io.core.probeResp := tlb.io.probeResp

  io.out <> io.in

  io.out.rInst.addr.bits := Cat(0.U(3.W), tlb.io.result(0).pageInfo.pfn(16, 0), io.in.rInst.addr.bits(11, 0))
  io.out.rInst.addr.valid := io.in.rInst.addr.valid &&
    (!tlb.io.result(0).mapped || tlb.io.result(0).hit && tlb.io.result(0).pageInfo.valid)

  io.out.rChannel.addr := Cat(0.U(3.W), tlb.io.result(1).pageInfo.pfn(16, 0), io.in.rChannel.addr(11, 0))
//  io.out.rChannel.enable := io.in.rChannel.enable &&
//    (!tlb.io.result(1).mapped || tlb.io.result(1).hit && tlb.io.result(1).pageInfo.valid)

  io.out.wChannel.addr := Cat(0.U(3.W), tlb.io.result(1).pageInfo.pfn(16, 0), io.in.wChannel.addr(11, 0))
//  io.out.wChannel.enable := io.in.wChannel.enable &&
//    (!tlb.io.result(1).mapped ||
//      tlb.io.result(1).hit && tlb.io.result(1).pageInfo.valid && tlb.io.result(1).pageInfo.dirty)

  io.dataUncached := tlb.io.result(1).uncached

  io.core.except.inst.refill  := tlb.io.result(0).mapped && !tlb.io.result(0).hit
  io.core.except.inst.invalid := tlb.io.result(0).mapped && tlb.io.result(0).hit && !tlb.io.result(0).pageInfo.valid

  io.core.except.data.refill  := tlb.io.result(1).mapped && !tlb.io.result(1).hit
  io.core.except.data.invalid := tlb.io.result(1).mapped && tlb.io.result(1).hit && !tlb.io.result(1).pageInfo.valid
  io.core.except.data.modified := tlb.io.result(1).mapped &&
    tlb.io.result(1).hit && tlb.io.result(1).pageInfo.valid && !tlb.io.result(1).pageInfo.dirty
}
