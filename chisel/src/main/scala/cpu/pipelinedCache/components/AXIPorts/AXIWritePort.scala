package cpu.pipelinedCache.components.AXIPorts

import axi.AXIIO
import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.addressBundle.RecordAddressBundle

//TODO: use outstanding AXI
@chiselName
class AXIWritePort(AXIID: UInt)(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** request request, when request request is valid, try to initiate a write transaction
      * when axi write port is ready to start a transaction, assert the ready signal */
    val addrRequest = Flipped(Decoupled(new RecordAddressBundle))

    /** data transfer for each beat, when there is a hanshake, carry new data next cycle */
    val data = Flipped(Decoupled(UInt(32.W)))

    /** indicate the end of one transfer */
    val dataLast = Input(Bool())

    /** standard axi interface */
    val axi = AXIIO.master()
  })

  val writeIdle :: writeTransfer :: Nil = Enum(3)
  val writeState                        = RegInit(writeIdle)

  /** aw and w requests are initiated at the same time, this is to keep track of aw status */
  val awValidReg = RegInit(false.B)

  /** is w ready before the first w handshake */
  val wFirstReg = RegInit(false.B)

  val requestAddress = Cat(
    io.addrRequest.bits.tag,
    io.addrRequest.bits.index,
    0.U((32 - cacheConfig.tagLen - cacheConfig.indexLen).W)
  )

  io.addrRequest.ready := writeState === writeIdle
  io.data.ready        := io.axi.w.fire || wFirstReg
  // axi section
  io.axi.ar := DontCare
  io.axi.r  := DontCare

  io.axi.aw.valid := awValidReg || (writeState === writeIdle && io.addrRequest.fire)
  io.axi.aw.bits.addr := Cat(
    io.addrRequest.bits.tag,
    io.addrRequest.bits.index,
    0.U((32 - cacheConfig.tagLen - cacheConfig.indexLen).W)
  )
  io.axi.aw.bits.burst := "b01".U(2.W)
  io.axi.aw.bits.len   := (cacheConfig.numOfBanks).U(4.W)
  io.axi.aw.bits.id    := AXIID
  io.axi.aw.bits.size  := "b010".U(3.W)
  io.axi.aw.bits.cache := 0.U
  io.axi.aw.bits.prot  := 0.U
  io.axi.aw.bits.lock  := 0.U

  io.axi.w.bits.data := io.data.bits
  io.axi.w.valid     := io.data.valid
  io.axi.w.bits.last := io.dataLast
  io.axi.w.bits.strb := "b1111".U(4.W)
  io.axi.w.bits.id   := AXIID

  // always ignore b channel response ( no cache error )
  io.axi.b.ready := true.B
  switch(writeState) {
    is(writeIdle) {
      when(io.addrRequest.fire) {
        writeState := writeTransfer
        awValidReg := !io.axi.aw.fire
        wFirstReg  := !io.axi.w.fire
      }
    }
    is(writeTransfer) {
      // state register elements
      when(io.axi.aw.fire) {
        awValidReg := false.B
      }
      when(io.axi.w.fire) {
        wFirstReg := false.B
      }
      when(io.dataLast) {
        writeState := writeIdle
      }
    }
  }

}
