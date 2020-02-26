// See README.md for license details.

package cpu.core.bundles.stage

import chisel3._
import cpu.core.Constants._

class IFIDBundle extends Bundle {
  val pc = UInt(addrLen.W)
  val instValid = Bool()
  val instFetchExcept = Bool()
}
