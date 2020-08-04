// See README.md for license details.

package cpu.core

import chisel3._
import chisel3.util.Decoupled
import cpu.CPUConfig
import cpu.common.MemReqBundle
import cpu.core.Constants._
import cpu.core.bundles.stages._
import cpu.core.bundles.{InstructionFIFOEntry, TLBOpIO}
import cpu.core.components._
import cpu.core.pipeline._

class InstFetchIO(implicit conf: CPUConfig) extends Bundle {
  val addr   = Decoupled(UInt(addrLen.W))
  val data   = Flipped(Decoupled(Vec(conf.fetchAmount, UInt(dataLen.W))))
  val change = Output(Bool())

  override def cloneType: InstFetchIO.this.type = new InstFetchIO().asInstanceOf[this.type]
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

    val rInst            = new InstFetchIO
    val memAccess        = new MemAccessIO
    val iCacheInvalidate = Decoupled(UInt(conf.iCacheConf.indexLen.W))
    val dCacheInvalidate = Decoupled(UInt(0.W))
    val tlb              = new TLBOpIO(conf.tlbSize)
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
  val instFIFO  = Module(new InstructionFIFO(new InstructionFIFOEntry()))
  val id_exe    = Module(new Stage(1, new IdExeBundle))
  val exe_mem   = Module(new Stage(2, new ExeMemBundle))
  val mem0_mem1 = Module(new Stage(3, new Mem0Mem1Bundle))
  val mem1_mem2 = Module(new Stage(4, new Mem1Mem2Bundle))
  val mem2_wb   = Module(new Stage(5, new Mem2WbBundle))

  fetchTop.io.stall   := hazard.io.frontend.stall(0)
  fetchTop.io.flush   := hazard.io.flushAll
  fetchTop.io.flushPC := hazard.io.flushPC

  fetchTop.io.predUpdate.bits  := executeTop.io.predUpdate
  fetchTop.io.predUpdate.valid := hazard.io.predictUpdate

  fetchTop.io.predict       := fetch1Top.io.predict
  fetchTop.io.predictWithDS := fetch1Top.io.predictWithDS
  fetchTop.io.branch.bits   := executeTop.io.branch.bits
  fetchTop.io.branch.valid  := hazard.io.branchValid
  fetchTop.io.inDelaySlot   := fetch1Top.io.nextInstInDelaySlot

  fetchTop.io.ramReady := io.rInst.addr.ready

  fetchTop.io.tlbExcept.refill  := io.tlb.except.inst.refill
  fetchTop.io.tlbExcept.invalid := io.tlb.except.inst.invalid

  if_if1.io.in    := fetchTop.io.out
  if_if1.io.stall := hazard.io.frontend.stall
  if_if1.io.flush := hazard.io.flushAll || hazard.io.frontend.flushIf1

  fetch1Top.io.in         := if_if1.io.out
  fetch1Top.io.fifoReady  := instFIFO.io.readyForEnqueue
  fetch1Top.io.flushFIFO  := hazard.io.fifo.flush
  fetch1Top.io.inst.bits  := io.rInst.data.bits
  fetch1Top.io.inst.valid := io.rInst.data.valid

  instFIFO.reset                 := reset.asBool() || hazard.io.flushAll || hazard.io.fifo.flush
  instFIFO.io.flushTail          := false.B
  instFIFO.io.enqueue            := fetch1Top.io.out
  instFIFO.io.dequeue.head.ready := hazard.io.fifo.deqReady

  decodeTop.io.in      := instFIFO.io.dequeue.head.bits
  decodeTop.io.inValid := instFIFO.io.dequeue.head.valid

  id_exe.io.in    := decodeTop.io.out
  id_exe.io.stall := hazard.io.backend.stall
  id_exe.io.flush := hazard.io.flushAll

  executeTop.io.in          := id_exe.io.out
  executeTop.io.flush       := hazard.io.flushAll
  executeTop.io.decodeValid := instFIFO.io.dequeue.head.valid
  // hio forward
  executeTop.io.rawHILO  := hilo.io.out
  executeTop.io.mem0HILO := exe_mem.io.out.hilo
  executeTop.io.mem1HILO := mem0_mem1.io.out.hiloWrite
  // cp0 forward
  executeTop.io.cp0Data := cp0.io.data
  executeTop.io.mem0CP0 := exe_mem.io.out.cp0
  executeTop.io.mem1CP0 := mem0_mem1.io.out.cp0Write
  // op forward
  executeTop.io.mem0Op := exe_mem.io.out.operation
  executeTop.io.mem1Op := mem2_wb.io.out.op
  // wr forward
  executeTop.io.mem0WR := exe_mem.io.out.write
  executeTop.io.mem1WR := mem0_mem1.io.out.write
  executeTop.io.mem2WR := mem1_mem2.io.out.write
  executeTop.io.wbWR   := mem2_wb.io.out.write
  // regfile data
  executeTop.io.rsData := regFile.io.rsData
  executeTop.io.rtData := regFile.io.rtData

