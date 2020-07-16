package cpu.pipelinedCache.components.addressBundle

import chisel3._
import cpu.pipelinedCache.CacheConfig

class QueryAddressBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val phyTag     = UInt(cacheConfig.bankIndexLen.W)
  val index      = UInt(cacheConfig.indexLen.W)
  val bankIndex  = UInt(cacheConfig.bankIndexLen.W)
}
