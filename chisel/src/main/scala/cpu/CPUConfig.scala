package cpu

import cpu.pipelinedCache.CacheConfig

case class CPUConfig(
  build:              Boolean,
  memoryFile:         String  = "",
  tlbSize:            Int     = 32,
  compareRamDirectly: Boolean = false,
  verification:       Boolean = false
) {
  val iCacheConfig = CacheConfig()
  val dCacheConfig = CacheConfig()

  val branchPredictorAddrLen       = 10
  val branchPredictorTableEntryNum = 64
//  require(build && memoryFile.isEmpty || !build && !memoryFile.isEmpty)
}

object CPUConfig {
  val Build = new CPUConfig(build = true, memoryFile = "", compareRamDirectly = false)
}
