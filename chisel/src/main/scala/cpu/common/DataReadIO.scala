// See README.md for license details.

package cpu.common

import chisel3._
import DefaultConfig._

/**
 * (in) .rValid <br/>
 * (in) .data <br/>
 * (out) .enable
 */
class DataReadIO extends Bundle {
  val rValid = Input(Bool())
  val data = Input(UInt(dataLen.W))

  val enable = Output(Bool())

}
