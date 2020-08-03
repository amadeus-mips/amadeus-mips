package cpu.core.pipeline

import chisel3._
import chisel3.util.Valid
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.stages.IfIf1Bundle
import cpu.core.components.{BrPrUpdateBundle, TwoBitPredictor}
import shared.ValidBundle

class FetchTop(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    // from hazard module
    val stall   = Input(Bool())
    val flush   = Input(Bool())
    val flushPC = Input(UInt(addrLen.W))
    // from decode stage
    val inDelaySlot   = Input(Bool())
    val predict       = Input(ValidBundle(32))
    val predictWithDS = Input(Bool())
    // from execute stage
    val branch     = Input(ValidBundle(32))
    val predUpdate = Flipped(Valid(new BrPrUpdateBundle))

    // from ram
    val ramReady = Input(Bool())

    // to IFID
    val out = Output(new IfIf1Bundle)
    // to ram
    val pcValid  = Output(Bool())
    val pcChange = Output(Bool())
    // to ctrl
    val stallReq = Output(Bool())

    val tlbExcept = Input(new Bundle {
      val refill  = Bool()
      val invalid = Bool()
    })
  })
  val branchPredictor = Module(new TwoBitPredictor())

  val hazard = Module(new cpu.core.fetch.Hazard)
  val pcMux  = Module(new cpu.core.fetch.PCMux(n = 4))

  val except  = io.out.except.asUInt().orR()
  val pcValid = !(io.branch.valid || io.flush) && !(hazard.io.out.predict.valid && hazard.io.out.predictWithDS)

  branchPredictor.io.update := io.predUpdate
  branchPredictor.io.pc     := pcMux.io.pc

  hazard.io.flush            := io.flush
  hazard.io.stall            := io.stall
  hazard.io.in.inDelaySlot   := io.inDelaySlot
  hazard.io.in.branch        := io.branch
  hazard.io.in.predict       := io.predict
  hazard.io.in.predictWithDS := io.predictWithDS

  pcMux.io.ins(0) := ValidBundle(io.flush, io.flushPC)
  pcMux.io.ins(1) := io.branch
  pcMux.io.ins(2) := ValidBundle(io.stall, pcMux.io.pc)
  pcMux.io.ins(3) := hazard.io.out.predict

  io.out.pc             := pcMux.io.pc
  io.out.instValid      := io.ramReady && !except
  io.out.inDelaySlot    := hazard.io.out.inDelaySlot
  io.out.validPcMask(0) := pcValid && !io.stallReq
  io.out.validPcMask(1) :=
    !pcMux.io.pcCacheCorner && (!hazard.io.out.predict.valid || hazard.io.out.predictWithDS) && io.out.instValid && pcValid
  io.out.brPredict   := branchPredictor.io.prediction
  io.out.brPrHistory := branchPredictor.io.history

  io.out.except                          := DontCare
  io.out.except(EXCEPT_FETCH)            := pcMux.io.pcNotAligned
  io.out.except(EXCEPT_INST_TLB_REFILL)  := io.tlbExcept.refill
  io.out.except(EXCEPT_INST_TLB_INVALID) := io.tlbExcept.invalid

  io.pcValid  := !except && pcValid
  io.pcChange := false.B
  io.stallReq := !io.ramReady && !except
}
