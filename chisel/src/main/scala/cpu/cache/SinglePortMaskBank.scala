package cpu.cache

import chisel3._
import chisel3.util._

/**
  * a single port bank with mask support
  * ex: a cacheline is 16 bytes ( 4 words ) with 8 sets, then numberOfSets = 8, minWidth = 8, maskWidth = 16(this number of sets)
  * default is a 1-word ( 4 bytes ) bank with write mask support, with number of sets of sets
  * @param numberOfSet how many sets there are in this bank
  * @param minWidth what is the minimum addressable width, pinned to 8 because mips is byte aligned
  * @param maskWidth how many minimum width there are in this bank, which is how long the write mask should be
  * @param syncRead is it asynchronous or sync
  */
@deprecated
class SinglePortMaskBank(numberOfSet: Int, minWidth: Int = 8, maskWidth: Int = 4, syncRead: Boolean = true) extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(log2Ceil(numberOfSet).W))
    val we = Input(Bool())
    val writeMask = Input(UInt(maskWidth.W))
    val writeData = Input(UInt((minWidth * maskWidth).W))
    val readData = Output(UInt((minWidth * maskWidth).W))
  })
  val bank = if(syncRead) {
    SyncReadMem(numberOfSet, Vec(maskWidth, UInt(minWidth.W)))
  } else {
    Mem(numberOfSet, Vec(maskWidth, UInt(minWidth.W)))
  }
  // make sure this is inferred as a single port ram
  io.readData := DontCare

  // only use one port ( read/write ) at one time
  when (io.we) {
    bank.write(io.addr, (io.writeData).asTypeOf(Vec(maskWidth, UInt(minWidth.W))), io.writeMask.asBools)
  }.otherwise {
    io.readData := bank.read(io.addr).asUInt()
  }
}
