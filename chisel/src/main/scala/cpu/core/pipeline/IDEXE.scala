// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.MuxCase
import cpu.core.bundles.stage.IDEXEBundle
import cpu.core.components.{Stage, StageIO}

private class IDEXEIO extends StageIO(new IDEXEBundle) {
  val nextInstInDelaySlot = Input(Bool())
  val inDelaySlot = Output(Bool())  // back to Decode
}

class IDEXE extends Stage(2, new IDEXEBundle) {
  override val io = IO(new IDEXEIO)
  val inDelaySlot = RegInit(false.B)
  inDelaySlot := MuxCase(inDelaySlot,
    Array(
      io.flush -> false.B,
      stallEnd -> inDelaySlot,
      notStalled -> io.nextInstInDelaySlot
    )
  )
  io.inDelaySlot := inDelaySlot
}
