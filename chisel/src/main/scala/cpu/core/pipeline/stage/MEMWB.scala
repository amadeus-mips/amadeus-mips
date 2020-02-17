// See README.md for license details.

package cpu.core.pipeline.stage

import cpu.core.bundles.stage.MEMWBBundle
import cpu.core.components.{Stage, StageIO}

/**
 * Nothing need to modify.
 */
class MEMWB extends Stage(4, new MEMWBBundle) {

}
