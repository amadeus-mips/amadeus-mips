// See README.md for license details.

package cpu.core.bundles.stages

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, WriteBundle}
import shared.ValidBundle

class IdExeBundle extends Bundle {
  val instType    = UInt(instTypeLen.W)
  val operation   = UInt(opLen.W)
  val rs          = ValidBundle(5)
  val rt          = ValidBundle(5)
  val imm         = UInt(32.W)
  val write       = new WriteBundle
  val cp0         = new CPBundle
  val except      = Vec(exceptAmount, Bool())
  val imm26       = UInt(26.W)
  val pc          = UInt(addrLen.W)
  val inDelaySlot = Bool()
  val brPredict   = ValidBundle(32)
  val instValid   = Bool()
}
