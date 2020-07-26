package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

/**
  * refill buffer to temporarily holds the returned data until AXI transfer is complete
  *
  */
@chiselName
class ReFillBuffer(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** new bank index for the next refill */
    val newRefillBankIndex = Input(UInt(log2Ceil(cacheConfig.numOfBanks).W))

    /** request bank index, valid is asserted when there is a new miss */
    val queryBankIndex = Input(UInt(log2Ceil(cacheConfig.numOfBanks).W))

    /** input data from [[cpu.pipelinedCache.components.AXIPorts.AXIReadPort]]
      * valid means the data in this beat is valid */
    val inputData = Flipped(Decoupled(UInt(32.W)))

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

  val buffer = Reg(
    Vec(cacheConfig.numOfBanks, UInt(32.W))
  )
  val bufferValidMask = RegInit(VecInit(Seq.fill(cacheConfig.numOfBanks)(false.B)))

  val writePtr = Reg(UInt(log2Ceil(cacheConfig.numOfBanks).W))

  val hitInInputData = WireInit(io.inputData.valid && io.queryBankIndex === writePtr)

  val waitForDataReg = RegInit(false.B)

  io.queryResult.valid := Mux(
    hitInInputData,
    true.B,
    bufferValidMask(io.queryBankIndex)
  )

  io.queryResult.bits := Mux(
    hitInInputData,
    io.inputData.bits,
    buffer(io.queryBankIndex)
  )

  io.allData := buffer

  val writeBackThisCycle = RegNext(io.finish && io.inputData.valid)

  io.inputData.ready := !writeBackThisCycle

  when(!io.inputData.fire && !waitForDataReg) {
    writePtr := io.newRefillBankIndex
  }

  when(io.inputData.valid) {
    waitForDataReg := true.B
    writePtr                  := writePtr + 1.U
    bufferValidMask(writePtr) := true.B
    buffer(writePtr)          := io.inputData.bits
    when(io.finish) {
      bufferValidMask := 0.U.asTypeOf(bufferValidMask)
      waitForDataReg := false.B
    }
  }
}
