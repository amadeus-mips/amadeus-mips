// See README.md for license details.

package shared

import chisel3._

class Buffer(len: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(len.W))
    val en = Input(Bool())
    val out = Output(UInt(len.W))
  })

  val buffer = RegInit(0.U.asTypeOf(new ValidBundle(len)))
  buffer.valid := io.en
  buffer.bits := Mux(buffer.valid, buffer.bits, io.in)
  io.out := Mux(buffer.valid, buffer.bits, io.in)
}
