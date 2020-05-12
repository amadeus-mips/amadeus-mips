// See README.md for license details.

package cpu.core

import chisel3._
import cpu.CPUConfig
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import cpu.core.Constants._
import cpu.core.bundles.stages.{ExeMemBundle, IdExeBundle, IfIdBundle, MemWbBundle}
import cpu.core.components.{CP0, HILO, RegFile, Stage}
import cpu.core.pipeline._
import shared.Buffer

class Core extends MultiIOModule {
  val io = IO(new Bundle {
    val intr = Input(UInt(intrLen.W))

    val rInst = new NiseSramReadIO()
    val rData = new NiseSramReadIO()
    val wData = new NiseSramWriteIO()
  })

  implicit val conf: CPUConfig = new CPUConfig()

  /**
    * fetch | decodeTop | executeTop | memoryTop | wb
    */
  val fetchTop   = Module(new FetchTop)
  val decodeTop  = Module(new DecodeTop)
  val executeTop = Module(new ExecuteTop)
  val memoryTop  = Module(new MemoryTop)
  val regFile    = Module(new RegFile)
  val cp0        = Module(new CP0)
  val hilo       = Module(new HILO)
  val hazard     = Module(new Hazard)

  // stages
  val if_id   = Module(new Stage(1, new IfIdBundle))
  val id_exe  = Module(new Stage(2, new IdExeBundle))
  val exe_mem = Module(new Stage(3, new ExeMemBundle))
  val mem_wb  = Module(new Stage(4, new MemWbBundle))

  fetchTop.io.stall   := hazard.io.stall(0)
  fetchTop.io.flush   := hazard.io.flush
  fetchTop.io.flushPC := hazard.io.flushPC

  fetchTop.io.predict     := decodeTop.io.predict
  fetchTop.io.branch      := executeTop.io.branch
  fetchTop.io.inDelaySlot := decodeTop.io.nextInstInDelaySlot

  fetchTop.io.instValid := io.rInst.valid

  if_id.io.in    := fetchTop.io.out
  if_id.io.stall := hazard.io.stall
  if_id.io.flush := hazard.io.flush ||
    (!fetchTop.io.out.inDelaySlot && executeTop.io.branch.valid && !hazard.io.stall(3))

  val inst = Buffer(in = io.rInst.data, en = !hazard.io.flush && hazard.io.stall(0)).io.out

  decodeTop.io.in     := if_id.io.out
  decodeTop.io.inst   := inst
  decodeTop.io.exeWR  := executeTop.io.out.write
  decodeTop.io.memWR  := memoryTop.io.out.write
  decodeTop.io.wbWR   := mem_wb.io.out.write
  decodeTop.io.rsData := regFile.io.rsData
  decodeTop.io.rtData := regFile.io.rtData

  decodeTop.io.predictorUpdate := DontCare
  decodeTop.io.predictorTaken  := DontCare

  regFile.io.write := mem_wb.io.out.write
  regFile.io.rs    := inst(25, 21)
  regFile.io.rt    := inst(20, 16)

  id_exe.io.in    := decodeTop.io.out
  id_exe.io.stall := hazard.io.stall
  id_exe.io.flush := hazard.io.flush

  executeTop.io.in      := id_exe.io.out
  executeTop.io.flush   := hazard.io.flush
  executeTop.io.rawHILO := hilo.io.out
  executeTop.io.memHILO := exe_mem.io.out.hilo
  executeTop.io.wbHILO  := mem_wb.io.out.hilo
  executeTop.io.cp0Data := cp0.io.data
  executeTop.io.memCP0  := exe_mem.io.out.cp0
  executeTop.io.wbCP0   := mem_wb.io.out.cp0

  exe_mem.io.in    := executeTop.io.out
  exe_mem.io.stall := hazard.io.stall
  exe_mem.io.flush := hazard.io.flush

  memoryTop.io.in                 := exe_mem.io.out
  memoryTop.io.inCP0Handle.status := cp0.io.status_o
  memoryTop.io.inCP0Handle.cause  := cp0.io.cause_o
  memoryTop.io.inCP0Handle.EPC    := cp0.io.EPC_o
  memoryTop.io.wbCP0              := mem_wb.io.out.cp0

  cp0.io.intr        := io.intr
  cp0.io.cp0Write    := mem_wb.io.out.cp0
  cp0.io.addr        := id_exe.io.out.imm26(15, 11)
  cp0.io.sel         := id_exe.io.out.imm26(2, 0)
  cp0.io.except      := memoryTop.io.except
  cp0.io.inDelaySlot := exe_mem.io.out.inDelaySlot
  cp0.io.pc          := Mux(memoryTop.io.except(EXCEPT_INTR), mem_wb.io.out.pc + 4.U, exe_mem.io.out.pc)
  cp0.io.badAddr     := memoryTop.io.badAddr

  hilo.io.in := mem_wb.io.out.hilo

  hazard.io.except              := memoryTop.io.except
  hazard.io.EPC                 := memoryTop.io.EPC
  hazard.io.stallReqFromFetch   := fetchTop.io.stallReq
  hazard.io.stallReqFromDecode  := decodeTop.io.stallReq
  hazard.io.stallReqFromExecute := executeTop.io.stallReq
  hazard.io.stallReqFromMemory  := memoryTop.io.stallReq

  mem_wb.io.in    := memoryTop.io.out
  mem_wb.io.stall := hazard.io.stall
  mem_wb.io.flush := hazard.io.flush

  io.rInst.addr   := fetchTop.io.out.pc
  io.rInst.enable := fetchTop.io.pcValid

  io.rData <> memoryTop.io.rData
  io.wData <> memoryTop.io.wData
}
