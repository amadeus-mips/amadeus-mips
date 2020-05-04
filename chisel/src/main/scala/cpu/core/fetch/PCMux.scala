// See README.md for license details.

package cpu.core.fetch

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._
import shared.ValidBundle

class PCMux(n: Int) extends Module {
  val io = IO(new Bundle {
    val ins = Input(Vec(n, new ValidBundle()))

    val pc           = Output(UInt(addrLen.W))
    val pcNotAligned = Output(Bool())
  })

  val pc = RegInit(startPC)

  pc := MuxCase(
    pc + 4.U,
    io.ins.map(in => in.valid -> in.bits)
  )

  io.pc           := pc
  io.pcNotAligned := pc(1, 0).orR()

}
