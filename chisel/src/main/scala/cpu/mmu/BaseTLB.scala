package cpu.mmu

import chisel3._
import chisel3.util._

abstract class BaseTLB(numOfReadPorts: Int, tlbSize: Int) extends Module {
  require(isPow2(tlbSize), "TLB size should be a power of 2")
  val io = IO(new Bundle {
    val asid          = Input(UInt(8.W))
    val kseg0Uncached = Input(Bool())
    val query         = Input(Vec(numOfReadPorts, new TLBQuery()))
    val result        = Output(Vec(numOfReadPorts, new TLBResult()))

    // there should be only 1 operation port that can handle both read and write
    val instrReq = Input(new TLBRWReq(tlbSize))
    val readResp = Output(new TLBEntry)

    // the probing instruction
    val probeReq  = Input(UInt(19.W))
    val probeResp = Output(UInt(32.W))
  })
}
