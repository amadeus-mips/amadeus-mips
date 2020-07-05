package cpu.pipelinedCache.instCache

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

//TODO: add flush support
@chiselName
class ICacheController(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    val isMiss = Input(Bool())
    val finishTransfer = Input(Bool())
    val stall = Output(Bool())
    val writeBack = Output(Bool())
  })

  val sIdle :: sMiss :: sWriteBack :: Nil = Enum(3)
  val state = RegInit(sIdle)

  io.stall := state =/= sIdle && io.isMiss
  io.writeBack := state === sWriteBack

  switch(state) {
    is(sIdle) {
      when(io.isMiss) {
        state := sMiss
      }
    }
    is(sMiss) {
      when(io.finishTransfer) {
        state := sWriteBack
      }
    }
    is(sWriteBack) {
      state := sIdle
    }
  }
}
