package cpu.pipelinedCache.veri

import chisel3._
import chisel3.util._

class VerificationBRam(depth: Int, width: Int, way: Int, bankIndex: Int) extends Module {
  val addrLen = log2Ceil(depth)

  val io = IO(new Bundle {
    val addr        = Input(UInt(addrLen.W))
    val en          = Input(Bool())
    val writeVector = Input(UInt((width).W))
    val inData      = Input(UInt(width.W))
    val outData     = Output(UInt(width.W))
  })
  val bank             = RegInit(VecInit(Seq.tabulate(depth)(i => (way * 8 + i * 4 + i * 2 + bankIndex).U(32.W))))
  require(depth == 2)
  val readHoldRegister = Reg(UInt(32.W))
  io.outData       := readHoldRegister
  readHoldRegister := bank(io.addr)
  when(io.writeVector =/= 0.U) {
    bank(io.addr) := Cat(
      (3 to 0 by -1).map(i => Mux(io.writeVector(i), io.inData(8 * i + 7, 8 * i), bank(io.addr)(8 * i + 7, 8 * i)))
    )
  }
}
