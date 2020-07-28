// See README.md for license details.

package cpu.core

import chisel3._
import chisel3.util.Decoupled
import cpu.CPUConfig
import cpu.common.MemReqBundle
import cpu.core.Constants._
import cpu.core.bundles.TLBOpIO
import cpu.core.bundles.stages._
import cpu.core.components.{CP0, HILO, RegFile, Stage}
import cpu.core.pipeline._

class InstFetchIO extends Bundle {
  val addr   = Decoupled(UInt(addrLen.W))
  val data   = Flipped(Decoupled(UInt(dataLen.W)))
  val change = Output(Bool())
}

class MemAccessIO extends Bundle {
  // mem0
  val request  = Decoupled(new MemReqBundle)
  val uncached = Input(Bool())
  // mem1
  val commit = Input(Bool())
  // mem2
  val cachedData   = Input(UInt(dataLen.W))
  val uncachedData = Input(UInt(dataLen.W))
}

class Core(implicit conf: CPUConfig) extends MultiIOModule {
  val io = IO(new Bundle {
    val intr = Input(UInt(intrLen.W))

    val rInst     = new InstFetchIO
    val memAccess = new MemAccessIO
    val tlb       = new TLBOpIO(conf.tlbSize)
  })

  /**
    * fetchTop | fetch1Top | decodeTop | executeTop | memoryTop | wb
    */
  val fetchTop   = Module(new FetchTop)
  val fetch1Top  = Module(new Fetch1Top)
  val decodeTop  = Module(new DecodeTop)
  val executeTop = Module(new ExecuteTop)
  val memory0Top = Module(new Memory0Top)
  val memory1Top = Module(new Memory1Top)
  val memory2Top = Module(new Memory2Top)

  val regFile = Module(new RegFile)
  val cp0     = Module(new CP0)
  val hilo    = Module(new HILO)
  val hazard  = Module(new Hazard)

  // stages
  val if_if1    = Module(new Stage(1, new IfIf1Bundle))
  val if1_id    = Module(new Stage(2, new If1IdBundle))
  val id_exe    = Module(new Stage(3, new IdExeBundle))
  val exe_mem   = Module(new Stage(4, new ExeMemBundle))
  val mem0_mem1 = Module(new Stage(5, new Mem0Mem1Bundle))
  val mem1_mem2 = Module(new Stage(6, new Mem1Mem2Bundle))
  val mem2_wb   = Module(new Stage(7, new Mem2WbBundle))

  fetchTop.io.stall   := hazard.io.stall(0)
  fetchTop.io.flush   := hazard.io.flush
  fetchTop.io.flushPC := hazard.io.flushPC
  fetchTop.io.lastDS  := hazard.io.lastDS

  fetchTop.io.predUpdate := executeTop.io.predUpdate

  fetchTop.io.predict     := fetch1Top.io.predict
  fetchTop.io.branch      := executeTop.io.branch
  fetchTop.io.inDelaySlot := fetch1Top.io.nextInstInDelaySlot

  fetchTop.io.instValid := io.rInst.addr.ready

  fetchTop.io.tlbExcept.refill  := io.tlb.except.inst.refill
  fetchTop.io.tlbExcept.invalid := io.tlb.except.inst.invalid

  if_if1.io.in    := fetchTop.io.out
  if_if1.io.stall := hazard.io.stall
  if_if1.io.flush := hazard.io.flush ||
    (executeTop.io.branch.valid && hazard.io.predictFailFlush(0))

  fetch1Top.io.in         := if_if1.io.out
  fetch1Top.io.itReady    := !VecInit(hazard.io.stallReq.tail.tail).asUInt().orR()
  fetch1Top.io.inst.bits  := io.rInst.data.bits
  fetch1Top.io.inst.valid := io.rInst.data.valid

  if1_id.io.in    := fetch1Top.io.out
  if1_id.io.stall := hazard.io.stall
  if1_id.io.flush := hazard.io.flush ||
    (executeTop.io.branch.valid && hazard.io.predictFailFlush(1))

  decodeTop.io.in     := if1_id.io.out
  decodeTop.io.exeWR  := executeTop.io.out.write
  decodeTop.io.mem0WR := memory0Top.io.out.write
  decodeTop.io.mem1WR := memory1Top.io.out.write
  decodeTop.io.mem2WR := memory2Top.io.out.write
  decodeTop.io.wbWR   := mem2_wb.io.out.write
  decodeTop.io.rsData := regFile.io.rsData
  decodeTop.io.rtData := regFile.io.rtData

