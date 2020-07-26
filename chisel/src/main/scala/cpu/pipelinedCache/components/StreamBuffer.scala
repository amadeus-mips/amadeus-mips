package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.addressBundle.QueryAddressBundle

/** stream buffer generate cache miss. When r data arrives, it first arrives at the refill buffer */
@chiselName
class StreamBuffer(size: Int)(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** request a new miss in the stream buffer
      * when valid is asserted, flush the stream buffer */
    val newAddress = Flipped(Valid(new QueryAddressBundle()))

    /** input data from [[ReFillBuffer]]
      * valid means the data in this beat is valid */
    val inputData = Flipped(Valid(Vec(cacheConfig.numOfBanks, UInt(32.W))))

    /** valid is asserted in following scenarios:
      * the line is valid
      */
    val queryResult = Valid(Vec(2, UInt(32.W)))

    val writeBack = Output(Bool())

    /** connect directly to [[cpu.pipelinedCache.dataCache.DataBanks]], used for write back */
    val allData = Output(Vec(cacheConfig.numOfBanks, UInt(32.W)))
  })

  //TODO: what if flush when rlast comes
  /** how many returning miss I'm waiting */
  val outstandingMissCountReg = RegInit(0.U((log2Ceil(size) + 1).W))

}
