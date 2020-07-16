package cpu.pipelinedCache.instCache.fetch

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.metaBanks.TagValidBundle

/**
  * fetch top that fetches tags, valid, and do address translation
  */
@chiselName
class FetchTop(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val addr              = Input(UInt(32.W))
    val write             = Flipped(Valid(new WriteTagValidBundle))
    val index             = Output(UInt(cacheConfig.indexLen.W))
    val tagValid          = Output(Vec(cacheConfig.numOfWays, new TagValidBundle))
    val invalidateAllWays = Input(Bool())
    val phyTag            = Output(UInt(cacheConfig.tagLen.W))
    val bankIndex         = Output(UInt(cacheConfig.bankIndexLen.W))
  })

  val tagValid = Module(new TagValid)

  val virtualTag = io.addr(31, 32 - cacheConfig.tagLen)
  val index = io.addr(
    cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen + cacheConfig.indexLen - 1,
    cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen
  )
  val bankIndex = io.addr(cacheConfig.bankOffsetLen - 1 + cacheConfig.bankIndexLen, cacheConfig.bankOffsetLen)

  io.index                      := index
  tagValid.io.index             := index
  tagValid.io.write             := io.write
  tagValid.io.invalidateAllWays := io.invalidateAllWays
  io.tagValid                   := tagValid.io.tagValid
  io.bankIndex                  := bankIndex
  //TODO: TLB here
  io.phyTag := Cat(0.U(3.W), virtualTag(cacheConfig.tagLen - 4, 0))
}
