// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._

class CTRL extends Module {
  val io = IO(new Bundle {
    val except = Input(Vec(exceptAmount, Bool()))
    val EPC = Input(UInt(dataLen.W))
    val stallReqFromFetch = Input(Bool())
    val stallReqFromDecode = Input(Bool())
    val stallReqFromExecute = Input(Bool())
    val stallReqFromMemory = Input(Bool())

    val flush = Output(Bool())
    val flushPC = Output(UInt(dataLen.W))
    val stall = Output(UInt(cpuStallLen.W))
  })

  io.flush := io.except.asUInt() =/= 0.U
  io.flushPC := Mux(io.except(EXCEPT_ERET), io.EPC, exceptPC)

  io.stall := MuxCase(0.U,
    Array(
      (io.except.asUInt() =/= 0.U) -> 0.U,
      io.stallReqFromMemory   -> "b011111".U,
      io.stallReqFromExecute  -> "b001111".U,
      io.stallReqFromDecode   -> "b000111".U,
      io.stallReqFromFetch    -> "b000111".U
    )
  )

}
