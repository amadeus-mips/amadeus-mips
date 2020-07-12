package cpu.pipelinedCache.dataCache

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.TagValidDirtyBundle
import cpu.pipelinedCache.dataCache.fetch.TagValidDirty

@chiselName
class Fetch(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(32.W))
    val writeTagValidDirty = Flipped(Valid(new Bundle {
      val waySelection   = UInt(log2Ceil(cacheConfig.numOfWays).W)
      val indexSelection = UInt(cacheConfig.indexLen.W)
      val tagValid       = new TagValidDirtyBundle
    }))
    val index         = Output(UInt(cacheConfig.indexLen.W))
    val tagValidDirty = Output(Vec(cacheConfig.numOfWays, new TagValidDirtyBundle))
    val phyTag        = Output(UInt(cacheConfig.tagLen.W))
    val bankIndex     = Output(UInt(cacheConfig.bankIndexLen.W))
  })

  val tagValidDirty = Module(new TagValidDirty)

  val virtualTag = io.addr(31, 32 - cacheConfig.tagLen)
  val index = io.addr(
    cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen + cacheConfig.indexLen - 1,
    cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen
  )
  val bankIndex = io.addr(cacheConfig.bankOffsetLen - 1 + cacheConfig.bankIndexLen, cacheConfig.bankOffsetLen)

  io.index               := index
  tagValidDirty.io.index := index
  tagValidDirty.io.write := io.writeTagValidDirty
  io.tagValidDirty       := tagValidDirty.io.tagValidDirty
  io.bankIndex           := bankIndex
  io.phyTag              := Cat(0.U(3.W), virtualTag(cacheConfig.tagLen - 4, 0))
}
