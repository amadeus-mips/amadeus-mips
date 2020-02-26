// See README.md for license details.

package cpu.core

import chisel3._
import chisel3.util.MuxLookup
import cpu.common.{DataReadIO, DataWriteIO}
import cpu.core.Constants._
import cpu.core.bundles.ValidBundle
import cpu.core.components.{CP0, HILO, RegFile}
import cpu.core.fetch.Fetch
import cpu.core.pipeline._
import cpu.core.pipeline.stage._

class Core extends MultiIOModule {
  val io = IO(new Bundle {
    val intr = Input(UInt(intrLen.W))
    val inst = Input(new ValidBundle)

    val pc = Output(new ValidBundle)
    val load = new DataReadIO
    val store = new DataWriteIO
    val addr = Output(UInt(addrLen.W))
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
  val if_id = Module(new IFID)
  val id_exe = Module(new IDEXE)
  val exe_mem = Module(new EXEMEM)
  val mem_wb = Module(new MEMWB)

  fetch.io.stall := ctrl.io.stall
  fetch.io.flush := ctrl.io.flush
  fetch.io.flushPC := ctrl.io.flushPC

  fetch.io.branch <> decodeTop.io.branch
  fetch.io.instValid := io.inst.valid

  if_id.io.in <> fetch.io.out
  if_id.io.stall := ctrl.io.stall
  if_id.io.flush := ctrl.io.flush

  val stallByOther = !ctrl.io.flush && ctrl.io.stall(0)
  val instBuffer = RegInit(0.U.asTypeOf(new ValidBundle))
  instBuffer.valid := stallByOther
  instBuffer.bits := Mux(instBuffer.valid, instBuffer.bits, io.inst.bits)
  val inst = Mux(instBuffer.valid, instBuffer.bits, io.inst.bits)

  decodeTop.io.in <> if_id.io.out
  decodeTop.io.inst := inst
  decodeTop.io.exeOp := id_exe.io.out.operation
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
  memoryTop.io.load <> io.load
  memoryTop.io.store <> io.store

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

  io.pc.bits := fetch.io.out.pc
  io.pc.valid := fetch.io.outPCValid
  io.addr := memoryTop.io.addr
}
