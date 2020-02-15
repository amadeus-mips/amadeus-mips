// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

class IDEXEBundle extends Bundle {
  val instType = UInt(instTypeLen.W)
  val operation = UInt(opLen.W)
  val op1 = UInt(dataLen.W)
  val op2 = UInt(dataLen.W)
  val write = new WriteControlBundle
  val cp0Control = new CPControlBundle
  val except = Vec(exceptionTypeAmount, Bool())
  val imm26 = UInt(26.W)
  val pcPlus4 = UInt(addrLen.W)
  val inDelaySlot = Bool()
}
