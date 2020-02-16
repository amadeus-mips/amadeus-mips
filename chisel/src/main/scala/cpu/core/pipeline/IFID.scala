// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.stage.IFIDBundle
import cpu.core.components.{Stage, StageIO}

class IFID extends Stage(1, new IFIDBundle) {

  when(!io.stall(1)) {
    pipeReg.inst := Mux(io.in.instFetchExcept, 0.U, io.in.inst)
  }

}
