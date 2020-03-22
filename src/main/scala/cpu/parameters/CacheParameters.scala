package cpu.parameters

import chisel3.util._

trait CacheParameters {
  val numOfWays = 8
  val numOfSets = 1
  val numOfBlocks = numOfWays * numOfSets
  val blockSize = 16 // in byte
  val addrWidth = 32 // in bit
  val instWidth = 32 // in bit

  val cacheSize = numOfBlocks * blockSize // in byte

  // the bits length in the meta data

  val offsetWidth = log2Ceil(blockSize)
  // the index width of the tag line
  val indexWidth = log2Ceil(numOfSets)
  // the tag width of the cache line
  val tagWidth = addrWidth - offsetWidth - indexWidth
}
