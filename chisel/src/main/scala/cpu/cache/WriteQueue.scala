package cpu.cache

import chisel3._
import chisel3.util._
import shared.AXIIO

class WriteQueue(lineWidth: Int) extends Module {
  val io = IO(new Bundle {
    val writeData = Input(UInt(lineWidth.W))

    val axi = AXIIO.master()
  })
}
