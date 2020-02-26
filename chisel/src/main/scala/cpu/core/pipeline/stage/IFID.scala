// See README.md for license details.

package cpu.core.pipeline.stage

import chisel3._
import cpu.core.bundles.stage.IFIDBundle
import cpu.core.components.Stage

class IFID(stageId: Int = 1) extends Stage(stageId, new IFIDBundle) {
}
