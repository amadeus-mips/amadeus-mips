// See README.md for license details.

package cpu.core.bundles.stages

import chisel3._
import cpu.CPUConfig
import cpu.core.Constants._
import shared.ValidBundle
import shared.bundles.Dual

class If1IdBundle(implicit conf: CPUConfig) extends Bundle {
  val pc          = UInt(addrLen.W)
  val pcOne       = Bool()
  val inst        = Vec(conf.fetchAmount, UInt(dataLen.W))
  val instValid   = Bool()
  val except      = Vec(exceptAmount, Bool())
  val inDelaySlot = Dual(Bool())
  val brPredict   = Vec(conf.fetchAmount, ValidBundle(UInt(32.W)))
}
