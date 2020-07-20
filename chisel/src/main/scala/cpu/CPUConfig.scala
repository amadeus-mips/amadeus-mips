package cpu

import chisel3.util.isPow2
import cpu.BranchPredictorType.TwoBit
import cpu.common.WriteMask
import cpu.pipelinedCache.CacheConfig

case class CPUConfig(
  build:                        Boolean,
  memoryFile:                   String              = "",
  tlbSize:                      Int                 = 32,
  fetchAmount:                  Int                 = 2,
  compareRamDirectly:           Boolean             = false,
  iCacheConf:                   CacheConfig         = CacheConfig(),
  dCacheConf:                   CacheConfig         = CacheConfig(),
  branchPredictorAddrLen:       Int                 = 10,
  branchPredictorTableEntryNum: Int                 = 64,
  branchPredictorType:          BranchPredictorType = TwoBit,
  instructionFIFOLength:        Int                 = 4,
  instructionFIFOWidth:         Int                 = 2
) {
  WriteMask.tlbSize = tlbSize
  require(isPow2(instructionFIFOLength))

//  require(build && memoryFile.isEmpty || !build && !memoryFile.isEmpty)
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
