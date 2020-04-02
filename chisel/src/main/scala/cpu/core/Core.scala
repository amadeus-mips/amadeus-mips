// See README.md for license details.

package cpu.core

import chisel3._
import common.Buffer
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import cpu.core.Constants._
import cpu.core.bundles.stage5.{ExeMemBundle, IfIdBundle, MemWbBundle}
import cpu.core.components.{CP0, HILO, RegFile, Stage}
import cpu.core.fetch.Fetch
import cpu.core.pipeline._
import cpu.core.pipeline.stage5._

class Core extends MultiIOModule {
  val io = IO(new Bundle {
    val intr = Input(UInt(intrLen.W))

    val rInst = new NiseSramReadIO()
    val rData = new NiseSramReadIO()
    val wData = new NiseSramWriteIO()
  })

  /**
   * fetch | decodeTop | executeTop | memoryTop | wb
   */
  val fetch = Module(new Fetch)
  val decodeTop = Module(new DecodeTop)
  val executeTop = Module(new ExecuteTop)
  val memoryTop = Module(new MemoryTop)
  val regFile = Module(new RegFile)
  val cp0 = Module(new CP0)
  val hilo = Module(new HILO)
  val ctrl = Module(new CTRL)

  // stages
  val if_id = Module(new Stage(1, new IfIdBundle))
  val id_exe = Module(new IdExe)
  val exe_mem = Module(new Stage(3, new ExeMemBundle))
  val mem_wb = Module(new Stage(4, new MemWbBundle))

  fetch.io.stall := ctrl.io.stall
  fetch.io.flush := ctrl.io.flush
  fetch.io.flushPC := ctrl.io.flushPC

  fetch.io.branch <> decodeTop.io.branch
  fetch.io.instValid := io.rInst.valid

  if_id.io.in <> fetch.io.out
  if_id.io.stall := ctrl.io.stall
  if_id.io.flush := ctrl.io.flush

  val stalled = !ctrl.io.flush && ctrl.io.stall(0)
  val instBuffer = Module(new Buffer(dataLen))
  instBuffer.io.in := io.rInst.data
  instBuffer.io.en := stalled
  val inst = instBuffer.io.out

  decodeTop.io.in <> if_id.io.out
  decodeTop.io.inst := inst
  decodeTop.io.exeWR := executeTop.io.out.write
  decodeTop.io.memWR := memoryTop.io.out.write
  decodeTop.io.rsData := regFile.io.rsData
  decodeTop.io.rtData := regFile.io.rtData
  decodeTop.io.inDelaySlot := id_exe.ioExt.inDelaySlot

  regFile.io.write <> mem_wb.io.out.write
  regFile.io.rs := inst(25, 21)
  regFile.io.rt := inst(20, 16)

  id_exe.io.in <> decodeTop.io.out
  id_exe.io.stall := ctrl.io.stall
  id_exe.io.flush := ctrl.io.flush
  id_exe.ioExt.nextInstInDelaySlot := decodeTop.io.nextInstInDelaySlot

  executeTop.io.in <> id_exe.io.out
  executeTop.io.flush := ctrl.io.flush
  executeTop.io.rawHILO <> hilo.io.out
  executeTop.io.memHILO <> exe_mem.io.out.hilo
  executeTop.io.wbHILO <> mem_wb.io.out.hilo
  executeTop.io.cp0Data := cp0.io.data
  executeTop.io.memCP0 <> exe_mem.io.out.cp0
  executeTop.io.wbCP0 <> mem_wb.io.out.cp0

  exe_mem.io.in <> executeTop.io.out
  exe_mem.io.stall := ctrl.io.stall
  exe_mem.io.flush := ctrl.io.flush

  memoryTop.io.in <> exe_mem.io.out
  memoryTop.io.inCP0Handle.status := cp0.io.status_o
  memoryTop.io.inCP0Handle.cause := cp0.io.cause_o
  memoryTop.io.inCP0Handle.EPC := cp0.io.EPC_o
  memoryTop.io.wbCP0 <> mem_wb.io.out.cp0
  memoryTop.io.rData <> io.rData
  memoryTop.io.wData <> io.wData

  cp0.io.intr := io.intr
  cp0.io.cp0Write <> mem_wb.io.out.cp0
  cp0.io.addr := id_exe.io.out.imm26(15, 11)
  cp0.io.sel := id_exe.io.out.imm26(2,0)
  cp0.io.except <> memoryTop.io.except
  cp0.io.inDelaySlot := exe_mem.io.out.inDelaySlot
  cp0.io.pc := exe_mem.io.out.pc
  cp0.io.badAddr := memoryTop.io.badAddr

  hilo.io.in := mem_wb.io.out.hilo

  ctrl.io.except <> memoryTop.io.except
  ctrl.io.EPC := memoryTop.io.EPC
  ctrl.io.stallReqFromFetch := fetch.io.outStallReq
  ctrl.io.stallReqFromDecode := decodeTop.io.stallReq
  ctrl.io.stallReqFromExecute := executeTop.io.stallReq
  ctrl.io.stallReqFromMemory := memoryTop.io.stallReq

  mem_wb.io.in <> memoryTop.io.out
  mem_wb.io.stall := ctrl.io.stall
  mem_wb.io.flush := ctrl.io.flush

  io.rInst.addr := fetch.io.out.pc
  io.rInst.enable := fetch.io.outPCValid
}
