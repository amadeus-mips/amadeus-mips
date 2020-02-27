// See README.md for license details.

package cpu.cache

import chisel3._

@deprecated
class ICacheBram extends BlackBox {
  val io = IO(new Bundle {
    val addra = Input(UInt(7.W))
    val clka = Input(Clock())
    val dina = Input(UInt(32.W))
    val douta = Output(UInt(32.W))
    val wea = Input(Bool())
  })

//  override def desiredName: String = "blk_mem_gen_0"
}
