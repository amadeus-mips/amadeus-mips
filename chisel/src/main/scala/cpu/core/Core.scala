// See README.md for license details.

package cpu.core

import chisel3._
import chisel3.util.Decoupled
import cpu.CPUConfig
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
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

class Core(implicit conf: CPUConfig) extends MultiIOModule {
  val io = IO(new Bundle {
    val intr = Input(UInt(intrLen.W))

    val rInst    = new InstFetchIO
    val rChannel = new NiseSramReadIO()
    val wChannel = new NiseSramWriteIO()
    val tlb      = new TLBOpIO(conf.tlbSize)
  })

  /**
    * fetchTop | fetch1Top | decodeTop | executeTop | memoryTop | wb
    */
  val fetchTop   = Module(new FetchTop)
  val fetch1Top  = Module(new Fetch1Top)
  val decodeTop  = Module(new DecodeTop)
  val executeTop = Module(new ExecuteTop)
  val memoryTop  = Module(new MemoryTop)
  val wbTop      = Module(new WbTop)

  val regFile = Module(new RegFile)
  val cp0     = Module(new CP0)
  val hilo    = Module(new HILO)
  val hazard  = Module(new Hazard)

  // stages
  val if_if1   = Module(new Stage(new IfIf1Bundle))
  val instFIFO = Module(new InstructionFIFO(new InstructionFIFOEntry()))
  val id_exe   = Module(new Stage(new IdExeBundle))
  val exe_mem  = Module(new Stage(new ExeMemBundle))
  val mem_wb   = Module(new Stage(new MemWbBundle))

  fetchTop.io.stall   := hazard.io.stallIf0
  fetchTop.io.flush   := hazard.io.flushAll
  fetchTop.io.flushPC := hazard.io.flushPC

  fetchTop.io.predUpdate.bits  := executeTop.io.predUpdate
  fetchTop.io.predUpdate.valid := hazard.io.branchValid

  fetchTop.io.predict       := fetch1Top.io.predict
  fetchTop.io.predictWithDS := fetch1Top.io.predictWithDS
  fetchTop.io.branch.bits   := executeTop.io.branch.bits
  fetchTop.io.branch.valid  := hazard.io.branchValid
  fetchTop.io.inDelaySlot   := fetch1Top.io.nextInstInDelaySlot

  fetchTop.io.ramReady := io.rInst.addr.ready

  fetchTop.io.tlbExcept.refill  := io.tlb.except.inst.refill
  fetchTop.io.tlbExcept.invalid := io.tlb.except.inst.invalid

  if_if1.io.in    := fetchTop.io.out
  if_if1.io.stall := hazard.io.stallIf1
  if_if1.io.flush := hazard.io.flushAll || hazard.io.flushIf1

  fetch1Top.io.in         := if_if1.io.out
  fetch1Top.io.fifoReady  := instFIFO.io.readyForEnqueue
  fetch1Top.io.flushFIFO  := hazard.io.flushFIFO
  fetch1Top.io.inst.bits  := io.rInst.data.bits
  fetch1Top.io.inst.valid := io.rInst.data.valid

  instFIFO.reset                 := reset.asBool() || hazard.io.flushAll || hazard.io.flushFIFO
  instFIFO.io.flushTail          := false.B
  instFIFO.io.enqueue            := fetch1Top.io.out
  instFIFO.io.dequeue.head.ready := hazard.io.fifoDeqReady

  decodeTop.io.in      := instFIFO.io.dequeue.head.bits
  decodeTop.io.inValid := instFIFO.io.dequeue.head.valid
  decodeTop.io.exeWR   := executeTop.io.out.write
  decodeTop.io.memWR   := memoryTop.io.out.write
  decodeTop.io.wbWR    := wbTop.io.out.write
  decodeTop.io.rsData  := regFile.io.rsData
  decodeTop.io.rtData  := regFile.io.rtData

  regFile.io.write := wbTop.io.out.write
  regFile.io.rs    := instFIFO.io.dequeue.head.bits.inst(25, 21)
  regFile.io.rt    := instFIFO.io.dequeue.head.bits.inst(20, 16)

  id_exe.io.in    := decodeTop.io.out
  id_exe.io.stall := hazard.io.stallExe
  id_exe.io.flush := hazard.io.flushAll || hazard.io.flushExe

  executeTop.io.in          := id_exe.io.out
  executeTop.io.flush       := hazard.io.flushAll
  executeTop.io.decodeValid := instFIFO.io.dequeue.head.valid
  executeTop.io.rawHILO     := hilo.io.out
  executeTop.io.memHILO     := exe_mem.io.out.hilo
  executeTop.io.wbHILO      := wbTop.io.out.hilo
  executeTop.io.cp0Data     := cp0.io.data
  executeTop.io.memCP0      := exe_mem.io.out.cp0
  executeTop.io.wbCP0       := wbTop.io.out.cp0
  executeTop.io.memOp       := exe_mem.io.out.operation
  executeTop.io.wbOp        := mem_wb.io.out.operation

  exe_mem.io.in    := executeTop.io.out
  exe_mem.io.stall := hazard.io.stallMem
  exe_mem.io.flush := hazard.io.flushAll || hazard.io.flushMem

  memoryTop.io.in           := exe_mem.io.out
  memoryTop.io.exceptionCP0 := cp0.io.exceptionCP0
  memoryTop.io.tlbCP0       := cp0.io.tlbCP0
  memoryTop.io.wbCP0        := wbTop.io.out.cp0

  cp0.io.intr        := io.intr
  cp0.io.cp0Write    := wbTop.io.out.cp0
  cp0.io.addr        := id_exe.io.out.imm26(15, 11)
  cp0.io.sel         := id_exe.io.out.imm26(2, 0)
  cp0.io.except      := memoryTop.io.except
  cp0.io.inDelaySlot := exe_mem.io.out.inDelaySlot
  cp0.io.pc          := exe_mem.io.out.pc
  cp0.io.badAddr     := memoryTop.io.badAddr

  cp0.io.op  := wbTop.io.out.operation
  cp0.io.tlb := wbTop.io.out.tlb

  hilo.io.in := wbTop.io.out.hilo

  hazard.io.except          := memoryTop.io.except
  hazard.io.EPC             := memoryTop.io.EPC
  hazard.io.stallReqFromIf0 := fetchTop.io.stallReq
  hazard.io.stallReqFromIf1 := fetch1Top.io.stallReq
  hazard.io.stallReqFromId  := decodeTop.io.stallReq
  hazard.io.stallReqFromExe := executeTop.io.stallReq
  hazard.io.stallReqFromMem := memoryTop.io.stallReq

  hazard.io.predictFail := executeTop.io.branch.valid
  hazard.io.waitingDS   := executeTop.io.waitingDS

  mem_wb.io.in    := memoryTop.io.out
  mem_wb.io.stall := false.B
  mem_wb.io.flush := hazard.io.flushAll || hazard.io.flushWb

  wbTop.io.in    := mem_wb.io.out
  wbTop.io.rData := io.rChannel.data

  io.rInst.addr.bits  := fetchTop.io.out.pc
  io.rInst.addr.valid := fetchTop.io.pcValid
  io.rInst.data       <> fetch1Top.io.inst

  io.rChannel <> memoryTop.io.rData
  io.wChannel <> memoryTop.io.wData
  io.tlb      <> memoryTop.io.tlb

  io.rInst.change := fetchTop.io.pcChange
}
