// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.{Fill, MuxCase}
import cpu.core.Constants._

class Hazard extends Module {
  val io = IO(new Bundle {
    val except   = Input(Vec(exceptAmount, Bool()))
    val EPC      = Input(UInt(dataLen.W))
    val stallReq = Input(Vec(5, Bool()))

    val flush   = Output(Bool())
    val flushPC = Output(UInt(dataLen.W))
    val stall   = Output(UInt(cpuStallLen.W))
  })

  val hasExcept = io.except.asUInt().orR()

  io.flush   := hasExcept
  io.flushPC := Mux(io.except(EXCEPT_ERET), io.EPC, exceptPC)

  io.stall := MuxCase(
    0.U,
    Array(
      hasExcept -> 0.U
    ) ++ io.stallReq.zipWithIndex.reverse.map(zip => {
      val (sr, i) = zip
      sr -> Fill(i + 2, true.B)
    })
  )

}
