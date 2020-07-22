package cpu.pipelinedCache.dataCache

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

//TODO: much of this does not require storage
class DCacheCommitBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val indexSel     = UInt(log2Ceil(cacheConfig.numOfSets).W)
  val waySel       = UInt((log2Ceil(cacheConfig.numOfWays).W))
  val bankIndexSel = UInt((log2Ceil(cacheConfig.numOfBanks).W))

  /** write data for a normal write query */
  val writeData = UInt(32.W)

  /** writeBack enable signal for data banks. If writeBack is hit in refill buffer, then
    * writeBack enable is false */
  val writeEnable = Bool()

  /** NOTE: this does not control if writeBack is enabled. If writeBack enable is low, this is ignored
    * should be set to 0 */
  val writeMask = UInt(4.W)

  /** data read from write queue or refill buffer */
  val readData = UInt(32.W)

  /** when read data is valid, select io.readData
    * otherwise, select data coming from bank */
  val readDataValid = Bool()

  override def cloneType = (new DCacheCommitBundle).asInstanceOf[this.type]
}
