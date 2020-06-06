// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import cpu.CPUConfig
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import cpu.core.Constants._
import cpu.core.bundles.stages.{ExeMemBundle, MemWbBundle}
import cpu.core.bundles.{CPBundle, TLBOpIO}
import cpu.core.components.{ExceptionHandleBundle, TLBHandleBundle}

class MemoryTop(implicit cfg: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val in = Input(new ExeMemBundle)

    val exceptionCP0 = Input(new ExceptionHandleBundle)
    val tlbCP0       = Input(new TLBHandleBundle(cfg.tlbSize))
    val wbCP0        = Input(new CPBundle)

    /** load data from memory */
    val rData = new NiseSramReadIO

    /** save data to memory */
    val wData = new NiseSramWriteIO

    val tlb = new TLBOpIO(cfg.tlbSize)

    val out         = Output(new MemWbBundle)
    val badAddr     = Output(UInt(addrLen.W))
    val EPC         = Output(UInt(dataLen.W))
    val inDelaySlot = Output(Bool())
    val except      = Output(Vec(exceptAmount, Bool()))
    val stallReq    = Output(Bool())
  })

  val control = Module(new cpu.core.memory.Control)
  val except  = Module(new cpu.core.memory.Except)

  val forward = Module(new cpu.core.memory.Forward)

  control.io.inWriteData := io.in.write.data
  control.io.inMemData   := io.in.memData
  control.io.operation   := io.in.operation
  control.io.addr        := io.in.memAddr
  control.io.except      := except.io.outExcept.asUInt().orR()

  control.io.rData <> io.rData
  control.io.wData <> io.wData

  except.io.pc        := io.in.pc
  except.io.addr      := io.in.memAddr
  except.io.cp0Status := forward.io.outExceptionCP0.status
  except.io.cp0Cause  := forward.io.outExceptionCP0.cause
  except.io.inExcept  := io.in.except

  forward.io.inExceptionCP0 := io.exceptionCP0
  forward.io.inTLBCP0       := io.tlbCP0
  forward.io.wbCP0          := io.wbCP0

  io.out.addrL2     := io.in.memAddr(1, 0)
  io.out.operation  := io.in.operation
  io.out.tlb        := io.tlb
  io.out.write      := io.in.write
  io.out.write.data := control.io.outWriteData
  //  io.out.write.valid := !control.io.stallReq
  io.out.cp0  := io.in.cp0
  io.out.hilo := io.in.hilo
  io.out.pc   := io.in.pc

  io.badAddr     := except.io.badAddr
  io.inDelaySlot := io.in.inDelaySlot
  io.except      := except.io.outExcept
  io.EPC         := forward.io.outExceptionCP0.EPC
  io.stallReq    := control.io.stallReq

  io.tlb.asid          := forward.io.outTLBCP0.entryHi.asid
  io.tlb.kseg0Uncached := false.B

  io.tlb.instrReq.writeEn := io.in.operation === TLB_WR || io.in.operation === TLB_WI
  io.tlb.instrReq.TLBIndex := Mux(
    io.in.operation === TLB_WI,
    forward.io.outTLBCP0.index.index,
    forward.io.outTLBCP0.random.random
  )

  io.tlb.instrReq.writeData.global       := forward.io.outTLBCP0.entryLo0.g & forward.io.outTLBCP0.entryLo1.g
  io.tlb.instrReq.writeData.asid         := forward.io.outTLBCP0.entryHi.asid
  io.tlb.instrReq.writeData.vpn2         := forward.io.outTLBCP0.entryHi.vpn2
  io.tlb.instrReq.writeData.pages(0).pfn := forward.io.outTLBCP0.entryLo0.pfn
  io.tlb.instrReq.writeData.pages(1).pfn := forward.io.outTLBCP0.entryLo1.pfn

  io.tlb.probeReq := forward.io.outTLBCP0.entryHi.vpn2
}