  regFile.io.write := mem2_wb.io.out.write
  regFile.io.rs    := id_exe.io.out.rs.bits
  regFile.io.rt    := id_exe.io.out.rt.bits

  exe_mem.io.in    := executeTop.io.out
  exe_mem.io.stall := hazard.io.backend.stall
  exe_mem.io.flush := hazard.io.flushAll

  memory0Top.io.in           := exe_mem.io.out
  memory0Top.io.stalled      := exe_mem.io.stalled
  memory0Top.io.uncached     := io.memAccess.uncached
  memory0Top.io.tlbCP0       := cp0.io.tlbCP0
  memory0Top.io.exceptionCP0 := cp0.io.exceptionCP0
  memory0Top.io.mem1Except   := mem0_mem1.io.out.except.asUInt().orR()

  cp0.io.intr        := io.intr
  cp0.io.addr        := id_exe.io.out.imm26(15, 11)
  cp0.io.sel         := id_exe.io.out.imm26(2, 0)
  cp0.io.cp0Write    := mem0_mem1.io.out.cp0Write
  cp0.io.except      := mem0_mem1.io.out.except
  cp0.io.inDelaySlot := mem0_mem1.io.out.inDelaySlot
  cp0.io.pc          := mem0_mem1.io.out.pc
  cp0.io.badAddr     := mem0_mem1.io.out.badAddr

  cp0.io.op  := mem0_mem1.io.out.op
  cp0.io.tlb := mem0_mem1.io.out.tlbWrite

  hilo.io.in := mem0_mem1.io.out.hiloWrite

  hazard.io.except         := mem0_mem1.io.out.except
  hazard.io.exceptJumpAddr := memory1Top.io.exceptJumpAddr
  hazard.io.EPC            := cp0.io.exceptionCP0.EPC

  hazard.io.frontend.stallReqFromIf0 := fetchTop.io.stallReq
  hazard.io.frontend.stallReqFromIf1 := fetch1Top.io.stallReq
  hazard.io.backend.stallReqFromId   := decodeTop.io.stallReq
  hazard.io.backend.stallReqFromExe  := executeTop.io.stallReq
  hazard.io.backend.stallReqFromMem0 := memory0Top.io.stallReq
  hazard.io.backend.stallReqFromMem1 := memory1Top.io.stallReq

  hazard.io.predictFail          := executeTop.io.branch.valid
  hazard.io.backend.exeWaitingDS := executeTop.io.waitingDS
  hazard.io.backend.exeIsBranch  := executeTop.io.isBranch

  mem0_mem1.io.in    := memory0Top.io.out
  mem0_mem1.io.stall := hazard.io.backend.stall
  mem0_mem1.io.flush := hazard.io.flushAll

  memory1Top.io.in           := mem0_mem1.io.out
  memory1Top.io.commit       := io.memAccess.commit
  memory1Top.io.exceptionCP0 := cp0.io.exceptionCP0

  mem1_mem2.io.in    := memory1Top.io.out
  mem1_mem2.io.stall := hazard.io.backend.stall
  mem1_mem2.io.flush := false.B

  memory2Top.io.in           := mem1_mem2.io.out
  memory2Top.io.cachedData   := io.memAccess.cachedData
  memory2Top.io.uncachedData := io.memAccess.uncachedData

  mem2_wb.io.in    := memory2Top.io.out
  mem2_wb.io.stall := hazard.io.backend.stall
  mem2_wb.io.flush := false.B

  io.rInst.addr.bits  := fetchTop.io.out.pc
  io.rInst.addr.valid := fetchTop.io.pcValid
  io.rInst.data       <> fetch1Top.io.inst

  io.memAccess.request <> memory0Top.io.request
  io.tlb               <> memory0Top.io.tlb
  io.tlb.kseg0Uncached := cp0.io.kseg0Uncached

  io.iCacheInvalidate <> memory0Top.io.iCacheInvalidate
  io.dCacheInvalidate <> memory0Top.io.dCacheInvalidate

  io.rInst.change := fetchTop.io.pcChange
}
