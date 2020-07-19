package cpu

import cpu.common.WriteMask
import cpu.pipelinedCache.CacheConfig

case class CPUConfig(
  build:              Boolean,
  memoryFile:         String      = "",
  tlbSize:            Int         = 32,
  compareRamDirectly: Boolean     = false,
  iCacheConf:         CacheConfig = new CacheConfig,
  dCacheConf:         CacheConfig = new CacheConfig,
  branchPredictorAddrLen: Int      = 10,
  branchPredictorTableEntryNum: Int = 64
) {
  WriteMask.tlbSize = tlbSize

//  require(build && memoryFile.isEmpty || !build && !memoryFile.isEmpty)
}

object CPUConfig {
  val Build = new CPUConfig(build = true, memoryFile = "", compareRamDirectly = false)
}
