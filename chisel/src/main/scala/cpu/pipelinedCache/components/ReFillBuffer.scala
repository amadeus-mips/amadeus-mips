package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

/**
  * refill buffer to temporarily holds the returned data until AXI transfer is complete
  *
  * @param writeEnable whether this can be used for D-cache
  */
@chiselName
class ReFillBuffer(writeEnable: Boolean = false)(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    val addr = Flipped(Valid(UInt(log2Ceil(cacheConfig.numOfBanks).W)))
    val inputData = Flipped(Valid(UInt(32.W)))
    val finish = Input(Bool())
    val queryResult = Valid(UInt(32.W))
    val allData = Output(Vec(cacheConfig.numOfBanks, UInt(32.W)))
  })
  require(!writeEnable, "don't enable write for now")

  val sIdle :: sTransfer :: sWriteBack :: Nil = Enum(3)
  val state = RegInit(sIdle)

  val buffer = Reg(
    Vec(cacheConfig.numOfBanks, (if (writeEnable) Vec(4, UInt(8.W)) else UInt(32.W)))
  )
  val bufferValidMask = RegInit(VecInit(Seq.fill(cacheConfig.numOfBanks)(false.B)))

  val writePtr = Reg(UInt(log2Ceil(cacheConfig.numOfBanks).W))

  io.queryResult.valid := bufferValidMask(io.addr.bits) && state =/= sIdle
  io.queryResult.bits := buffer(io.addr.bits)

  io.allData := buffer

  switch(state) {
    is(sIdle) {
      when(io.addr.valid) {
        writePtr := io.addr.bits
        buffer := 0.U.asTypeOf(buffer)
        state := sTransfer
      }
    }
    is(sTransfer) {
      when(io.inputData.valid) {
        writePtr := writePtr + 1.U
        bufferValidMask(writePtr) := true.B
        buffer(writePtr) := io.inputData.bits
      }
      when(io.finish) {
        state := sWriteBack
      }
    }
    is(sWriteBack) {
      assert(bufferValidMask.asUInt.andR, "valid mask should all be true")
      state := sIdle
      bufferValidMask := 0.U.asTypeOf(bufferValidMask)
    }
  }
  assert((io.addr.valid && state === sIdle) || (state =/= sIdle && !io.addr.valid))
  assert((io.inputData.valid && state === sTransfer) || (state =/= sTransfer && !io.inputData.valid))
}
