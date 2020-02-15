// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

class EXEMEMBundle extends Bundle {
  val write = new WriteBundle
  val cp0 = new CPBundle
  val hilo = new HILOValidBundle
  val inDelaySlot = Bool()
  val except = Vec(exceptionTypeAmount, Bool())
  val pcPlus4 = UInt(addrLen.W)
  val memAddr = UInt(addrLen.W)
  val memData = UInt(dataLen.W)
}
