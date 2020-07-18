package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.metaBanks.TagValidBundle

class MSHREntry(implicit cacheConfig: CacheConfig) extends Bundle {
  val tag   = UInt(cacheConfig.tagLen.W)
  val index = UInt(cacheConfig.indexLen.W)

  /** bank index is required to support axi burst transfer initial address */
  val bankIndex = UInt(cacheConfig.bankIndexLen.W)

  override def cloneType = (new MSHREntry).asInstanceOf[this.type]
}

@chiselName
class MSHR(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** miss request value for mshr input
      * tag valid at index is for dcache only */
    val recordMiss = Flipped(Valid(new Bundle {
      val addr            = new MSHREntry
      val tagValidAtIndex = Input(Vec(cacheConfig.numOfWays, new TagValidBundle))
    }))

    /** is the current state in a miss? *
      *current missing info*/
    val extractMiss = Output(new Bundle {
      val addr            = new MSHREntry
      val tagValidAtIndex = (Vec(cacheConfig.numOfWays, new TagValidBundle))
    })
  })
  val missEntryReg       = Reg((new MSHREntry))
  val tagValidAtIndexReg = Reg(Vec(cacheConfig.numOfWays, new TagValidBundle))
  when(io.recordMiss.valid) {
    missEntryReg       := io.recordMiss.bits.addr
    tagValidAtIndexReg := io.recordMiss.bits.tagValidAtIndex
  }
  io.extractMiss.addr            := missEntryReg
  io.extractMiss.tagValidAtIndex := tagValidAtIndexReg

}
