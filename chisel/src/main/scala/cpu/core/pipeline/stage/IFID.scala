// See README.md for license details.

package cpu.core.pipeline.stage

import chisel3._
import cpu.core.bundles.stage.IFIDBundle
import cpu.core.components.Stage

class IFID extends Stage(1, new IFIDBundle) {

  when(io.flush || stallEnd) {
    pipeReg.inst := 0.U
  }.elsewhen(notStalled) {
    pipeReg.inst := Mux(io.in.instFetchExcept, 0.U, io.in.inst)
  }.otherwise {
    // stalled, do nothing
  }

}
