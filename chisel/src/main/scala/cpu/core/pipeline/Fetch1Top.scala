package cpu.core.pipeline

import chisel3._
import chisel3.util.Decoupled
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.stages.{If1IdBundle, IfIf1Bundle}
import shared.ValidBundle

class Fetch1Top(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle() {
    val in      = Input(new IfIf1Bundle)
    val itReady = Input(Bool())
    val inst    = Flipped(Decoupled(Vec(conf.fetchAmount, UInt(dataLen.W))))

    val out = Output(new If1IdBundle)

    val stallReq = Output(Bool())

    val predict = Output(ValidBundle(UInt(32.W))) // to Fetch

    val nextInstInDelaySlot = Output(Bool()) // to Fetch
  })

  val inst = Mux(
    !io.in.instValid,
    0.U.asTypeOf(Vec(conf.fetchAmount, UInt(dataLen.W))),
    io.inst.bits
  )

  val isBranchInstVec = io.inst.bits.map(isBranchInst)

  val branchVec = io.in.brPredict.zip(isBranchInstVec).zip(io.in.validPcMask).map {
    case ((entry, isBranch), validPc) => entry.valid && isBranch && entry.bits.statistics(1) && validPc
  }

  io.predict.bits  := Mux(isBranchInstVec(0), io.in.brPredict(0).bits.target, io.in.brPredict(1).bits.target)
  io.predict.valid := io.inst.fire() && branchVec.reduce(_ || _)

  io.out.pc    := io.in.pc
  io.out.pcOne := io.in.pc
  io.out.inDelaySlot := VecInit(
    // shift right
    Seq(io.in.inDelaySlot) ++ isBranchInstVec.zip(io.in.validPcMask).reverse.tail.reverse.map(e => e._1 && e._2)
  )
  io.out.except    := io.in.except
  io.out.instValid := io.in.instValid
  io.out.inst      := inst

  for (i <- 0 until conf.fetchAmount) {
    io.out.brPredict(i).valid := !branchVec.slice(0, i).reduce(_ || _) && branchVec(i)
    io.out.brPredict(i).bits  := io.in.brPredict(i).bits.target
  }

  io.inst.ready := io.itReady

  io.stallReq := io.in.instValid && !io.inst.valid

  // Whether the last instruction in current instruction package is branch
  val lastIsBranch = WireInit(false.B)
  for (i <- 0 until conf.fetchAmount) {
    when(io.in.validPcMask(i)) {
      lastIsBranch := isBranchInstVec(i)
    }
  }
  io.nextInstInDelaySlot := lastIsBranch && io.inst.fire()
}
