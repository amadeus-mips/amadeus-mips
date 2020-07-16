package cpu.pipelinedCache.components.addressBundle

import chisel3._
import cpu.pipelinedCache.CacheConfig

/**
  * bundle for storing an address, lower bits are emitted
  */
class RecordAddressBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val tag   = UInt(cacheConfig.tagLen.W)
  val index = UInt(cacheConfig.indexLen.W)
}
