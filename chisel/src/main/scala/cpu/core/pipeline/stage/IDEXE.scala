// See README.md for license details.

package cpu.core.pipeline.stage

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants.cpuStallLen
import cpu.core.bundles.stage.IDEXEBundle
import cpu.core.components.{Stage, StageIO}

class IDEXE extends Stage(2, new IDEXEBundle) {
  val ioExt = IO(new Bundle {
    val nextInstInDelaySlot = Input(Bool())
    val inDelaySlot = Output(Bool()) // back to Decode
  })

  val inDelaySlot = RegInit(false.B)
  inDelaySlot := MuxCase(inDelaySlot,
    Array(
      io.flush -> false.B,
      stallEnd -> inDelaySlot,
      notStalled -> ioExt.nextInstInDelaySlot
    )
  )
  ioExt.inDelaySlot := inDelaySlot

}
