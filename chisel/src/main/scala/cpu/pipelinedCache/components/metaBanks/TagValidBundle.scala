package cpu.pipelinedCache.components.metaBanks

import chisel3._
import cpu.pipelinedCache.CacheConfig

class TagValidBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val tag   = UInt(cacheConfig.tagLen.W)
  val valid = Bool()

  override def cloneType: this.type = (new TagValidBundle).asInstanceOf[this.type]
}
