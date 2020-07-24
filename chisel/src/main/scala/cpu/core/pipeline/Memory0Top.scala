// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.Decoupled
import cpu.CPUConfig
import cpu.common.MemReqBundle
import cpu.core.Constants._
import cpu.core.bundles.stages.{ExeMemBundle, Mem0Mem1Bundle}
import cpu.core.bundles.{CPBundle, HILOValidBundle, TLBOpIO, TLBReadBundle}
import cpu.core.components.{ExceptionHandleBundle, TLBHandleBundle}

class Memory0Top(implicit cfg: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val in = Input(new ExeMemBundle)

    val exceptionCP0 = Input(new ExceptionHandleBundle)
    val tlbCP0       = Input(new TLBHandleBundle(cfg.tlbSize))

    // with ram
    val request  = Decoupled(new MemReqBundle)
    val uncached = Input(Bool())

    // with mmu
    val tlb = new TLBOpIO(cfg.tlbSize)

    val out         = Output(new Mem0Mem1Bundle)
    val badAddr     = Output(UInt(addrLen.W))
    val EPC         = Output(UInt(dataLen.W))
    val inDelaySlot = Output(Bool())
    val except      = Output(Vec(exceptAmount, Bool()))

    val tlbWrite  = Output(new TLBReadBundle)
    val cp0Write  = Output(new CPBundle)
    val hiloWrite = Output(new HILOValidBundle)
    val stallReq  = Output(Bool())
  })

  val control = Module(new cpu.core.memory.Control)
  val except  = Module(new cpu.core.memory.Except)

  control.io.inMemData := io.in.memData
  control.io.operation := io.in.operation
  control.io.addr      := io.in.memAddr
  control.io.except    := except.io.outExcept.asUInt().orR()

  except.io.pc        := io.in.pc
  except.io.addr      := io.in.memAddr
  except.io.instValid := io.in.instValid
  except.io.op        := io.in.operation
  except.io.cp0Status := io.exceptionCP0.status.asUInt()
  except.io.cp0Cause  := io.exceptionCP0.cause.asUInt()
  except.io.inExcept  := io.in.except

  except.io.tlbExcept.refill   := io.tlb.except.data.refill
  except.io.tlbExcept.invalid  := io.tlb.except.data.invalid
  except.io.tlbExcept.modified := io.tlb.except.data.modified

  io.out.addrL2   := io.in.memAddr(1, 0)
  io.out.op       := io.in.operation
  io.out.write    := io.in.write
  io.out.pc       := io.in.pc
  io.out.uncached := io.uncached
  io.out.valid    := !except.io.outExcept.asUInt().orR()

  io.tlbWrite  := io.tlb
  io.cp0Write  := io.in.cp0
  io.hiloWrite := io.in.hilo

  io.badAddr     := except.io.badAddr
  io.inDelaySlot := io.in.inDelaySlot
  io.except      := except.io.outExcept
  io.EPC         := io.exceptionCP0.EPC
  io.stallReq    := control.io.stallReq

  io.request <> control.io.request

  io.tlb.asid          := io.tlbCP0.entryHi.asid
  io.tlb.kseg0Uncached := false.B

  io.tlb.instrReq.writeEn := io.in.operation === TLB_WR || io.in.operation === TLB_WI
  io.tlb.instrReq.TLBIndex := Mux(
    io.in.operation === TLB_WR,
    io.tlbCP0.random.random,
    io.tlbCP0.index.index
  )

  io.tlb.instrReq.writeData.global   := io.tlbCP0.entryLo0.global & io.tlbCP0.entryLo1.global
  io.tlb.instrReq.writeData.asid     := io.tlbCP0.entryHi.asid
  io.tlb.instrReq.writeData.vpn2     := io.tlbCP0.entryHi.vpn2
  io.tlb.instrReq.writeData.pages(0) := io.tlbCP0.entryLo0
  io.tlb.instrReq.writeData.pages(1) := io.tlbCP0.entryLo1

  io.tlb.probeReq := io.tlbCP0.entryHi.vpn2
}
