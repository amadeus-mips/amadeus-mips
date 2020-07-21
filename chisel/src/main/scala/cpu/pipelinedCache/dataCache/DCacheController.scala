package cpu.pipelinedCache.dataCache

import chisel3._
import cpu.pipelinedCache.CacheConfig

class DCacheController(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** is stage 2 ready for another signal? */
    val stage2Ready = Input(Bool())

    /** external control signal, controls whether dcache can accept next input */
    val inputReady = Output(Bool())

  })
  io.inputReady      := io.stage2Ready

}
