package cpu.pipelinedCache.dataCache

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

class DCacheCommitBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val refillBufferData = UInt(32.W)
  val waySel           = UInt((log2Ceil(cacheConfig.numOfWays).W))

  override def cloneType = (new DCacheCommitBundle).asInstanceOf[this.type]
}
