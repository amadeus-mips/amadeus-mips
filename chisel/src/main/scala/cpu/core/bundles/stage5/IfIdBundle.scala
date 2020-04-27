// See README.md for license details.

package cpu.core.bundles.stage5

import chisel3._
import cpu.core.Constants._

class IfIdBundle extends Bundle {
  val pc = UInt(addrLen.W)
  val instValid = Bool()
  val instFetchExcept = Bool()
}
