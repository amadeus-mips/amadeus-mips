package cpu.core.bundles

import chisel3._
import cpu.CPUConfig
import cpu.core.Constants.{addrLen, dataLen, exceptAmount}
import shared.ValidBundle

class InstructionFIFOEntry(implicit conf: CPUConfig) extends Bundle {
  val pc          = UInt(addrLen.W)
  val inst        = UInt(dataLen.W)
  val except      = Vec(exceptAmount, Bool())
  val inDelaySlot = Bool()
  val brPredict   = ValidBundle(addrLen)
  val brPrHistory = UInt(conf.branchPredictorHistoryLen.W)
  val instValid   = Bool()

  override def cloneType: InstructionFIFOEntry.this.type = new InstructionFIFOEntry().asInstanceOf[this.type]
}
