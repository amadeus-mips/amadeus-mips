// See README.md for license details.

package cpu.core.bundles.stage

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, HILOValidBundle, WriteBundle}

class MEMWBBundle extends Bundle {
  val write = new WriteBundle
  val cp0 = new CPBundle
  val hilo = new HILOValidBundle
  val pc = UInt(dataLen.W)
}
