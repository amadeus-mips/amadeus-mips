// See README.md for license details.

package cpu.cache

import chisel3._
import chisel3.util._

class SinglePortBank(depth: Int, width: Int, syncRead: Boolean = true, dataC: Boolean = false) extends Module {
  require(isPow2(depth))
  val addrLen = log2Ceil(depth)
  val io = IO(new Bundle {
    val addr = Input(UInt(addrLen.W))
    val en = if(syncRead) Some(Input(Bool())) else None
    val we = Input(Bool())
    val inData = Input(UInt(width.W))
    val outData = Output(UInt(width.W))
  })
  if(syncRead) {
    val bank = SyncReadMem(depth, UInt(width.W))
    io.outData := DontCare
    when(io.en.get) {
      val rdwrPort = bank(io.addr)
      when(io.we) {
        rdwrPort := io.inData
      }.otherwise {
        io.outData := rdwrPort
      }
    }
  } else {
    val bank = Mem(depth, UInt(width.W))
    when(io.we) {
      bank.write(io.addr, io.inData)
    }
    io.outData := bank.read(io.addr)
  }

  override def desiredName: String = if(dataC) "SinglePortBank_d" else super.desiredName
}
