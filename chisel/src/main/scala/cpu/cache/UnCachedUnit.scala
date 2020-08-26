package cpu.cache

import axi.AXIIO
import chisel3._
import chisel3.util._
import cpu.common.MemReqBundle
import shared.Constants

/**
  * uncached unit will only accept another request when the previous one transaction has finished
  * this is the slowest implementation that is possible
  */
//TODO: axi memory ordering
class UnCachedUnit extends Module {
  val io = IO(new Bundle {

    /** see documentation of [[cpu.pipelinedCache.DataCache.io.request]] */
    val request = Flipped(Decoupled(new MemReqBundle))

    /** see documentation of [[cpu.pipelinedCache.DataCache.io.commit]] */
    val commit = Output(Bool())

    /** see documentation of [[cpu.pipelinedCache.DataCache.io.readData]] */
    val readData = Output(UInt(32.W))
    val axi      = AXIIO.master()
  })

  val readIdle :: readWaitForAR :: readWaitForR :: Nil = Enum(3)
  val readState                                        = RegInit(readIdle)
  val readAddressReg                                   = Reg(UInt(32.W))

  val writeIdle :: writeAW :: writeTransfer :: writeFinish :: Nil = Enum(4)
  val writeState                                                  = RegInit(writeIdle)
  val writeAddressReg                                             = Reg(UInt(32.W))
  val writeDataReg                                                = Reg(UInt(32.W))
  val writeMaskReg                                                = Reg(UInt(4.W))

  //-----------------------------------------------------------------------------
  //------------------default IO--------------------------------------
  //-----------------------------------------------------------------------------
  io.readData := RegNext(io.axi.r.bits.data)

  /** when the last ( and only ) r data came, or both w and aw has performed a handshake */
  io.commit := io.axi.r.fire

  io.request.ready := writeState === writeIdle && readState === readIdle

  io.axi.ar.bits.id    := Constants.DATA_ID
  io.axi.ar.bits.addr  := readAddressReg
  io.axi.ar.bits.len   := 0.U(4.W)
  io.axi.ar.bits.size  := MuxCase("b010".U(3.W), Array(
    (readAddressReg(0) === 1.U(1.W)) -> "b000".U(3.W),
    (readAddressReg(1) === 1.U(1.W)) -> "b001".U(3.W)
  ))
  io.axi.ar.bits.burst := "b01".U(2.W) // Incrementing-request burst
  io.axi.ar.bits.lock  := 0.U
  io.axi.ar.bits.cache := 0.U
  io.axi.ar.bits.prot  := 0.U
  io.axi.ar.valid      := readState === readWaitForAR

  io.axi.r.ready := readState === readWaitForR

  io.axi.aw.bits.id    := Constants.DATA_ID
  io.axi.aw.bits.addr  := Cat(writeAddressReg(31, 2), 0.U(2.W))
  io.axi.aw.bits.len   := 0.U(4.W)
  io.axi.aw.bits.size  := "b10".U(2.W)
  io.axi.aw.bits.burst := "b01".U(2.W)
  io.axi.aw.bits.lock  := 0.U
  io.axi.aw.bits.cache := 0.U
  io.axi.aw.bits.prot  := 0.U
  io.axi.aw.valid      := writeState === writeAW

  io.axi.w.bits.id   := Constants.DATA_ID
  io.axi.w.bits.data := writeDataReg
  io.axi.w.bits.strb := writeMaskReg
  io.axi.w.bits.last := true.B
  io.axi.w.valid     := writeState === writeTransfer

  /** ignore b channel completely */
  io.axi.b.ready := writeState === writeFinish
  //-----------------------------------------------------------------------------
  //------------------fsm transformation--------------------------------------
  //-----------------------------------------------------------------------------
  switch(readState) {
    is(readIdle) {
      // check the delayed read data register
      when(io.request.fire && io.request.bits.writeMask === 0.U) {
        readAddressReg := Cat(io.request.bits.tag, io.request.bits.physicalIndex)
        readState      := readWaitForAR
      }
    }
    is(readWaitForAR) {
      when(io.axi.ar.fire) {
        readState := readWaitForR
      }
    }
    is(readWaitForR) {
      when(io.axi.r.fire) {
        readState := readIdle
      }
    }
  }

  switch(writeState) {
    is(writeIdle) {
      when(io.request.fire && io.request.bits.writeMask =/= 0.U) {
        writeAddressReg := Cat(io.request.bits.tag, io.request.bits.physicalIndex)
        writeMaskReg    := io.request.bits.writeMask
        writeDataReg    := io.request.bits.writeData
        writeState      := writeAW
      }
    }
    is(writeAW) {
      when(io.axi.aw.fire) {
        writeState := writeTransfer
      }
    }
    is(writeTransfer) {
      when(io.axi.w.fire) {
        writeState := Mux(io.axi.b.fire, writeIdle ,writeFinish)
      }
    }
    is(writeFinish) {
      when(io.axi.b.fire) {
        writeState := writeIdle
      }
    }
  }
}
