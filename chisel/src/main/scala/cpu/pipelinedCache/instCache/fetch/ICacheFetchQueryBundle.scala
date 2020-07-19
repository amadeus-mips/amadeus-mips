package cpu.pipelinedCache.instCache.fetch

import chisel3._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.metaBanks.TagValidBundle

class ICacheFetchQueryBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val tagValid  = Vec(cacheConfig.numOfWays, new TagValidBundle)
  val phyTag    = UInt(cacheConfig.tagLen.W)
  val index     = UInt(cacheConfig.indexLen.W)
  val bankIndex = UInt(cacheConfig.bankIndexLen.W)
  val valid     = Bool()

  override def cloneType = (new ICacheFetchQueryBundle).asInstanceOf[this.type]
}
