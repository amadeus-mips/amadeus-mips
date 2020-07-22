package cpu.pipelinedCache.veri

import chisel3._
import chisel3.util._

class VerificationTagLUTRam(depth: Int, width: Int) extends Module {
  require(isPow2(depth))
  val addrLen = log2Ceil(depth)
  val io = IO(new Bundle {
    val readAddr = Input(UInt(addrLen.W))
    val readData = Output(UInt(width.W))

    val writeAddr   = Input(UInt(addrLen.W))
    val writeData   = Input(UInt(width.W))
    val writeEnable = Input(Bool())
    val writeOutput = Output(UInt(width.W))
  })

}
