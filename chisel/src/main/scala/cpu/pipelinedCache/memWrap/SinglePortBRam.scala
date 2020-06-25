package cpu.pipelinedCache.memWrap

import chisel3._
import chisel3.util.{isPow2, log2Ceil}
import cpu.pipelinedCache.memip.SinglePortRam

class SinglePortBRam(depth: Int, width: Int) extends Module {
  require(isPow2(depth))

  val addrLen = log2Ceil(depth)
  val io = IO(new Bundle {
    val addr = Input(UInt(addrLen.W))
    val en = Input(Bool())
    val we = Input(Bool())
    val inData = Input(UInt(width.W))
    val outData = Output(UInt(width.W))
  })
  val bank = Module(new SinglePortRam(dataWidth = width, byteWriteWidth = width, addrWidth = addrLen, numberOfLines = depth))
  bank.io.clka := clock
  bank.io.rsta := reset
  bank.io.addra := io.addr
  bank.io.dina := io.inData
  bank.io.ena := io.en
  bank.io.wea := io.we
  io.outData := bank.io.douta
  bank.io.regcea := false.B
}
