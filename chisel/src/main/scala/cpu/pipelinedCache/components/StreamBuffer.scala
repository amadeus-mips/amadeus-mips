package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.addressBundle.QueryAddressBundle

@chiselName
class StreamBuffer(size: Int)(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** flush the whole stream buffer */
    val flush = Input(Bool())

    /** request bank index, valid is asserted when there is a new miss */
    val address = Flipped(Valid(new QueryAddressBundle()))

    /** input data from [[cpu.pipelinedCache.components.AXIPorts.AXIReadPort]]
      * valid means the data in this beat is valid */
    val inputData = Flipped(Valid(UInt(32.W)))

    /** axi r last signal from [[cpu.pipelinedCache.components.AXIPorts.AXIReadPort]]
      * this serves as a wire from axi port to centrol cache control */
    val finish = Input(Bool())

    /** valid is asserted in following scenarios:
      * 1. write is successful
      * 2. read data is valid at bank index in refill buffer*/
    val queryResult = Valid(UInt(32.W))

    /** connect directly to [[cpu.pipelinedCache.dataCache.DataBanks]], used for write back */
    val allData = Output(Vec(cacheConfig.numOfBanks, UInt(32.W)))
  })

  //TODO: what if flush when rlast comes
  /** how many returning miss I'm waiting */
  val outstandingMissCountReg = RegInit(0.U((log2Ceil(size) + 1).W))

}
