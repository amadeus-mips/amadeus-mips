// See README.md for license details.

package cpu.core.bundles.stage

import chisel3._
import chisel3.util.ValidIO
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, CPControlBundle, HILOValidBundle, WriteBundle}

class EXEMEMBundle extends Bundle {
  val write = new WriteBundle
  val operation = UInt(opLen.W)
  val cp0 = new CPBundle
  val hilo = new HILOValidBundle
  val inDelaySlot = Bool()
  val except = Vec(exceptAmount, Bool())
  val pc = UInt(addrLen.W)
  val memAddr = UInt(addrLen.W)
  val memData = UInt(dataLen.W)
}