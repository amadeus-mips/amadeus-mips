// See README.md for license details.

package cpu.core.components

import chisel3._
import chisel3.util.MuxCase
import cpu.CPUConfig
import cpu.core.bundles.WriteBundle
import cpu.core.Constants._

class RegFileReadIO extends Bundle {
  val rs = Input(UInt(regAddrLen.W))  // from `Decode`
  val rt = Input(UInt(regAddrLen.W))  // ^

  val rsData = Output(UInt(dataLen.W))
  val rtData = Output(UInt(dataLen.W))
}

class RegFile(implicit c: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val write = Input(Vec(c.decodeWidth, new WriteBundle)) // from `WriteBack`
    val read = Vec(c.decodeWidth + c.decodeBufferNum, new RegFileReadIO)
  })

  /** construct 32 32-bit reg */
  val regs = RegInit(VecInit(Seq.fill(regAmount)(0.U(dataLen.W))))

  when(io.write(0).enable && io.write(0).address =/= 0.U) {
    regs(io.write(0).address) := io.write(0).data
  }
  when(io.write(1).enable && io.write(1).address =/= 0.U) {
    regs(io.write(1).address) := io.write(1).data
  }

  io.read.foreach(r => {
    r.rsData := Mux(r.rs.orR(), regs(r.rs), 0.U)
    r.rtData := Mux(r.rt.orR(), regs(r.rt), 0.U)
  })
}
