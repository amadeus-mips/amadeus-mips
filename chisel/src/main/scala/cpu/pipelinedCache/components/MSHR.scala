package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

class MSHREntry(implicit cacheConfig: CacheConfig) extends Bundle {
  val tag = UInt(cacheConfig.tagLen.W)
  val index = UInt(cacheConfig.indexLen.W)
  val bankIndex = UInt(cacheConfig.bankIndexLen.W)

  override def cloneType = (new MSHREntry).asInstanceOf[this.type]
}

@chiselName
class MSHR(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    /*miss address value for mshr input
     * this `valid` value is key for flushing */
    val missAddr = Flipped(Decoupled(new MSHREntry))
    /*is the refill buffer ready for write back*/
    val readyForWB = Input(Bool())

    /*do we write back *this* cycle*/
    val writeBack = Output(Bool())
    /*is the current state in a miss? */
    /*current missing info*/
    val mshrInfo = Output(new MSHREntry())
  })
  val missEntryReg = RegInit(0.U.asTypeOf(new MSHREntry))
  val wbNext = RegInit(false.B)
  wbNext := false.B
  val sIdle :: sTransfer :: Nil = Enum(2)
  val state = RegInit(sIdle)

  io.writeBack := wbNext
  io.mshrInfo := missEntryReg
  io.missAddr.ready := state === sIdle

  switch(state) {
    is(sIdle) {
      when(io.missAddr.valid) {
        missEntryReg := io.missAddr.bits
        state := sTransfer
      }
    }
    is(sTransfer) {
      when(io.readyForWB) {
        wbNext := true.B
        state := sIdle
      }
    }
  }

}
