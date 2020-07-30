package cpu.pipelinedCache.dataCache.fetch

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.addressBundle.QueryAddressBundle
import cpu.pipelinedCache.components.metaBanks.TagValidBundle
import cpu.pipelinedCache.instCache.fetch.TagValid

class FetchTop(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {

    /** request information, no need for decoupled interface because this is the data path
      * we don't use them anyway */
    val addr = Input(new Bundle {

      /** see [[cpu.common.MemReqBundle.tag]] */
      val translatedTag = UInt(20.W)

      /** see [[cpu.common.MemReqBundle.physicalIndex]] */
      val physicalIndex = UInt(12.W)
    })

    /** writeBack tag valid from refill stage */
    val write = Flipped(Valid(new Bundle {
      val addr = new Bundle {
        val index  = UInt(cacheConfig.indexLen.W)
        val waySel = UInt(log2Ceil(cacheConfig.numOfWays).W)
      }
      val tagValid = new TagValidBundle
    }))

    val invalidateAll = Input(Bool())

    /** the query result of the physical request for cache request */
    val addrResult = Output(new QueryAddressBundle)

    /** tag valid bundle for all ways in cache */
    val tagValid = Output(Vec(cacheConfig.numOfWays, new TagValidBundle))
  })
  val tagValid = Module(new TagValid)

  val bankIndex =
    io.addr.physicalIndex(cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen - 1, cacheConfig.bankOffsetLen)
  val virtualIndex = io.addr.physicalIndex(
    cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen + cacheConfig.indexLen - 1,
    cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen
  )

  val physicalTag = if (cacheConfig.tagLen > 20) Cat(io.addr.translatedTag, io.addr.physicalIndex(11, 32 - cacheConfig.tagLen)) else io.addr.translatedTag

  io.addrResult.phyTag    := physicalTag
  io.addrResult.index     := virtualIndex
  io.addrResult.bankIndex := bankIndex
  io.tagValid             := tagValid.io.tagValid

  tagValid.io.index := virtualIndex
  tagValid.io.write := io.write

  tagValid.io.invalidateAllWays := io.invalidateAll
}
