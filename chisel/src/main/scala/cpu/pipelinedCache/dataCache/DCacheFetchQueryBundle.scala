package cpu.pipelinedCache.dataCache

import chisel3._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.instCache.fetch.ICacheFetchQueryBundle

class DCacheFetchQueryBundle(implicit cacheConfig: CacheConfig) extends ICacheFetchQueryBundle {
  val writeEnable = Bool()
  val writeData   = Vec(4, UInt(8.W))
  val writeMask   = Vec(4, Bool())

  override def cloneType = (new DCacheFetchQueryBundle).asInstanceOf[this.type]
}
