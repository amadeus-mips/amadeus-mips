// See README.md for license details.

package memory.axi

import chisel3._
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}

class SimpleSramIO extends Bundle {
  val read = new NiseSramReadIO
  val write = new NiseSramWriteIO
}
