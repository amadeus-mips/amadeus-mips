// See README.md for license details.

package cpu.core.components

import chisel3._
import chisel3.util.MuxCase
import cpu.core.bundles.WriteBundle
import cpu.core.Constants._
class RegFile extends Module {
  val io = IO(new Bundle {
    val write = Input(new WriteBundle) // from `WriteBack`

    val rs = Input(UInt(regAddrLen.W))  // from `Decode`
    val rt = Input(UInt(regAddrLen.W))  // ^

    val rsData = Output(UInt(dataLen.W))
    val rtData = Output(UInt(dataLen.W))
  })

  /** construct 32 32-bit reg */
  val regs = RegInit(VecInit(Seq.fill(regAmount)(0.U(dataLen.W))))

  when(io.write.enable && io.write.address =/= 0.U) {
    regs(io.write.address) := io.write.data
  }

  io.rsData := MuxCase(regs(io.rs),
    Array(
      (io.rs === 0.U) -> 0.U,
      (io.write.enable && io.write.address === io.rs) ->
        io.write.data,
    )
  )
  io.rtData := MuxCase(regs(io.rt),
    Array(
      (io.rt === 0.U) -> 0.U,
      (io.write.enable && io.write.address === io.rt) ->
        io.write.data,
    )
  )

}
