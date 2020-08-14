// See README.md for license details.

package cpu.core.decode

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._
import cpu.core.bundles.WriteBundle
import shared.ValidBundle

/**
  * Handle hazard
  */
class Hazard(n: Int) extends Module {
  val io = IO(new Bundle {
    val wrs = Input(Vec(n, new WriteBundle))

    val ops = Vec(
      2,
      new Bundle() {
        val addr    = Input(UInt(regAddrLen.W))
        val inData  = Input(UInt(dataLen.W))
        val outData = Output(new ValidBundle)
      }
    )
  })

  io.ops.foreach(op => {
    op.outData.bits := MuxCase(
      op.inData,
      (!op.addr.orR() -> 0.U) +: io.wrs.map(wr => (wr.enable && wr.address === op.addr) -> wr.data)
    )
    op.outData.valid := MuxCase(
      true.B,
      (!op.addr.orR() -> true.B) +: io.wrs.map(wr => (wr.enable && wr.address === op.addr) -> wr.valid)
    )
  })

}
