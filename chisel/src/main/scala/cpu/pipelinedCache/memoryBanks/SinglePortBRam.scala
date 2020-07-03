package cpu.pipelinedCache.memoryBanks

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.memoryBanks.memip.SinglePortRamIP
import firrtl.options.TargetDirAnnotation

@chiselName
class SinglePortBRam(depth: Int, width: Int = 32, byteAddressable: Boolean)(implicit
                                                                            cpuCfg: CPUConfig = new CPUConfig(build = false)
) extends Module {
  require(isPow2(depth))
  require(
    width % 8 == 0 || !byteAddressable,
    "if memory is byte addressable, then the adderss width must be a multiple of 8"
  )
  val addrLen = log2Ceil(depth)

  val io = IO(new Bundle {
    val addr = Input(UInt(addrLen.W))
    val en = Input(Bool())
    val writeVector = Input(UInt((if (byteAddressable) width / 8 else 1).W))
    val inData = Input(UInt(width.W))
    val outData = Output(UInt(width.W))
  })

  if (cpuCfg.build) {
    val bank = Module(
      new SinglePortRamIP(
        dataWidth = width,
        byteWriteWidth = if (byteAddressable) 8 else width,
        addrWidth = addrLen,
        numberOfLines = depth
      )
    )
    bank.io.clka := clock
    bank.io.rsta := reset
    bank.io.addra := io.addr
    bank.io.dina := io.inData
    bank.io.ena := io.en
    bank.io.wea := io.writeVector
    io.outData := bank.io.douta
    bank.io.regcea := false.B
  } else {
    if (byteAddressable) {
      val bank = SyncReadMem(depth, Vec(width / 8, UInt(8.W)))
      io.outData := DontCare
      when(io.en) {
        when(io.writeVector.orR) {
          bank.write(io.addr, io.inData.asTypeOf(Vec(width / 8, UInt(8.W))), io.writeVector.asBools())
        }.otherwise {
          io.outData := bank(io.addr).asUInt
        }
      }
    } else {
      val bank = SyncReadMem(depth, UInt(width.W))
      io.outData := DontCare
      when(io.en) {
        when(io.writeVector.asBool) {
          bank(io.addr) := io.inData
        }.otherwise {
          io.outData := bank(io.addr).asUInt
        }
      }
    }
  }

}

object SinglePortBRamElaborate extends App {
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new SinglePortBRam(32, 32, false)), TargetDirAnnotation("generation"))
  )
}
