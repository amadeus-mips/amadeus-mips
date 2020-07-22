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

    /** connect directly to [[cpu.pipelinedCache.dataCache.DataBanks]], used for write back */
    val allData = Output(Vec(cacheConfig.numOfBanks, UInt(32.W)))

    val dataDirty = Output(Bool())
  })

  val sIdle :: sTransfer :: Nil = Enum(2)
  val state                     = RegInit(sIdle)

  val refillBuffer = Reg(
    Vec(cacheConfig.numOfBanks, UInt(32.W))
  )

  /** is each element in the true *refill buffer* valid? */
  val refillBufferValidArray = RegInit(VecInit(Seq.fill(cacheConfig.numOfBanks)(false.B)))

  /** writePtr points to the next location to write */
  val writePtr = Reg(UInt(log2Ceil(cacheConfig.numOfBanks).W))

  /** all write goes into this buffer */
  val writeHoldBuffer          = Reg(Vec(cacheConfig.numOfBanks, UInt(32.W)))
  val writeHoldBufferMaskArray = RegInit(VecInit(Seq.fill(cacheConfig.numOfBanks)(VecInit(Seq.fill(4)(false.B)))))

  /** have I written to buffer since refill? */
  val bufferDirty = RegInit(false.B)

  val readData =
    VecInit(
      (0 until cacheConfig.numOfBanks).map(bankIndex =>
        Cat(
          (3 to 0 by -1).map(byteOffset =>
            Mux(
              writeHoldBufferMaskArray(bankIndex)(byteOffset),
              writeHoldBuffer(bankIndex)(8 * byteOffset + 7, 8 * byteOffset),
              refillBuffer(bankIndex)(8 * byteOffset + 7, 8 * byteOffset)
            )
          )
        )
      )
    )

  val requestedWriteHoldBuffer = writeHoldBuffer(io.request.bits.bankIndex)

  val requestedRefillBuffer = refillBuffer(io.request.bits.bankIndex)

  /** write query is always valid during transfer, read query is valid when there is a hit in InputData or
    * the read position is all valid */
  io.queryResult.valid := (refillBufferValidArray(io.request.bits.bankIndex) || (writeHoldBufferMaskArray(
    io.request.bits.bankIndex
  ).asUInt === "b1111".U(
    4.W
  )) && (io.request.bits.writeMask === 0.U)) || ((io.request.bits.writeMask =/= 0.U) && state === sTransfer)

  io.queryResult.bits := readData(io.request.bits.bankIndex)

  io.allData := readData

  io.dataDirty := bufferDirty

  switch(state) {
    is(sIdle) {
      when(io.request.valid) {
        writePtr               := io.request.bits.bankIndex
        refillBuffer           := 0.U.asTypeOf(refillBuffer)
        bufferDirty            := false.B
        state                  := sTransfer
        refillBufferValidArray := 0.U.asTypeOf(refillBufferValidArray)
      }
    }
    is(sTransfer) {

      when(io.request.bits.writeMask =/= 0.U) {
        bufferDirty := true.B
        for (i <- 0 until 4) {
          writeHoldBufferMaskArray(io.request.bits.bankIndex)(i) := writeHoldBufferMaskArray(io.request.bits.bankIndex)(
            i
          ) | io.request.bits.writeMask(i)
        }
        writeHoldBuffer(io.request.bits.bankIndex) := Cat(
          (3 to 0 by -1).map(byteOffset =>
            Mux(
              io.request.bits.writeMask(byteOffset),
              io.request.bits.writeData(8 * byteOffset + 7, 8 * byteOffset),
              requestedWriteHoldBuffer(8 * byteOffset + 7, 8 * byteOffset)
            )
          )
        )
      }

      when(io.inputData.valid) {
        writePtr                         := writePtr + 1.U
        refillBuffer(writePtr)           := io.inputData.bits
        refillBufferValidArray(writePtr) := true.B
      }

      when(io.finish) {
        refillBufferValidArray := 0.U.asTypeOf(refillBufferValidArray)
        state                  := sIdle
      }
    }
  }
}
