package cpu.pipelinedCache.dataCache.query

import chisel3._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.dataCache.DCacheFetchQueryBundle
import cpu.pipelinedCache.instCache.fetch.WriteTagValidBundle

class QueryTop(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    val fetchQuery = Input(new DCacheFetchQueryBundle)
    val write      = Output(new WriteTagValidBundle)
  })
}
