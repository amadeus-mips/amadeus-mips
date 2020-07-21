package cpu.core.bundles.stages

import chisel3._
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.components.BranchPredictorEntry
import shared.ValidBundle

class IfIf1Bundle(implicit conf: CPUConfig) extends Bundle {
  val pc          = UInt(addrLen.W)
  val instValid   = Bool()
  val except      = Vec(exceptAmount, Bool())
  val inDelaySlot = Bool()
  val brPredict   = Vec(conf.fetchAmount, ValidBundle(new BranchPredictorEntry(conf.branchPredictorType)))
  val validPcMask = Vec(conf.fetchAmount, Bool())

  override def cloneType: IfIf1Bundle.this.type = new IfIf1Bundle().asInstanceOf[this.type ]
}
