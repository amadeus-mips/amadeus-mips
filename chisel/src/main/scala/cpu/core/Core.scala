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
  val id_exe    = Module(new Stage(1, Vec(conf.decodeWidth, new IdExeBundle)))
  val exe_mem   = Module(new Stage(2, Vec(conf.decodeWidth, new ExeMemBundle)))
  val mem0_mem1 = Module(new Stage(3, Vec(conf.decodeWidth, new Mem0Mem1Bundle)))
  val mem1_mem2 = Module(new Stage(4, Vec(conf.decodeWidth, new Mem1Mem2Bundle)))
  val mem2_wb   = Module(new Stage(5, Vec(conf.decodeWidth, new Mem2WbBundle)))

  fetchTop.io.stall   := hazard.io.frontend.stall(0)
  fetchTop.io.flush   := hazard.io.flushAll
  fetchTop.io.flushPC := hazard.io.flushPC

  fetchTop.io.predict       := fetch1Top.io.predict
  fetchTop.io.predictWithDS := fetch1Top.io.predictWithDS
  fetchTop.io.branch        := executeTop.io.branch
  fetchTop.io.predUpdate    := executeTop.io.predUpdate
  fetchTop.io.inDelaySlot   := fetch1Top.io.nextInstInDelaySlot

  fetchTop.io.ramReady := io.rInst.addr.ready

  fetchTop.io.tlbExcept.refill  := io.tlb.except.inst.refill
  fetchTop.io.tlbExcept.invalid := io.tlb.except.inst.invalid

  if_if1.io.in    := fetchTop.io.out
  if_if1.io.stall := hazard.io.frontend.stall
  if_if1.io.flush := hazard.io.flushAll || executeTop.io.branch.valid

  fetch1Top.io.in         := if_if1.io.out
  fetch1Top.io.fifoReady  := instFIFO.io.readyForEnqueue
  fetch1Top.io.flushFIFO  := executeTop.io.branch.valid
  fetch1Top.io.inst.bits  := io.rInst.data.bits
  fetch1Top.io.inst.valid := io.rInst.data.valid

  instFIFO.reset        := reset.asBool() || hazard.io.flushAll || executeTop.io.branch.valid
  instFIFO.io.flushTail := false.B
  instFIFO.io.enqueue   := fetch1Top.io.out

  decodeTop.io.ins <> instFIFO.io.dequeue
  when(hazard.io.flushAll || executeTop.io.branch.valid) {
    decodeTop.io.ins.foreach(_.valid := false.B)
  }
  decodeTop.io.flush   := hazard.io.flushAll || executeTop.io.branch.valid
  decodeTop.io.stalled := hazard.io.backend.stall(0)
  decodeTop.io.forwards.zipWithIndex.foreach {
    case (forward, i) =>
      forward.exeWR  := executeTop.io.out(i).write
      forward.mem0WR := memory0Top.io.out(i).write
      forward.mem1WR := memory1Top.io.out(i).write
      forward.mem2WR := memory2Top.io.out(i).write
      forward.wbWR   := mem2_wb.io.out(i).write
  }
  decodeTop.io.operands <> regFile.io.read

  regFile.io.write := VecInit(mem2_wb.io.out.map(_.write))

  id_exe.io.in    := decodeTop.io.out
  id_exe.io.stall := hazard.io.backend.stall
  id_exe.io.flush := hazard.io.flushAll || executeTop.io.branch.valid && !(hazard.io.backend
    .stall(1) && hazard.io.backend.stall(2))

  executeTop.io.ins     := id_exe.io.out
  executeTop.io.flush   := hazard.io.flushAll
  executeTop.io.stalled := id_exe.io.stalled
  // hio forward
  executeTop.io.rawHILO                  := hilo.io.out
  executeTop.io.hiloForwards(0).mem0HILO := exe_mem.io.out(0).hilo
  executeTop.io.hiloForwards(0).mem1HILO := mem0_mem1.io.out(0).hiloWrite
  executeTop.io.hiloForwards(1).mem0HILO := exe_mem.io.out(1).hilo
  executeTop.io.hiloForwards(1).mem1HILO := mem0_mem1.io.out(1).hiloWrite
  // cp0 forward
  executeTop.io.cp0Data               := VecInit(cp0.io.read.map(_.data))
  executeTop.io.cp0Forward(0).mem0CP0 := exe_mem.io.out(0).cp0
  executeTop.io.cp0Forward(0).mem1CP0 := mem0_mem1.io.out(0).cp0Write
  executeTop.io.cp0Forward(1).mem0CP0 := exe_mem.io.out(1).cp0
  executeTop.io.cp0Forward(1).mem1CP0 := mem0_mem1.io.out(1).cp0Write
  // op forward
  executeTop.io.opForward(0).mem0Op := exe_mem.io.out(0).operation
  executeTop.io.opForward(0).mem1Op := mem0_mem1.io.out(0).op
  executeTop.io.opForward(1).mem0Op := exe_mem.io.out(1).operation
  executeTop.io.opForward(1).mem1Op := mem0_mem1.io.out(1).op

  exe_mem.io.in    := executeTop.io.out
  exe_mem.io.stall := hazard.io.backend.stall
  exe_mem.io.flush := hazard.io.flushAll

  memory0Top.io.ins          := exe_mem.io.out
  memory0Top.io.stalled      := exe_mem.io.stalled
  memory0Top.io.uncached     := io.memAccess.uncached
  memory0Top.io.tlbCP0       := cp0.io.tlbCP0
  memory0Top.io.exceptionCP0 := cp0.io.exceptionCP0
  memory0Top.io.mem1Except   := mem0_mem1.io.out.map(_.except.asUInt().orR()).reduce(_ || _)

  cp0.io.intr         := io.intr
  cp0.io.read(0).addr := id_exe.io.out(0).imm26(15, 11)
  cp0.io.read(0).sel  := id_exe.io.out(0).imm26(2, 0)
  cp0.io.read(1).addr := id_exe.io.out(1).imm26(15, 11)
  cp0.io.read(1).sel  := id_exe.io.out(1).imm26(2, 0)

  cp0.io.cp0Write := memory1Top.io.cp0Write
  cp0.io.op       := memory1Top.io.op
  cp0.io.tlb      := memory1Top.io.tlbWrite

  cp0.io.except      := memory1Top.io.except
  cp0.io.inDelaySlot := memory1Top.io.inDelaySlot
  cp0.io.pc          := memory1Top.io.pc
  cp0.io.badAddr     := memory1Top.io.badAddr

  hilo.io.in := VecInit(mem0_mem1.io.out.map(_.hiloWrite))

  hazard.io.except         := memory1Top.io.except
  hazard.io.exceptJumpAddr := memory1Top.io.exceptJumpAddr
  hazard.io.EPC            := cp0.io.exceptionCP0.EPC

  hazard.io.frontend.stallReqFromIf0 := fetchTop.io.stallReq
  hazard.io.frontend.stallReqFromIf1 := fetch1Top.io.stallReq
  hazard.io.backend.stallReqFromId   := decodeTop.io.stallReq
  hazard.io.backend.stallReqFromExe  := executeTop.io.stallReq
  hazard.io.backend.stallReqFromMem0 := memory0Top.io.stallReq
  hazard.io.backend.stallReqFromMem1 := memory1Top.io.stallReq

  mem0_mem1.io.in    := memory0Top.io.out
  mem0_mem1.io.stall := hazard.io.backend.stall
  mem0_mem1.io.flush := hazard.io.flushAll && !memory1Top.io.stallReq

  memory1Top.io.ins          := mem0_mem1.io.out
  memory1Top.io.commit       := io.memAccess.commit
  memory1Top.io.exceptionCP0 := cp0.io.exceptionCP0

  mem1_mem2.io.in    := memory1Top.io.out
  mem1_mem2.io.stall := hazard.io.backend.stall
  mem1_mem2.io.flush := false.B

  memory2Top.io.ins          := mem1_mem2.io.out
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
