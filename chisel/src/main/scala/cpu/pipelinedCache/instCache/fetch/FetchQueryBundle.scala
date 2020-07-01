package cpu.pipelinedCache.instCache.fetch

import chisel3._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.TagValidBundle

class FetchQueryBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val tagValid = Vec(4, new TagValidBundle)
  val phyTag = UInt(cacheConfig.tagLen.W)
  val index = UInt(cacheConfig.indexLen.W)
  val bankIndex = UInt(cacheConfig.bankIndexLen.W)
  val invalid = Bool()

  override def cloneType = (new FetchQueryBundle).asInstanceOf[this.type]
}
