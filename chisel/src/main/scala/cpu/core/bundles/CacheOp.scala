package cpu.core.bundles

import chisel3._

class CacheOp extends Bundle {
  val target = UInt(2.W)
  val valid = Bool()
}
