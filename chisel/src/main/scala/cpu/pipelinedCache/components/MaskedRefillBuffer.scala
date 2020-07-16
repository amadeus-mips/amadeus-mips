package cpu.pipelinedCache.components

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

//TODO: how to switch between a read request and a write request
class MaskedRefillBuffer(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** valid signal is asserted when *a new miss is encountered*,
      * always issue a read request otherwise */
    val request = Flipped(Valid(new Bundle {

      /** the which bank is the request in */
      val bankIndex = UInt(log2Ceil(cacheConfig.numOfBanks).W)

      /** the write mask for a write request */
      val writeMask = UInt(4.W)

      /** what data to write */
      val writeData = UInt(32.W)
    }))

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

    /** connect directly to banks, used for write back */
    val allData = Output(Vec(cacheConfig.numOfBanks, UInt(32.W)))
  })

  val sIdle :: sTransfer :: Nil = Enum(2)
  val state                     = RegInit(sIdle)

  val buffer = Reg(
    Vec(cacheConfig.numOfBanks, UInt(32.W))
  )
  val bufferValidMask = RegInit(VecInit(Seq.fill(cacheConfig.numOfBanks)(0.U(4.W))))

  /** writePtr points to the next location to write */
  val writePtr = Reg(UInt(log2Ceil(cacheConfig.numOfBanks).W))

//  io.request.ready := state === sTransfer && io.request.bits.bankIndex =/= writePtr

  io.queryResult.valid := (bufferValidMask(
    io.request.bits.bankIndex
  ) === 15.U && io.request.bits.writeMask === 0.U) || (io.request.bits.writeMask =/= 0.U)

  io.queryResult.bits := buffer(io.request.bits.bankIndex)

  io.allData := buffer

  val oldRefillData = WireInit(buffer(writePtr))
  val oldRefillMask = WireInit(bufferValidMask(writePtr))

  val oldRequestData = WireInit(buffer(io.request.bits.bankIndex))
  val oldRequestMask = WireInit(bufferValidMask(io.request.bits.bankIndex))

  switch(state) {
    is(sIdle) {
      when(io.request.valid) {
        writePtr        := io.request.bits.bankIndex
        buffer          := 0.U.asTypeOf(buffer)
        state           := sTransfer
        bufferValidMask := 0.U.asTypeOf(bufferValidMask)
      }
    }
    is(sTransfer) {
      when(io.inputData.valid) {
        writePtr := writePtr + 1.U
        buffer(writePtr) := Cat(
          (3 to 0 by -1).map(i =>
            Mux(oldRefillMask(i), oldRefillData(8 * i + 7, 8 * i), io.inputData.bits(8 * i + 7, 8 * i))
          )
        )
        bufferValidMask(writePtr) := 15.U
      }
      when(io.finish) {
        bufferValidMask := 0.U.asTypeOf(bufferValidMask)
        state           := sIdle
      }
    }
  }
}
