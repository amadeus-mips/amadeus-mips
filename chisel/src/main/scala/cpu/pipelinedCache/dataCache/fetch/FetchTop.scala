package cpu.pipelinedCache.dataCache.fetch

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.addressBundle.QueryAddressBundle
import cpu.pipelinedCache.components.metaBanks.TagValidBundle
import cpu.pipelinedCache.instCache.fetch.{TagValid, WriteTagValidBundle}

class FetchTop(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {

    /** request information, no need for decoupled interface because this is the data path
      * we don't use them anyway */
    val addr = Input(UInt(32.W))

    /** write tag valid from refill stage */
    val write = Flipped(Valid(new WriteTagValidBundle))

    /** the query result of the physical request for cache request */
    val addrResult = Output(new QueryAddressBundle)

    /** tag valid bundle for all ways in cache */
    val tagValid = Output(Vec(cacheConfig.numOfWays, new TagValidBundle))
  })
  val tagValid = Module(new TagValid)

  val bankIndex = io.addr(cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen - 1, cacheConfig.bankOffsetLen)
  val virtualIndex = io.addr(
    cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen + cacheConfig.indexLen - 1,
    cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen
  )
  val virtualTag = io.addr(31, cacheConfig.bankOffsetLen + cacheConfig.bankIndexLen + cacheConfig.indexLen)

  //TODO: interface with TLB
  val physicalTag = Cat(0.U(3.W), virtualTag(cacheConfig.tagLen - 4, 0))

  io.addrResult.phyTag    := physicalTag
  io.addrResult.index     := virtualIndex
  io.addrResult.bankIndex := bankIndex
  io.tagValid             := tagValid.io.tagValid

  tagValid.io.index := virtualIndex
  tagValid.io.write := io.write

  //TODO: implement cache instruction
  tagValid.io.invalidateAllWays := false.B
}
