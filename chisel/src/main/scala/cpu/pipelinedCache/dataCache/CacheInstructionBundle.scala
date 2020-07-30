package cpu.pipelinedCache.dataCache

import chisel3._
import cpu.pipelinedCache.CacheConfig

class CacheInstructionBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val index = UInt(cacheConfig.indexLen.W)
}
