package cpu.pipelinedCache.veri

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.CPUConfig
import cpu.common.MemReqBundle
import cpu.pipelinedCache.DataCache
import firrtl.options.TargetDirAnnotation
import verification.SymbiyosysAXIRam

class VeriDCache extends Module {
  val io = IO(new Bundle {
    val request = Flipped(Decoupled(new MemReqBundle))
    val data = Valid(UInt(32.W))
    val cacheInstruction = Flipped(Decoupled(UInt(0.W)))
  })
  implicit val cpuConfig:   CPUConfig   = new CPUConfig(build = false, verification = true)
  val dcache  = Module(new DataCache(cpuConfig.dCacheConf))
  val veriRam = Module(new SymbiyosysAXIRam)
  dcache.io.cacheInstruction <> io.cacheInstruction
  dcache.io.axi     <> veriRam.io.axi
  dcache.io.request <> io.request
  io.data.valid     := dcache.io.commit
  io.data.bits      := dcache.io.readData
}

object VeriDCacheElaborate extends App {
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new VeriDCache), TargetDirAnnotation("verification"))
  )
}
