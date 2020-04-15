// See README.md for license details.

package shared

import chisel3._

trait AXIConstants {
  val INST_ID = 0.U
  val DATA_ID = 1.U
}

object Constants extends AXIConstants {

}
