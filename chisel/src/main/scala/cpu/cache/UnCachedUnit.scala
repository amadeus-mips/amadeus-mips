package cpu.cache

import axi.AXIIO
import chisel3._
import chisel3.util._
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import shared.Constants

//TODO: what if trap during write
class UnCachedUnit extends Module {
  val io = IO(new Bundle {
    val rChannel = Flipped(new NiseSramReadIO)
    val wChannel = Flipped(new NiseSramWriteIO)
    val axi = AXIIO.master()
  })

  val readIdle :: readWaitForAR :: readWaitForR :: Nil = Enum(3)
  val readState = RegInit(readIdle)
  val readAddressReg = Reg(UInt(32.W))
  // delay the read data a cycle
  val readDataReg = Reg(UInt(32.W))
//  assert(!io.rChannel.enable || (io.rChannel.enable && io.rChannel.addr(31,29) === "b101".U(3.W)), s"${io.rChannel.addr}")

  val writeIdle :: writeAW :: writeTransfer :: writeFinish :: Nil = Enum(4)
  val writeState = RegInit(writeIdle)
  val writeAddressReg = Reg(UInt(32.W))
  val writeDataReg = Reg(UInt(32.W))
  val writeMaskReg = Reg(UInt(4.W))
  //TODO: does slave support aw and w out of order?

//  assert(!io.wChannel.enable || (io.wChannel.enable && io.wChannel.addr(31,29) === "b101".U(3.W)), s"${io.wChannel.addr}")
  //-----------------------------------------------------------------------------
  //------------------default IO--------------------------------------
  //-----------------------------------------------------------------------------
  io.rChannel.data := readDataReg
  io.rChannel.valid := false.B

  io.wChannel.valid := false.B

  io.axi.ar.bits.id := Constants.DATA_ID
  io.axi.ar.bits.addr := virToPhy(addr = readAddressReg)
  io.axi.ar.bits.len := 0.U(4.W)
  io.axi.ar.bits.size := "b010".U(3.W) // 4 Bytes
  io.axi.ar.bits.burst := "b01".U(2.W) // Incrementing-request burst
  io.axi.ar.bits.lock := 0.U
  io.axi.ar.bits.cache := 0.U
  io.axi.ar.bits.prot := 0.U
  io.axi.ar.valid := readState === readWaitForAR

  io.axi.r.ready := readState === readWaitForR

  io.axi.aw.bits.id := Constants.DATA_ID
  io.axi.aw.bits.addr := virToPhy(addr = writeAddressReg)
  io.axi.aw.bits.len := 0.U(4.W)
  io.axi.aw.bits.size := "b010".U(3.W)
  io.axi.aw.bits.burst := "b01".U(2.W)
  io.axi.aw.bits.lock := 0.U
  io.axi.aw.bits.cache := 0.U
  io.axi.aw.bits.prot := 0.U
  io.axi.aw.valid := writeState === writeAW

  io.axi.w.bits.id := Constants.DATA_ID
  io.axi.w.bits.data := writeDataReg
  io.axi.w.bits.strb := writeMaskReg
  io.axi.w.bits.last := true.B
  io.axi.w.valid := writeState === writeTransfer

  io.axi.b.ready := writeState === writeFinish
  //-----------------------------------------------------------------------------
  //------------------fsm transformation--------------------------------------
  //-----------------------------------------------------------------------------
  switch(readState) {
    is(readIdle) {
      // check the delayed read data register
      when (io.rChannel.enable) {
        readAddressReg := io.rChannel.addr
        readState := readWaitForAR
      }
    }
    is(readWaitForAR) {
      when (io.axi.ar.fire) {
        readState := readWaitForR
      }
    }
    is(readWaitForR) {
      when (io.axi.r.fire) {
        readState := readIdle
        //TODO: check I-D-Cache for this enable
        val sameAddress = readAddressReg === io.rChannel.addr && io.rChannel.enable
        io.rChannel.valid := sameAddress
        readDataReg := io.axi.r.bits.data
        assert(io.axi.r.bits.last)
      }
    }
  }

  switch(writeState) {
    is(writeIdle) {
      when (io.wChannel.enable) {
        writeAddressReg := io.wChannel.addr
        writeMaskReg := io.wChannel.sel
        writeDataReg := io.wChannel.data
        writeState := writeAW
      }
    }
    is(writeAW) {
      when (io.axi.aw.fire) {
        writeState := writeTransfer
      }
    }
    is(writeTransfer) {
      when (io.axi.w.fire) {
        writeState := writeFinish
      }
    }
    is(writeFinish) {
      when (io.axi.b.fire) {
        io.wChannel.valid := writeAddressReg === io.wChannel.addr && io.wChannel.enable
        writeState := writeIdle
      }
    }
  }

  /** just erase high 3 bits */
  def virToPhy(addr: UInt): UInt = {
    require(addr.getWidth == 32)
    Cat(0.U(3.W), addr(28,0))
  }
}
