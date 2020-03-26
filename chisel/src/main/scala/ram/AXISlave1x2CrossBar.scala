// See README.md for license details.
package ram

import chisel3._
import common.{AXIMasterIO, Constants}

class AXISlave1x2CrossBar extends Module {
  val io = IO(new Bundle {
    val bus = Flipped(new AXIMasterIO)
    val iram = new SimpleSramIO
    val dram = new SimpleSramIO
  })

  val iCon = Module(new AXISlaveToSram(Constants.INST_ID, 10))
  val dCon = Module(new AXISlaveToSram(Constants.DATA_ID, 10))

  // bus hasn't connect
  ???

  io.iram <> iCon.io.ram
  io.dram <> dCon.io.ram
}
