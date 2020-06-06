// See README.md for license details.

package cpu.core.bundles.stages

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, HILOValidBundle, TLBReadBundle, WriteBundle}

class MemWbBundle extends Bundle {
  val addrL2    = UInt(2.W)
  val operation = UInt(opLen.W)
  val tlb       = new TLBReadBundle
  val write     = new WriteBundle
  val cp0       = new CPBundle
  val hilo      = new HILOValidBundle
  val pc        = UInt(dataLen.W)
}
