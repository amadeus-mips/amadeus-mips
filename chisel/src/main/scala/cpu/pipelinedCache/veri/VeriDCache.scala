package cpu.pipelinedCache.veri

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.dataCache.DataCache
import verification.VeriAXIRam

class VeriDCache extends Module {
  val io = IO(new Bundle {
    val request = Flipped(Decoupled(new Bundle {
      val address   = UInt(32.W)
      val writeMask = UInt(4.W)
      val writeData = UInt(32.W)
    }))
    val dataOutput = Output(UInt(32.W))
    val dataValid  = Output(Bool())
  })
  implicit val cacheConfig: CacheConfig = new CacheConfig
  implicit val cpuConfig:   CPUConfig   = new CPUConfig(build = false)
  val dcache  = Module(new DataCache)
  val veriRam = Module(new VeriAXIRam)
  dcache.io.axi     <> veriRam.io.axi
  dcache.io.request <> io.request
  io.dataValid      := dcache.io.commit
  io.dataOutput     := dcache.io.readData
}
