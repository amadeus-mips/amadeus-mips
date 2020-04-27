// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

/**
 * CP0 signal
 */
class CPBundle extends Bundle {
  val enable = Bool()   // write enable
  val address = UInt(regAddrLen.W)
  val sel = UInt(3.W)

  val data = UInt(dataLen.W)
  /** data valid, to handle data hazard */
  val valid = Bool()
}
