package cpu.core.bundles.stages

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, HILOValidBundle, TLBReadBundle, WriteBundle}

class Mem2WbBundle extends Bundle {
  val op    = UInt(opLen.W)
  val write = new WriteBundle
  val pc    = UInt(dataLen.W)
}
