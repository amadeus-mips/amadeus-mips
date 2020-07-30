package cpu.pipelinedCache.veri

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.{CacheConfig, InstrCache}
import firrtl.options.TargetDirAnnotation
import verification.SymbiyosysAXIRam

class ICacheVeri(implicit CPUConfig: CPUConfig) extends Module {
  val cacheConfig = CPUConfig.iCacheConf
  val io = IO(new Bundle {
    val addr = Flipped(Decoupled(Vec(cacheConfig.numOfBanks, UInt((cacheConfig.bankWidth * 8).W))))
    val data = Decoupled(Vec(cacheConfig.numOfBanks, UInt((cacheConfig.bankWidth * 8).W)))
  })
  val insCache = Module(new InstrCache(CPUConfig.iCacheConfig))
  val ram      = Module(new SymbiyosysAXIRam)
  insCache.io.axi                   <> ram.io.axi
  insCache.io.fetchIO.addr          <> io.addr
  insCache.io.invalidateIndex.bits  := DontCare
  insCache.io.invalidateIndex.valid := false.B
  insCache.io.fetchIO.data          <> io.data
  insCache.io.fetchIO.change        := false.B
}

object ICacheElaborate extends App {
  implicit val cacheConfig = new CacheConfig
  implicit val CPUConfig   = new CPUConfig(build = false)

  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new ICacheVeri()), TargetDirAnnotation("verification"))
  )
}
