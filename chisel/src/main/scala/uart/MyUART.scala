package uart

import axi.AXIIO
import chisel3._
import chisel3.util._

class MyUART extends Module {
  val io = IO(new Bundle {
    val axi        = AXIIO.slave()
    val interrupt  = Output(Bool())
    val inputData  = Flipped(Decoupled(UInt(8.W)))
    val outputData = Valid(UInt(8.W))
  })

  // offset 0
  val datReg = RegInit(0.U(8.W))
  val tllReg = RegInit(0.U(8.W)) // Don't care
  // offset 1
  val ierReg = RegInit(0.U(8.W))
  val tlhReg = RegInit(0.U(8.W)) // Don't care
  // offset 2
  val iirReg  = RegInit("hc1".U(8.W)) // read only
  val fifoReg = RegInit("hc0".U(8.W)) // write only
  // offset 3
  val lcrReg = RegInit(3.U(8.W))
  // offset 4
  val mcrReg = RegInit(0.U(8.W))
  // offset 5
  val lsrReg = RegInit(0.U(8.W))
  // offset 6
  val msrReg = RegInit(0.U(8.W))

  val readAddress  = io.axi.ar.bits.addr(2, 0)
  val writeAddress = io.axi.aw.bits.addr(2, 0)

  val useTL = lcrReg(7)

  val readAddrReg  = Reg(UInt(3.W))
  val writeAddrReg = Reg(UInt(3.W))
  val writeDataReg = Reg(UInt(8.W))

  val writeAddrWire = WireInit(writeAddrReg)

  val rwIdle :: arHandshake :: rTransfer :: awHandshake :: wTransfer :: bTransfer :: Nil = Enum(6)
  val rwState                                                                            = RegInit(rwIdle)

  io.axi.ar.ready    := rwState === arHandshake
  io.axi.r.valid     := rwState === rTransfer
  io.axi.r.bits.last := rwState === rTransfer
  io.axi.r.bits.data := MuxCase(
    0.U,
    Array(
      (readAddrReg === 0.U) -> Mux(lcrReg(7), tllReg, datReg),
      (readAddrReg === 1.U) -> Mux(lcrReg(7), tlhReg, ierReg),
      (readAddrReg === 2.U) -> iirReg,
      (readAddrReg === 3.U) -> lcrReg,
      (readAddrReg === 4.U) -> mcrReg,
      (readAddrReg === 5.U) -> lsrReg,
      (readAddrReg === 6.U) -> msrReg
    )
  )
  io.axi.aw.ready := rwState === awHandshake
  io.axi.w.ready  := rwState === wTransfer
  io.axi.b.valid  := rwState === bTransfer

  io.outputData.bits  := writeDataReg
  io.outputData.valid := rwState === bTransfer && writeAddrReg === 0.U && lcrReg(7) === 0.U

  io.inputData.ready := rwState === rwIdle && lsrReg(0) === 0.U

  io.interrupt := lsrReg(0) === 1.U

  switch(rwState) {
    is(rwIdle) {
      when(io.inputData.fire) {
        assert(lsrReg(0) === 0.U)
        lsrReg := lsrReg | "b00000001".U(8.W)
      }
      when(io.axi.ar.valid) {
        rwState := arHandshake
      }.otherwise {
        when(io.axi.aw.valid) {
          rwState := awHandshake
        }
      }
    }
    is(arHandshake) {
      // valid should be asserted until handshake
      readAddrReg := readAddress
      rwState     := rTransfer
    }
    is(rTransfer) {
      when(io.axi.r.fire) {
        when(readAddrReg === 0.U && lcrReg(7) === 0.U) {
          lsrReg := lsrReg & "b11111110".U(8.W)
        }
        rwState := rwIdle
      }
    }
    is(awHandshake) {
      writeAddrReg := writeAddress
      rwState      := wTransfer
    }
    is(wTransfer) {
      when(io.axi.w.fire) {
        when(writeAddrWire(1, 0) === 0.U) {
          writeAddrReg := MuxCase(
            writeAddrWire,
            Array(
              (io.axi.w.bits.strb(1)) -> (writeAddrWire + 1.U),
              (io.axi.w.bits.strb(2)) -> (writeAddrWire + 2.U),
              (io.axi.w.bits.strb(3)) -> (writeAddrWire + 3.U)
            )
          )
        }
        writeDataReg := io.axi.w.bits.data(7, 0)
        rwState      := bTransfer
      }
    }
    is(bTransfer) {
      when(io.axi.b.fire) {
        when(writeAddrReg === 0.U) {
          when(lcrReg(7) === 0.U) {
            io.outputData.valid := true.B
          }.otherwise {
            tllReg := writeDataReg
          }
        }.elsewhen(writeAddrReg === 1.U) {
            when(lcrReg(7) === 0.U) {
              ierReg := writeDataReg
            }.otherwise {
              tlhReg := writeDataReg
            }
          }
          .elsewhen(writeAddrReg === 2.U) {
            iirReg := writeDataReg
          }
          .elsewhen(writeAddrReg === 3.U) {
            lcrReg := writeDataReg
          }
          .elsewhen(writeAddrReg === 4.U) {
            mcrReg := writeDataReg
          }
          .elsewhen(writeAddrReg === 5.U) {
            lsrReg := writeDataReg
          }
          .elsewhen(writeAddrReg === 6.U) {
            msrReg := writeDataReg
          }
      }
    }
  }

  def write(oldData: UInt, newData: UInt, mask: UInt): Unit = {
    require(oldData.getWidth == 8)
    require(newData.getWidth == 8)
    require(mask.getWidth == 8)
  }
}
