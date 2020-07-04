// See README.md for license details.

package cpu.core

import chisel3._
import chisel3.util.Decoupled
import cpu.CPUConfig
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import cpu.core.Constants._
import cpu.core.bundles.stages._
import cpu.core.components.{CP0, HILO, RegFile, Stage}
import cpu.core.pipeline._

class Core(implicit conf: CPUConfig) extends MultiIOModule {
  val io = IO(new Bundle {
    val intr = Input(UInt(intrLen.W))

    val rInst = new Bundle{
      val addr = Decoupled(UInt(addrLen.W))
      val data = Flipped(Decoupled(UInt(dataLen.W)))
      val change = Output(Bool())
    }
    val rChannel = new NiseSramReadIO()
    val wChannel = new NiseSramWriteIO()

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
  val if_if1  = Module(new Stage(1, new IfIf1Bundle))
  val if1_id  = Module(new Stage(2, new If1IdBundle))
  val id_exe  = Module(new Stage(3, new IdExeBundle))
  val exe_mem = Module(new Stage(4, new ExeMemBundle))
  val mem_wb  = Module(new Stage(5, new MemWbBundle))

  fetchTop.io.stall   := hazard.io.stall(0)
  fetchTop.io.flush   := hazard.io.flush
  fetchTop.io.flushPC := hazard.io.flushPC
  fetchTop.io.lastDS  := hazard.io.lastDS

  fetchTop.io.predict     := fetch1Top.io.predict
  fetchTop.io.branch      := executeTop.io.branch
  fetchTop.io.inDelaySlot := fetch1Top.io.nextInstInDelaySlot

  fetchTop.io.instValid := io.rInst.addr.ready

  if_if1.io.in    := fetchTop.io.out
  if_if1.io.stall := hazard.io.stall
  if_if1.io.flush := hazard.io.flush ||
    (executeTop.io.branch.valid && hazard.io.predictFailFlush(0))

  fetch1Top.io.in     := if_if1.io.out
  fetch1Top.io.buffer := hazard.io.stall(0)
  fetch1Top.io.inst.bits   := io.rInst.data.bits
  fetch1Top.io.inst.valid := io.rInst.data.valid

  fetch1Top.io.predUpdate := executeTop.io.predUpdate

  if1_id.io.in    := fetch1Top.io.out
  if1_id.io.stall := hazard.io.stall
  if1_id.io.flush := hazard.io.flush ||
    (executeTop.io.branch.valid && hazard.io.predictFailFlush(1))

  decodeTop.io.in     := if1_id.io.out
  decodeTop.io.exeWR  := executeTop.io.out.write
  decodeTop.io.memWR  := memoryTop.io.out.write
  decodeTop.io.wbWR   := wbTop.io.out.write
  decodeTop.io.rsData := regFile.io.rsData
  decodeTop.io.rtData := regFile.io.rtData

  regFile.io.write := wbTop.io.out.write
  regFile.io.rs    := if1_id.io.out.inst(25, 21)
  regFile.io.rt    := if1_id.io.out.inst(20, 16)

  id_exe.io.in    := decodeTop.io.out
  id_exe.io.stall := hazard.io.stall
  id_exe.io.flush := hazard.io.flush

  executeTop.io.in      := id_exe.io.out
  executeTop.io.flush   := hazard.io.flush
  executeTop.io.rawHILO := hilo.io.out
  executeTop.io.memHILO := exe_mem.io.out.hilo
  executeTop.io.wbHILO  := wbTop.io.out.hilo
  executeTop.io.cp0Data := cp0.io.data
  executeTop.io.memCP0  := exe_mem.io.out.cp0
  executeTop.io.wbCP0   := wbTop.io.out.cp0

  exe_mem.io.in    := executeTop.io.out
  exe_mem.io.stall := hazard.io.stall
  exe_mem.io.flush := hazard.io.flush

  memoryTop.io.in                 := exe_mem.io.out
  memoryTop.io.inCP0Handle.status := cp0.io.status_o
  memoryTop.io.inCP0Handle.cause  := cp0.io.cause_o
  memoryTop.io.inCP0Handle.EPC    := cp0.io.EPC_o
  memoryTop.io.wbCP0              := wbTop.io.out.cp0

  cp0.io.intr        := io.intr
  cp0.io.cp0Write    := wbTop.io.out.cp0
  cp0.io.addr        := id_exe.io.out.imm26(15, 11)
  cp0.io.sel         := id_exe.io.out.imm26(2, 0)
  cp0.io.except      := memoryTop.io.except
  cp0.io.inDelaySlot := exe_mem.io.out.inDelaySlot
  cp0.io.pc          := Mux(memoryTop.io.except(EXCEPT_INTR), wbTop.io.out.pc + 4.U, exe_mem.io.out.pc)
  cp0.io.badAddr     := memoryTop.io.badAddr

  hilo.io.in := wbTop.io.out.hilo

  hazard.io.except      := memoryTop.io.except
  hazard.io.EPC         := memoryTop.io.EPC
  hazard.io.stallReq(0) := fetchTop.io.stallReq
  hazard.io.stallReq(1) := fetch1Top.io.stallReq
  hazard.io.stallReq(2) := decodeTop.io.stallReq
  hazard.io.stallReq(3) := executeTop.io.stallReq
  hazard.io.stallReq(4) := memoryTop.io.stallReq

  hazard.io.delaySlots(0) := fetchTop.io.out.inDelaySlot
  hazard.io.delaySlots(1) := fetch1Top.io.out.inDelaySlot
  hazard.io.delaySlots(2) := decodeTop.io.out.inDelaySlot

  mem_wb.io.in    := memoryTop.io.out
  mem_wb.io.stall := hazard.io.stall
  mem_wb.io.flush := hazard.io.flush

  wbTop.io.in    := mem_wb.io.out
  wbTop.io.rData := io.rChannel.data

  io.rInst.addr.bits  := fetchTop.io.out.pc
  io.rInst.addr.valid := fetchTop.io.pcValid
  io.rInst.data.ready := true.B

  io.rChannel <> memoryTop.io.rData
  io.wChannel <> memoryTop.io.wData

  io.rInst.change := fetchTop.io.pcChange
}
