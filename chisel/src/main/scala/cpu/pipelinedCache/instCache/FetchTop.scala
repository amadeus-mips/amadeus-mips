package cpu.pipelinedCache.instCache

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.TagValidBundle
import cpu.pipelinedCache.instCache.fetch.TagValid

/**
  * fetch top that fetches tags, valid, and do address translation
  */
@chiselName
class FetchTop(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(32.W))
    val writeTagValid = Flipped(Valid(new Bundle {
      val waySelection   = UInt(log2Ceil(cacheConfig.numOfWays).W)
      val indexSelection = UInt(cacheConfig.indexLen.W)
      val tagValid       = new TagValidBundle
      /** when write all ways is true, instructions at 4 ways in index is invalidated */
      val writeAllWays       = Bool()
    }))
    val index     = Output(UInt(cacheConfig.indexLen.W))
    val tagValid  = Output(Vec(cacheConfig.numOfWays, new TagValidBundle))
    val phyTag    = Output(UInt(cacheConfig.tagLen.W))
    val bankIndex = Output(UInt(cacheConfig.bankIndexLen.W))
  })

  val tagValid = Module(new TagValid)

  val virtualTag = io.addr(31, 32 - cacheConfig.tagLen)
  val index      = io.addr(31 - cacheConfig.tagLen, 32 - cacheConfig.tagLen - cacheConfig.indexLen)
  val bankIndex  = io.addr(cacheConfig.bankOffsetLen - 1 + cacheConfig.bankIndexLen, cacheConfig.bankOffsetLen)

  io.index            := index
  tagValid.io.address := index
  tagValid.io.write   := io.writeTagValid
  io.tagValid         := tagValid.io.tagValid
  io.bankIndex        := bankIndex
  //TODO: TLB here
  io.phyTag := Cat(0.U(3.W), virtualTag(cacheConfig.tagLen-4, 0))
}
