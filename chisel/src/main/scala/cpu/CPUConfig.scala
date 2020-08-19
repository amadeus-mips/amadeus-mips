package cpu

import chisel3.util.isPow2
import cpu.pipelinedCache.CacheConfig

case class CPUConfig(
  build:                     Boolean,
  memoryFile:                String      = "",
  enableTLB:                 Boolean     = true,
  useMemTLB:                 Boolean     = true,
  tlbSize:                   Int         = 16,
  fetchAmount:               Int         = 2,
  compareRamDirectly:        Boolean     = false,
  iCacheConf:                CacheConfig = CacheConfig(numOfSets = 128, numOfWays = 2),
  dCacheConf:                CacheConfig = CacheConfig(numOfSets = 128, numOfWays = 2),
  branchPredictorAddrLen:    Int         = 10,
  branchPredictorHistoryLen: Int         = 2,
  instructionFIFOLength:     Int         = 4,
  decodeWidth:               Int         = 2,
  verification:              Boolean     = false
) {
  val decodeBufferNum = 1
  require(isPow2(instructionFIFOLength))
}

object CPUConfig {
  val Build = new CPUConfig(build = true, memoryFile = "", compareRamDirectly = false)
}

sealed trait BranchPredictorType

object BranchPredictorType {
  case object TwoBit extends BranchPredictorType
  case object AlwaysTaken extends BranchPredictorType
  case object AlwaysNotTaken extends BranchPredictorType
}
