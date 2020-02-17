// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

class ValidBundle extends Bundle {
  val valid = Bool()
  val bits = UInt(dataLen.W)
}
