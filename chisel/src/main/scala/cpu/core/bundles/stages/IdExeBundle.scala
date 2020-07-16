// See README.md for license details.

package cpu.core.bundles.stages

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, WriteBundle}
import shared.ValidBundle

class IdExeBundle extends Bundle {
  val instType    = UInt(instTypeLen.W)
  val operation   = UInt(opLen.W)
  val op1         = UInt(dataLen.W)
  val op2         = UInt(dataLen.W)
  val write       = new WriteBundle
  val cp0         = new CPBundle
  val except      = Vec(exceptAmount, Bool())
  val imm26       = UInt(26.W)
  val pc          = UInt(addrLen.W)
  val inDelaySlot = Bool()
  val brPredict   = new ValidBundle()
  val instValid   = Bool()
}
