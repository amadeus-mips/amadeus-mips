package cpu.core.pipeline

import chisel3._
import chisel3.util.{Decoupled, Valid}
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.InstructionFIFOEntry
import cpu.core.bundles.stages.IfIf1Bundle
import shared.ValidBundle

class Fetch1Top(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle() {
    val in   = Input(new IfIf1Bundle)
    val inst = Flipped(Decoupled(Vec(conf.fetchAmount, UInt(dataLen.W))))
    
    val fifoReady = Input(Bool())
    val flushFIFO = Input(Bool())

    val out = Vec(conf.fetchAmount, Valid(new InstructionFIFOEntry()))

    val stallReq = Output(Bool())

    val predict       = Output(ValidBundle(32)) // to Fetch
    val predictWithDS = Output(Bool())

    val nextInstInDelaySlot = Output(Bool()) // to Fetch
  })

  val instVec = Mux(
    !io.in.instValid,
    0.U.asTypeOf(Vec(conf.fetchAmount, UInt(dataLen.W))),
    io.inst.bits
  )

  val isBranchInstVec = io.inst.bits.map(isBranchInst)

  val branchVec = io.in.brPredict.zip(isBranchInstVec).zip(io.in.validPcMask).map {
    case ((entry, isBranch), validPc) => entry.valid && isBranch && entry.statistics(1) && validPc
  }

  val inDelaySlotVec = VecInit(
    // shift right
    Seq(io.in.inDelaySlot) ++ isBranchInstVec.zip(io.in.validPcMask).reverse.tail.reverse.map(e => e._1 && e._2)
  )
  val pcVec   = (0 until conf.fetchAmount).map(e => (e * 4).U + io.in.pc)
  val pcValid = io.in.validPcMask.asUInt().orR()
  io.out.zip(pcVec).zip(instVec).zip(inDelaySlotVec).zip(io.in.brPredict).zipWithIndex.zip(io.in.validPcMask).foreach {
    case ((((((out, pc), inst), inDS), predict), i), valid) =>
      out.bits.pc              := pc
      out.bits.inst            := inst
      out.bits.except          := io.in.except
      out.bits.inDelaySlot     := inDS
      out.bits.brPredict.bits  := predict.target
      out.bits.brPredict.valid := (if (i == 0) branchVec(i) else !branchVec.slice(0, i).reduce(_ || _) && branchVec(i))
      out.bits.valid           := (if (i == 0) !io.in.except.asUInt().orR() && valid else valid)
      out.valid :=
        (if (i == 0) valid && (io.in.except.asUInt().orR() || io.inst.valid)
         else valid && io.inst.valid) && !io.flushFIFO
  }
  io.inst.ready := io.fifoReady || io.flushFIFO

  io.stallReq := pcValid && Mux(io.in.instValid, !io.inst.fire(), !io.inst.ready)

  // Whether the last instruction in current instruction package is branch
  val lastIsBranch = WireInit(false.B)
  for (i <- 0 until conf.fetchAmount) {
    when(io.in.validPcMask(i)) {
      lastIsBranch := isBranchInstVec(i)
    }
  }
  io.nextInstInDelaySlot := lastIsBranch && io.inst.fire()
  // TODO
  io.predict.bits  := Mux(isBranchInstVec(0), io.in.brPredict(0).target, io.in.brPredict(1).target)
  io.predict.valid := io.inst.fire() && branchVec.reduce(_ || _)
  io.predictWithDS := !lastIsBranch

  assert(!io.in.except.asUInt().orR() || io.in.validPcMask(0) && !io.in.validPcMask.tail.reduce(_ || _))
}