  regFile.io.write := mem2_wb.io.out.write
  regFile.io.rs    := if1_id.io.out.inst(25, 21)
  regFile.io.rt    := if1_id.io.out.inst(20, 16)

  id_exe.io.in    := decodeTop.io.out
  id_exe.io.stall := hazard.io.stall
  id_exe.io.flush := hazard.io.flush

  executeTop.io.in       := id_exe.io.out
  executeTop.io.flush    := hazard.io.flush
  executeTop.io.rawHILO  := hilo.io.out
  executeTop.io.mem0HILO := exe_mem.io.out.hilo
  executeTop.io.cp0Data  := cp0.io.data
  executeTop.io.mem0CP0  := exe_mem.io.out.cp0
  executeTop.io.mem0Op   := exe_mem.io.out.operation

  exe_mem.io.in    := executeTop.io.out
  exe_mem.io.stall := hazard.io.stall
  exe_mem.io.flush := hazard.io.flush

  memory0Top.io.in           := exe_mem.io.out
  memory0Top.io.exceptionCP0 := cp0.io.exceptionCP0
  memory0Top.io.tlbCP0       := cp0.io.tlbCP0
  memory0Top.io.uncached     := io.memAccess.uncached

  cp0.io.intr        := io.intr
  cp0.io.cp0Write    := memory0Top.io.cp0Write
  cp0.io.addr        := id_exe.io.out.imm26(15, 11)
  cp0.io.sel         := id_exe.io.out.imm26(2, 0)
  cp0.io.except      := memory0Top.io.except
  cp0.io.inDelaySlot := exe_mem.io.out.inDelaySlot
  cp0.io.pc          := exe_mem.io.out.pc
  cp0.io.badAddr     := memory0Top.io.badAddr

  cp0.io.op  := memory0Top.io.out.op
  cp0.io.tlb := memory0Top.io.tlbWrite

  hilo.io.in := memory0Top.io.hiloWrite

  hazard.io.except         := memory0Top.io.except
  hazard.io.exceptJumpAddr := memory0Top.io.exceptJumpAddr
  hazard.io.EPC            := memory0Top.io.EPC
  hazard.io.stallReq(0)    := fetchTop.io.stallReq
  hazard.io.stallReq(1)    := fetch1Top.io.stallReq
  hazard.io.stallReq(2)    := decodeTop.io.stallReq
  hazard.io.stallReq(3)    := executeTop.io.stallReq
  hazard.io.stallReq(4)    := memory0Top.io.stallReq
  hazard.io.stallReq(5)    := memory1Top.io.stallReq

  hazard.io.delaySlots(0) := fetchTop.io.out.inDelaySlot
  hazard.io.delaySlots(1) := fetch1Top.io.out.inDelaySlot
  hazard.io.delaySlots(2) := decodeTop.io.out.inDelaySlot

  mem0_mem1.io.in    := memory0Top.io.out
  mem0_mem1.io.stall := hazard.io.stall
  mem0_mem1.io.flush := false.B

  memory1Top.io.in     := mem0_mem1.io.out
  memory1Top.io.commit := io.memAccess.commit

  mem1_mem2.io.in    := memory1Top.io.out
  mem1_mem2.io.stall := hazard.io.stall
  mem1_mem2.io.flush := false.B

  memory2Top.io.in           := mem1_mem2.io.out
  memory2Top.io.cachedData   := io.memAccess.cachedData
  memory2Top.io.uncachedData := io.memAccess.uncachedData

  mem2_wb.io.in    := memory2Top.io.out
  mem2_wb.io.stall := hazard.io.stall
  mem2_wb.io.flush := false.B

  io.rInst.addr.bits  := fetchTop.io.out.pc
  io.rInst.addr.valid := fetchTop.io.pcValid
  io.rInst.data       <> fetch1Top.io.inst

  io.memAccess.request <> memory0Top.io.request
  io.tlb               <> memory0Top.io.tlb
  io.tlb.kseg0Uncached := cp0.io.kseg0Uncached

  io.rInst.change := fetchTop.io.pcChange
}
