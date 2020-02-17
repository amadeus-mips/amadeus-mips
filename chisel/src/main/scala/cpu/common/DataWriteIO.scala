// See README.md for license details.

package cpu.common

import chisel3._
import DefaultConfig._

/**
 * (in) .bValid <br/>
 * (out) .enable <br/>
 * (out) .sel <br/>
 * (out) .data
 */
class DataWriteIO extends Bundle {
  /** write response */
  val bValid = Input(Bool())

  val enable = Output(Bool())
  val sel = Output(UInt(4.W))
  val data = Output(UInt(dataLen.W))
}

