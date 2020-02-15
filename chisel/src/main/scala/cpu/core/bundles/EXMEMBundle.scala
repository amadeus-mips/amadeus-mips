// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

class EXMEMBundle extends Bundle {
  val write = new WriteBundle
}
