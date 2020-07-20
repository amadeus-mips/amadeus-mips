package cpu.pipelinedCache.veri

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.{CacheConfig, InstrCache}
import firrtl.options.TargetDirAnnotation
import verification.SymbiyosysAXIRam

class ICacheVeri(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val addr = Flipped(Decoupled(UInt(32.W)))
    val data = Decoupled(UInt(32.W))
  })
  val insCache = Module(new InstrCache)
  val ram      = Module(new SymbiyosysAXIRam)
  insCache.io.axi   <> ram.io.axi
  insCache.io.addr  <> io.addr
  insCache.io.invalidateIndex.bits := DontCare
  insCache.io.invalidateIndex.valid := false.B
  insCache.io.data  <> io.data
  insCache.io.flush <> false.B
}

object ICacheElaborate extends App {
  implicit val cacheConfig = new CacheConfig
  implicit val CPUConfig   = new CPUConfig(build = false)

  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new ICacheVeri()), TargetDirAnnotation("verification"))
  )
}
