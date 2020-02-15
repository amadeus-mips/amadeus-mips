// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

/**
 * CP0 control signal is in field `control`
 *
 * @see
 * [[cpu.core.bundles.CPControlBundle]]
 */
class CPBundle extends Bundle {
  val control = new CPControlBundle
  val data = UInt(dataLen.W)
}

/**
 * Only has control signal
 */
class CPControlBundle extends Bundle {
  val enable = Bool()   // write enable
  val address = UInt(regAddrLen.W)
  val sel = UInt(3.W)
}
