// See README.md for license details.

package cpu.core.bundles.stages

import chisel3._
import cpu.core.Constants._
import shared.ValidBundle

class If1IdBundle extends Bundle {
  val pc          = UInt(addrLen.W)
  val inst        = UInt(dataLen.W)
  val instValid   = Bool()
  val except      = Vec(exceptAmount, Bool())
  val inDelaySlot = Bool()
  val brPredict   = new ValidBundle()
}
