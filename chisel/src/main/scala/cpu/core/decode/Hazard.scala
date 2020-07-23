// See README.md for license details.

package cpu.core.decode

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._
import cpu.core.bundles.WriteBundle

/**
  * Handle hazard
  */
class Hazard(n: Int) extends Module {
  val io = IO(new Bundle {
    val wrs = Input(Vec(n, new WriteBundle))

    val ops = Vec(2, new Bundle() {
      val addr    = Input(UInt(regAddrLen.W))
      val inData  = Input(UInt(dataLen.W))
      val typ     = Input(UInt(1.W))
      val outData = Output(UInt(dataLen.W))
    })

    val stallReq = Output(Bool())
  })

  val stalls = Wire(Vec(2, Bool()))
  io.stallReq := stalls.asUInt().orR()

  for ((op, stall) <- io.ops.zip(stalls)) {
    op.outData := MuxCase(
      op.inData,
      (!op.addr.orR() -> 0.U) +: io.wrs.map(wr => (wr.enable && wr.address === op.addr) -> wr.data)
    )
    stall := op.typ === OPn_RF && MuxCase(
      false.B,
      (!op.addr.orR() -> 0.B) +: io.wrs.map(wr => (wr.enable && wr.address === op.addr) -> !wr.valid)
    )
  }

}
