// See README.md for license details.

package cpu.core.bundles.stage

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.{CPControlBundle, WriteControlBundle}

class IDEXEBundle extends Bundle {
  val instType = UInt(instTypeLen.W)
  val operation = UInt(opLen.W)
  val op1 = UInt(dataLen.W)
  val op2 = UInt(dataLen.W)
  val write = new WriteControlBundle
  val cp0Control = new CPControlBundle
  val except = Vec(exceptAmount, Bool())
  val imm26 = UInt(26.W)
  val pc = UInt(addrLen.W)
  val inDelaySlot = Bool()
}
