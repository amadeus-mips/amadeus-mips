package cpu.pipelinedCache

import chisel3.util._

case class CacheConfig(
  numOfSets:  Int = 64,
  numOfWays:  Int = 4,
  numOfBanks: Int = 8,
  bankWidth:  Int = 4 // bytes per bank
) {
  val indexLen:     Int = log2Ceil(numOfSets)
  val bankIndexLen: Int = log2Ceil(numOfBanks)
  // byte addressable memory, 1 bit -> 1 byte change in memory
  val bankOffsetLen: Int = log2Ceil(bankWidth)
  val tagLen:        Int = 32 - indexLen - bankIndexLen - bankOffsetLen
  require(isPow2(numOfSets))
  require(isPow2(numOfWays))
  require(isPow2(numOfBanks))
  require(isPow2(bankWidth))
  require(tagLen + indexLen + bankIndexLen + bankOffsetLen == 32, "basic request calculation")
  require((indexLen + bankIndexLen + bankOffsetLen) <= 12, "prevent request aliasing")
}
