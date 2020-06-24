// See README.md for license details.

package cpu.core.components

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.HILOValidBundle

class MultDivIO extends Bundle {
  val enable = Input(Bool())
  val flush = Input(Bool())
  val op1 = Input(UInt(dataLen.W))
  val op2 = Input(UInt(dataLen.W))
  val signed = Input(Bool())

  val result = Output(new HILOValidBundle)
}
