package cpu.pipelinedCache.dataCache

import chisel3._
import cpu.pipelinedCache.CacheConfig

class DCacheController(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** external control signal, controls whether dcache can accept next input */
    val inputReady = Output(Bool())

    /** internal control signal, read the evicted line when tryEvict is high */
    val tryEvict = Output(Bool())

    /** is stage 2 free for fetched results to enter */
    val stage2Free = Output(Bool())

    /** internal control signal, write back this cycle on this signal */
    val writeBack = Output(Bool())
  })
}
