package cpu.core.bundles.stages

import chisel3._
import cpu.core.Constants._

class IfIf1Bundle extends Bundle{
  val pc          = UInt(addrLen.W)
  val instValid   = Bool()
  val except      = Vec(exceptAmount, Bool())
  val inDelaySlot = Bool()
}
