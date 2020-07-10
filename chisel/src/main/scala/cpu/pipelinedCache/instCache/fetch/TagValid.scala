package cpu.pipelinedCache.instCache.fetch

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.{TagValidBanks, TagValidBundle}

/**
  * fetch the tag and valid bits for all ways
  *
  * @param cacheConfig implicit configuration that governs whole Cache
  */
@chiselName
class TagValid(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val address = Input(UInt(cacheConfig.indexLen.W))
    val write = Flipped(Valid(new Bundle {
      val waySelection   = UInt(log2Ceil(cacheConfig.numOfWays).W)
      val indexSelection = UInt(cacheConfig.indexLen.W)
      val tagValid       = new TagValidBundle
      val writeAllWays   = Bool()
    }))
    val tagValid = Output(Vec(cacheConfig.numOfWays, new TagValidBundle))
  })

  val tagValidBanks = Module(new TagValidBanks)

  //TODO: this currently uses a dual port LUT, change to single port in the future
  for (i <- 0 until cacheConfig.numOfWays) {
    // read tags and valid from tag valid banks
    tagValidBanks.io.way(i).portA.addr := io.address
    io.tagValid(i)                     := tagValidBanks.io.way(i).portA.data

    // write if required
    tagValidBanks.io.way(i).portB.addr          := io.write.bits.indexSelection
    tagValidBanks.io.way(i).portB.writeEnable   := io.write.valid && (io.write.bits.waySelection === i.U || io.write.bits.writeAllWays)
    tagValidBanks.io.way(i).portB.writeData     := io.write.bits.tagValid
  }

}
