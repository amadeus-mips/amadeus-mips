// See README.md for license details.

package cpu.core.pipeline.stage

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants.cpuStallLen
import cpu.core.bundles.stage.IDEXEBundle
import cpu.core.components.{Stage, StageIO}

class IDEXEIO extends Bundle {
  val stall = Input(UInt(cpuStallLen.W))
  val flush = Input(Bool())
  val in = Input(new IDEXEBundle)
  val out = Output(new IDEXEBundle)
  val nextInstInDelaySlot = Input(Bool())
  val inDelaySlot = Output(Bool())  // back to Decode
}

class IDEXE extends Module {
  val io = IO(new IDEXEIO)

  val pipeReg = RegInit(0.U.asTypeOf(new IDEXEBundle))

  def stallEnd: Bool = io.stall(2) && !io.stall(3)
  def notStalled: Bool = !io.stall(2)

  val inDelaySlot = RegInit(false.B)
  inDelaySlot := MuxCase(inDelaySlot,
    Array(
      io.flush -> false.B,
      stallEnd -> inDelaySlot,
      notStalled -> io.nextInstInDelaySlot
    )
  )
  io.inDelaySlot := inDelaySlot

  when(io.flush || stallEnd) {
    pipeReg := 0.U.asTypeOf(new IDEXEBundle)
  }.elsewhen(notStalled) {
    pipeReg <> io.in
  }.otherwise {
    // stalled, do nothing
  }
  io.out <> pipeReg
}
