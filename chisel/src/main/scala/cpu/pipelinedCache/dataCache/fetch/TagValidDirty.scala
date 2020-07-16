package cpu.pipelinedCache.dataCache.fetch

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.metaBanks.{TagValidBanks, TagValidBundle, TagValidDirtyBundle}

/**
  * tag valid dirty unit for data cache
  */
@chiselName
class TagValidDirty(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val index = Input(UInt(cacheConfig.indexLen.W))
    val write = Flipped(Valid(new Bundle {
      val waySelection   = UInt(log2Ceil(cacheConfig.numOfWays).W)
      val indexSelection = UInt(cacheConfig.indexLen.W)
      val tagValid       = new TagValidBundle
    }))
    val tagValidDirty = Output(Vec(cacheConfig.numOfWays, new TagValidDirtyBundle))
  })

  val tagValidDirtyBanks = Module(new TagValidBanks())

  for (i <- 0 until cacheConfig.numOfWays) {
    tagValidDirtyBanks.io.way(i).portA.addr := io.index
    io.tagValidDirty(i)                     := tagValidDirtyBanks.io.way(i).portA.data

    tagValidDirtyBanks.io.way(i).portB.addr        := io.write.bits.indexSelection
    tagValidDirtyBanks.io.way(i).portB.writeEnable := io.write.valid && io.write.bits.waySelection === 1.U
    tagValidDirtyBanks.io.way(i).portB.writeData   := io.write.bits.tagValid
  }
}
