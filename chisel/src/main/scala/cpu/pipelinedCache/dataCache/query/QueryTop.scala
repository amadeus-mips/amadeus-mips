package cpu.pipelinedCache.dataCache.query

import chisel3._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.AXIPorts.{AXIReadPort, AXIWritePort}
import cpu.pipelinedCache.components.{MSHR, MissComparator}
import cpu.pipelinedCache.dataCache.DCacheFetchQueryBundle
import cpu.pipelinedCache.instCache.fetch.WriteTagValidBundle
import shared.Constants.DATA_ID

class QueryTop(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    val fetchQuery = Input(new DCacheFetchQueryBundle)
    val write      = Output(new WriteTagValidBundle)
  })

  val comparator = Module(new MissComparator)
  val mshr = Module(new MSHR)
  val axiRead = Module(new AXIReadPort(addrReqWidth = 32, AXIID = DATA_ID))
  val axiWrite = Module(new AXIWritePort(AXIID = DATA_ID))


}
