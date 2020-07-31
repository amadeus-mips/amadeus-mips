package cpu.mmu

import chisel3._
import cpu.CPUConfig
import cpu.common.MemReqBundle
import cpu.core.InstFetchIO
import cpu.core.bundles.TLBOpIO

class MMUIO(implicit conf: CPUConfig) extends Bundle {
  class TempIO extends Bundle {
    val rInst = new InstFetchIO
    val memReq = Output(new MemReqBundle)
  }
  val in           = Flipped(new TempIO)
  val out          = new TempIO
  val dataUncached = Output(Bool())
  val core         = Flipped(new TLBOpIO(conf.tlbSize))

  override def cloneType: MMUIO.this.type = new MMUIO().asInstanceOf[this.type ]
}
