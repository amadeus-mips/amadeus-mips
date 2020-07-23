// See README.md for license details.

package cpu.core.bundles.stages

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, HILOValidBundle, TLBReadBundle, WriteBundle}

//noinspection DuplicatedCode
class Mem0Mem1Bundle extends Bundle {
  val addrL2   = UInt(2.W)
  val op       = UInt(opLen.W)
  val write    = new WriteBundle
  val pc       = UInt(dataLen.W)
  val uncached = Bool()
  val valid    = Bool()
}
