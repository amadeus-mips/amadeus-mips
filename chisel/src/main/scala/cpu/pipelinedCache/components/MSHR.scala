package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

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
      * this `valid` value is key for flushing
      * ready means there is no miss that is being handled */
    val missAddr = Flipped(Valid(new MSHREntry))

    /** is the current state in a miss? *
      *current missing info*/
    val mshrInfo = Output(new MSHREntry())
  })
  val missEntryReg = RegInit(0.U.asTypeOf(new MSHREntry))
  when(io.missAddr.valid) {
    missEntryReg := io.missAddr.bits
  }
  io.mshrInfo := missEntryReg

}
