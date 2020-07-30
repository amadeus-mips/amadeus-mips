// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.Decoupled
import cpu.CPUConfig
import cpu.common.MemReqBundle
import cpu.core.Constants._
import cpu.core.bundles.TLBOpIO
import cpu.core.bundles.stages.{ExeMemBundle, Mem0Mem1Bundle}
import cpu.core.components.{ExceptionHandleBundle, TLBHandleBundle}

class Memory0Top(implicit cfg: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val in = Input(new ExeMemBundle)

    val exceptionCP0 = Input(new ExceptionHandleBundle)
    val tlbCP0       = Input(new TLBHandleBundle(cfg.tlbSize))

    val mem1Except = Input(Bool())

    // with ram
    val request  = Decoupled(new MemReqBundle)
    val uncached = Input(Bool())

    val iCacheInvalidate = Decoupled(UInt(cfg.iCacheConf.indexLen.W))

    val dCacheInvalidate = Decoupled(UInt(0.W))

    // with mmu
    val tlb = new TLBOpIO(cfg.tlbSize)

    val out      = Output(new Mem0Mem1Bundle)
    val stallReq = Output(Bool())
  })

  val control = Module(new cpu.core.memory.Control)
  val except  = Module(new cpu.core.memory.Except)

  val hasExcept = except.io.outExcept.asUInt().orR()

  control.io.inMemData := io.in.memData
  control.io.operation := io.in.operation
  control.io.addr      := io.in.memAddr
  control.io.except    := hasExcept || io.mem1Except

  except.io.pc        := io.in.pc
  except.io.addr      := io.in.memAddr
  except.io.instValid := io.in.instValid
  except.io.op        := io.in.operation
  except.io.cp0Status := io.exceptionCP0.status
  except.io.cp0Cause  := io.exceptionCP0.cause
  except.io.inExcept  := io.in.except

  except.io.tlbExcept.refill   := io.tlb.except.data.refill
  except.io.tlbExcept.invalid  := io.tlb.except.data.invalid
  except.io.tlbExcept.modified := io.tlb.except.data.modified

  io.out.addrL2      := io.in.memAddr(1, 0)
  io.out.op          := io.in.operation
  io.out.write       := io.in.write
  io.out.write.enable := io.in.write.enable && !hasExcept
  io.out.pc          := io.in.pc
  io.out.uncached    := io.uncached
  io.out.inDelaySlot := io.in.inDelaySlot
  io.out.except      := except.io.outExcept
  io.out.badAddr     := except.io.badAddr
  io.out.tlbWrite    := io.tlb
  io.out.cp0Write    := io.in.cp0
  io.out.cp0Write.valid := io.in.cp0.valid && !hasExcept
  io.out.hiloWrite   := io.in.hilo
  io.out.hiloWrite.lo.valid := io.in.hilo.lo.valid && !hasExcept
  io.out.hiloWrite.hi.valid := io.in.hilo.hi.valid && !hasExcept

  io.stallReq := control.io.stallReq || (io.dCacheInvalidate.valid && !io.dCacheInvalidate.ready) || (io.iCacheInvalidate.valid && !io.iCacheInvalidate.ready)

  io.request <> control.io.request

  io.dCacheInvalidate.bits := DontCare
  io.dCacheInvalidate.valid := io.in.cacheOp.target === TARGET_D && io.in.cacheOp.valid && !hasExcept && !io.mem1Except && io.in.instValid

  val indexFrom = cfg.iCacheConf.indexLen + cfg.iCacheConf.bankIndexLen + cfg.iCacheConf.bankOffsetLen - 1
  val indexTo = cfg.iCacheConf.bankIndexLen + cfg.iCacheConf.bankOffsetLen
  io.iCacheInvalidate.bits := io.in.memAddr(indexFrom, indexTo)
  io.iCacheInvalidate.valid := io.in.cacheOp.target === TARGET_I && io.in.cacheOp.valid && !hasExcept && !io.mem1Except && io.in.instValid

  io.tlb.asid          := io.tlbCP0.entryHi.asid
  io.tlb.kseg0Uncached := false.B

  io.tlb.instrReq.writeEn := (io.in.operation === TLB_WR || io.in.operation === TLB_WI) && !hasExcept && !io.mem1Except
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
