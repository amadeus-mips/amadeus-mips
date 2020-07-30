package cpu.pipelinedCache.dataCache

import chisel3._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.instCache.fetch.ICacheFetchQueryBundle

class DCacheFetchQueryBundle(implicit cacheConfig: CacheConfig) extends ICacheFetchQueryBundle {
  val writeData   = UInt(32.W)
  val writeMask   = UInt(4.W)
  val invalidate = Bool()

  override def cloneType = (new DCacheFetchQueryBundle).asInstanceOf[this.type]
}
