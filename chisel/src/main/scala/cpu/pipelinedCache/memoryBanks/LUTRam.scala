package cpu.pipelinedCache.memoryBanks

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.memoryBanks.memip.LUTRamIP
import firrtl.options.TargetDirAnnotation

/**
  * LUT ram for XPM, one port for read/write, one port for read
  * @param depth how many lines there are in the bank
  * @param width how wide in bits each line is
  * @param cpuCFG implicit configuration to control generate ram for simulation or elaboration
  */
@chiselName
class LUTRam(depth: Int, width: Int)(implicit cpuCFG: CPUConfig = CPUConfig.Build) extends Module {
  require(isPow2(depth))
  val addrLen = log2Ceil(depth)
  val io = IO(new Bundle {
    val readAddr = Input(UInt(addrLen.W))
    val readData = Output(UInt(width.W))

    val writeAddr   = Input(UInt(addrLen.W))
    val writeData   = Input(UInt(width.W))
    val writeEnable = Input(Bool())
    val writeOutput = Output(UInt(width.W))
  })

  if (cpuCFG.build) {
    val bank = Module(
      new LUTRamIP(dataWidth = width, addrWidth = addrLen, byteWriteWidth = width, numberOfLines = depth)
    )
    bank.io.clka := clock
    bank.io.clkb := clock
    bank.io.rsta := reset
    bank.io.rstb := reset

    bank.io.regcea := false.B
    bank.io.regceb := false.B
    bank.io.ena    := true.B
    bank.io.enb    := true.B

    bank.io.addra  := io.writeAddr
    bank.io.wea    := io.writeEnable
    bank.io.dina   := io.writeData
    io.writeOutput := bank.io.douta

    bank.io.addrb := io.readAddr
    io.readData   := bank.io.doutb
  } else {
    assert(
      !(io.writeEnable && io.readAddr === io.writeAddr),
      s"there has been an address collision, the address is ${io.readAddr}"
    )
    val bank = SyncReadMem(depth, UInt(width.W))
    io.readData    := bank(io.readAddr)
    io.writeOutput := DontCare
    when(io.writeEnable) {
      bank(io.writeAddr) := io.writeData
    }.otherwise {
      io.writeOutput := bank(io.writeAddr)
    }
  }
}

object LUTRamElaborate extends App {
  val conf = new CPUConfig(build = true)
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new LUTRam(32, 32)), TargetDirAnnotation("generation"))
  )
}
