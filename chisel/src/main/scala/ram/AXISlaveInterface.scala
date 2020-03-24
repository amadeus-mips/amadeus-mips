// See README.md for license details.

package ram

import chisel3._
import common.AXIMasterIO

class AXISlaveInterface extends Module {
  val io = IO(new Bundle {
    val bus = Flipped(new AXIMasterIO)
    val ram = new SimpleSramIO
  })
}
