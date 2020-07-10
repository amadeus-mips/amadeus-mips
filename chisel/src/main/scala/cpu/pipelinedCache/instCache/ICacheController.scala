package cpu.pipelinedCache.instCache

import chisel3._
import chisel3.internal.naming.chiselName
import cpu.pipelinedCache.CacheConfig

@chiselName
class ICacheController(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** flush request */
    val flushReq = Input(Bool())

    /** is stage 2 free */
    val stage2Free = Input(Bool())

    /** is this cycle a writeback */
    val writeBack = Input(Bool())

    /** ready for next instruction */
    val reqReady = Output(Bool())

    /** write enable for instruction banks, for fetch stage */
    val writeEnable = Output(Bool())

    /** flush stage 2 registers */
    val flush = Output(Bool())
  })

  io.reqReady    := io.stage2Free && !io.writeBack
  io.flush       := io.flushReq
  io.writeEnable := io.writeBack
}
