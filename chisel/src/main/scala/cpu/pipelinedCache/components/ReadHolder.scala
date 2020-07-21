package cpu.pipelinedCache.components

import chisel3._
import chisel3.util._

class ReadHolder extends Module {
  val io = IO(new Bundle {

    /** when input is valid, there is a valid/not ready scenario */
    val input = Flipped(Valid(UInt(32.W)))

    /** when output is valid, read from here */
    val output = Valid(UInt(32.W))
  })

  val instruction = RegInit("hdeadbeef".U(32.W))
  val valid       = RegInit(false.B)

  instruction     := io.input.bits
  io.output.bits  := instruction
  valid           := io.input.valid
  io.output.valid := valid
}
