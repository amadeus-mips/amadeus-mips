// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._
import shared.ValidBundle

/**
 * hi and lo are ValidIO
 */
class HILOValidBundle extends Bundle {
  val hi = ValidBundle(32)
  val lo = ValidBundle(32)
}

/**
 * hi and lo are UInt
 */
class HILOBundle extends Bundle {
  val hi = UInt(dataLen.W)
  val lo = UInt(dataLen.W)
}
