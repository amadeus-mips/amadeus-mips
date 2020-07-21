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

    /** request bank index, valid is asserted when there is a new miss */
    val bankIndex = Flipped(Valid(UInt(log2Ceil(cacheConfig.numOfBanks).W)))

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

  val sIdle :: sTransfer :: Nil = Enum(2)
  val state                     = RegInit(sIdle)

  val buffer = Reg(
    Vec(cacheConfig.numOfBanks, UInt(32.W))
  )
  val bufferValidMask = RegInit(VecInit(Seq.fill(cacheConfig.numOfBanks)(false.B)))

  val writePtr = Reg(UInt(log2Ceil(cacheConfig.numOfBanks).W))

  val hitInInputData = WireInit(io.inputData.valid && io.bankIndex.bits === writePtr)

  io.queryResult.valid := Mux(
    hitInInputData,
    true.B,
    bufferValidMask(io.bankIndex.bits)
  )

  io.queryResult.bits := Mux(
    hitInInputData,
    io.inputData.bits,
    buffer(io.bankIndex.bits)
  )

  io.allData := buffer

  switch(state) {
    is(sIdle) {
      when(io.bankIndex.valid) {
        writePtr        := 0.U
        buffer          := 0.U.asTypeOf(buffer)
        state           := sTransfer
        bufferValidMask := 0.U.asTypeOf(bufferValidMask)
      }
    }
    is(sTransfer) {
      when(io.inputData.valid) {
        writePtr                  := writePtr + 1.U
        bufferValidMask(writePtr) := true.B
        buffer(writePtr)          := io.inputData.bits
      }
      when(io.finish) {
        bufferValidMask := 0.U.asTypeOf(bufferValidMask)
        state           := sIdle
      }
    }
  }
}
