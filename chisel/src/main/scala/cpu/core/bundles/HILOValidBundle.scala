// See README.md for license details.

package cpu.core.bundles

import chisel3._
import chisel3.util.ValidIO
import cpu.core.Constants._

/**
 * hi and lo are ValidIO
 */
class HILOValidBundle extends Bundle {
  val hi = new ValidIO(UInt(dataLen.W))
  val lo = new ValidIO(UInt(dataLen.W))
}

/**
 * hi and lo are UInt
 */
class HILOBundle extends Bundle {
  val hi = UInt(dataLen.W)
  val lo = UInt(dataLen.W)
}
