package cpu.core.pipeline

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.stages.IfIdBundle
import shared.ValidBundle

class FetchTop extends Module {
  val io = IO(new Bundle {
    // from ctrl module
    val stall   = Input(Bool())
    val flush   = Input(Bool())
    val flushPC = Input(UInt(addrLen.W))

    // from decode stage
    val inDelaySlot = Input(Bool())
    // from execute stage
    val branch = Input(new ValidBundle)

    // from ram
    val instValid = Input(Bool())

    // to IFID
    val out = Output(new IfIdBundle)
    // to ram
    val pcValid = Output(Bool())
    // to ctrl
    val stallReq = Output(Bool())
  })

  val hazard = Module(new cpu.core.fetch.Hazard)
  val pcMux  = Module(new cpu.core.fetch.PCMux(n = 3))

  hazard.io.flush          := io.flush
  hazard.io.stall          := io.stall
  hazard.io.in.inDelaySlot := io.inDelaySlot
  hazard.io.in.branch      <> io.branch

  pcMux.io.ins(0) <> ValidBundle(io.flush, io.flushPC)
  pcMux.io.ins(1) <> hazard.io.out.branch
  pcMux.io.ins(2) <> ValidBundle(io.stall, pcMux.io.pc)

  io.out.pc              := pcMux.io.pc
  io.out.instFetchExcept := pcMux.io.pcNotAligned
  io.out.instValid       := io.instValid
  io.out.inDelaySlot     := hazard.io.out.inDelaySlot

  io.pcValid  := !pcMux.io.pcNotAligned
  io.stallReq := !io.instValid && !pcMux.io.pcNotAligned
}
