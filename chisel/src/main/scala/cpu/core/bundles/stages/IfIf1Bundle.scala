package cpu.core.bundles.stages

import chisel3._
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.components.BranchPredictorEntryBundle

class IfIf1Bundle(implicit conf: CPUConfig) extends Bundle {
  val pc = UInt(addrLen.W)

  /**
    * whether the instruction is valid.
    * @note
    *       It is possible that when pc is valid, instruction is invalid.
    *       Exception may cause this.
    */
  val instValid   = Bool()
  val except      = Vec(exceptAmount, Bool())
  val inDelaySlot = Bool()
  val brPredict   = Vec(conf.fetchAmount, new BranchPredictorEntryBundle(conf.branchPredictorType))
  val validPcMask = Vec(conf.fetchAmount, Bool())

  override def cloneType: IfIf1Bundle.this.type = new IfIf1Bundle().asInstanceOf[this.type]
}
