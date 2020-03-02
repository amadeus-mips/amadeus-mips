// See README.md for license details.

package cpu.core.pipeline.stage

import cpu.core.bundles.stage.MEMWBBundle
import cpu.core.components.Stage

/**
 * Nothing need to modify.
 */
class MEMWB(stageId: Int = 4) extends Stage(stageId, new MEMWBBundle) {

}
