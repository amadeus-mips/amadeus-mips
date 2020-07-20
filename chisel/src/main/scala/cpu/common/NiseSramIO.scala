// See README.md for license details.

package cpu.common

import chisel3._

/**
  * master IO. <br/>
  * out: .bankIndex, .enable <br/>
  * in: .data, .valid
  */
class NiseSramReadIO extends Bundle {
  val addr = Output(UInt(32.W))
  val enable = Output(Bool())

  val data = Input(UInt(32.W))
  val valid = Input(Bool())
}

/**
  * master IO <br/>
  * out: .bankIndex, .enable, .sel, .data <br/>
  * in: .valid
  */
class NiseSramWriteIO extends Bundle {
  val addr = Output(UInt(32.W))
  val enable = Output(Bool())
  val sel = Output(UInt(4.W))
  val data = Output(UInt(32.W))

  val valid = Input(Bool())
}
