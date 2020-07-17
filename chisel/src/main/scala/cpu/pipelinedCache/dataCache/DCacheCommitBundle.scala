package cpu.pipelinedCache.dataCache

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

class DCacheCommitBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val indexSel     = UInt(log2Ceil(cacheConfig.numOfSets).W)
  val waySel       = UInt((log2Ceil(cacheConfig.numOfWays).W))
  val bankIndexSel = UInt((log2Ceil(cacheConfig.numOfBanks).W))
  val writeData    = UInt(32.W)
  val writeMask    = UInt(4.W)

  /** data read from write queue or refill buffer */
  val readData = UInt(32.W)

  /** when read data is valid, select io.readData
    * otherwise, select data coming from bank */
  val readDataValid = Bool()

  /** write enable signal for data banks. If write is hit in refill buffer, then
    * write enable is false */
  val writeEnable = Bool()

  override def cloneType = (new DCacheCommitBundle).asInstanceOf[this.type]
}
