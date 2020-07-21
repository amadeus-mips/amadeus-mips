// See README.md for license details.

package cpu.core.fetch

import chisel3._
import chisel3.util.MuxCase
import cpu.CPUConfig
import cpu.core.Constants._
import shared.ValidBundle

class PCMux(n: Int)(implicit val cpuConf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val ins = Input(Vec(n, ValidBundle(32)))

    val pc = Output(UInt(addrLen.W))

    val pcCacheCorner = Output(Bool())
    val pcNotAligned  = Output(Bool())
  })

  val pc = RegInit(startPC)

  val pcCacheBankOffset = pc(cpuConf.iCacheConf.bankOffsetLen + 1, 2)
  val isCacheCornerPc   = (cpuConf.iCacheConf.numOfBanks - 1).U === pcCacheBankOffset

  pc := MuxCase(
    Mux(isCacheCornerPc, pc + 4.U, pc + 8.U),
    io.ins.map(in => in.valid -> in.bits)
  )

  io.pc := pc

  io.pcCacheCorner := isCacheCornerPc
  io.pcNotAligned  := pc(1, 0).orR()
}
