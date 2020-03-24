// See README.md for license details.

package ram

import chisel3._

class SimpleSramIO extends Bundle {
  val addr = Output(UInt(32.W))
  val ready = Output(Bool())

  val wen = Output(Bool())
  val mask = Output(UInt(4.W))
  val wdata = Output(UInt(32.W))

  val rdata = Input(UInt(32.W))
  val valid = Input(Bool())
}
