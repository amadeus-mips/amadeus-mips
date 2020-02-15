// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._
import cpu.core.bundles.IDEXBundle

class IDEX extends Module {
  val io = IO(new Bundle {
    val stall = Input(UInt(cpuStallLen.W))
    val flush = Input(Bool())

    val in = Input(new IDEXBundle)
    val nextInstInDelaySlot = Input(Bool())

    val out = Output(new IDEXBundle)  // to Execute
    val inDelaySlot = Output(Bool())  // back to Decode
  })

  val inDelaySlot = RegInit(false.B)
  val pipeReg = RegInit(0.U.asTypeOf(new IDEXBundle))

  /**
   * @see
   * [[cpu.core.pipeline.IFID.stallEnd]]
   */
  val stallEnd = io.stall(2) && !io.stall(3)

  inDelaySlot := MuxCase(inDelaySlot,
    Array(
      io.flush -> false.B,
      stallEnd -> inDelaySlot,
      !io.stall(2) -> io.nextInstInDelaySlot
    )
  )
  io.inDelaySlot := inDelaySlot

  when(io.flush || stallEnd) {
    pipeReg := 0.U.asTypeOf(new IDEXBundle)
  }.elsewhen(!io.stall(2)) {
    pipeReg <> io.in
  }.otherwise {
    // stalled, do nothing
  }
  io.out <> pipeReg
}
