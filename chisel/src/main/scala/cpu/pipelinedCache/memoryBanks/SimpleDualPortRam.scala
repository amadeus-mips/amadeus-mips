package cpu.pipelinedCache.memoryBanks

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.memoryBanks.memip.SDPRamIP
import firrtl.options.TargetDirAnnotation

/**
  * simple dual port ram, with a port for reading and a port for writing
  *
  * @param depth           how many lines there are in the ram
  * @param width           how wide in bits each line is
  * @param byteAddressable is it byte addressable?
  * @param cpuCfg          the implicit configuration for simulation and elaboration
  */
@chiselName
class SimpleDualPortRam(depth: Int, width: Int, byteAddressable: Boolean)(implicit
                                                                          cpuCfg: CPUConfig = CPUConfig.Build
) extends Module {
  require(isPow2(depth))
  require(
    width % 8 == 0 || !byteAddressable,
    "if memory is byte addressable, then the adderss width must be a multiple of 8"
  )
  val addrLen = log2Ceil(depth)

  val io = IO(new Bundle {
    val addrR = Input(UInt(addrLen.W))
    val enR = Input(Bool())
    val dataR = Output(UInt(width.W))

    val addrW = Input(UInt(addrLen.W))
    val enW = Input(Bool())
    val writeVector = Input(UInt((if (byteAddressable) width / 8 else 1).W))
    val dataW = Input(UInt(width.W))
  })

  if (cpuCfg.build) {
    val memory = Module(
      new SDPRamIP(
        dataWidth = width,
        byteWriteWidth = if (byteAddressable) 8 else width,
        numberOfLines = depth,
        addrWidth = addrLen
      )
    )
    memory.io.clka := clock
    memory.io.clkb := clock
    memory.io.rsta := reset
    memory.io.rstb := reset

    memory.io.addra := io.addrW
    memory.io.ena := io.enW
    memory.io.dina := io.dataW
    memory.io.wea := io.writeVector

    memory.io.addrb := io.addrR
    memory.io.enb := io.enR
    memory.io.regceb := false.B
    io.dataR := memory.io.doutb
  } else {
    assert(io.writeVector.orR || !io.enW, "when write port enable is high, write vector cannot be all 0")
    assert(!(io.enR && io.enW && io.addrR === io.addrW), s"there should not have been an request collision, the request is ${io.addrR}")
    if (byteAddressable) {
      val bank = SyncReadMem(depth, Vec(width / 8, UInt(8.W)))
      when(io.enR) {
        io.dataR := bank(io.addrR)
      }
      when(io.enW) {
        bank.write(io.addrW, io.dataW.asTypeOf(Vec(width / 8, UInt(8.W))), io.writeVector.asBools)
      }
    } else {
      val bank = SyncReadMem(depth, UInt(width.W))

      when(io.enR) {
        io.dataR := bank(io.addrR)
      }
      when(io.enW) {
        bank(io.addrW) := io.dataW
      }
    }
  }
}

object SimpleDualPortRamEla extends App {
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new SimpleDualPortRam(32, 32, false)), TargetDirAnnotation("generation"))
  )
}
