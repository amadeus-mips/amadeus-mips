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
  val lsrReg = RegInit("h20".U(8.W)) // always can be wrote
  // offset 6
  val msrReg = RegInit(0.U(8.W))

  val useTL = lcrReg(7)

  val readAddrReg  = Reg(UInt(3.W))
  val writeAddrReg = Reg(UInt(3.W))

  val rIdle :: rTransfer :: Nil = Enum(2)

  val rState = RegInit(rIdle)
  val readId = RegInit(0.U(4.W))
  switch(rState) {
    is(rIdle) {
      when(io.axi.ar.fire()) {
        rState      := rTransfer
        readAddrReg := io.axi.ar.bits.addr(2, 0)
        readId      := io.axi.ar.bits.id
      }
    }
    is(rTransfer) {
      when(io.axi.r.fire()) {
        rState := rIdle
      }
    }
  }

  io.axi.ar.ready    := rState === rIdle
  io.axi.r.valid     := rState === rTransfer
  io.axi.r.bits.id   := readId
  io.axi.r.bits.last := rState === rTransfer
  io.axi.r.bits.resp := 0.U
  io.axi.r.bits.data := Fill(
    4,
    MuxLookup(
      readAddrReg,
      0.U,
      Array(
        0.U -> Mux(useTL, tllReg, datReg),
        1.U -> Mux(useTL, tlhReg, ierReg),
        2.U -> iirReg,
        3.U -> lcrReg,
        4.U -> mcrReg,
        5.U -> lsrReg,
        6.U -> msrReg
      )
    )
  )

  val wIdle :: wTransfer :: bResp :: Nil = Enum(3)

  val wState  = RegInit(wIdle)
  val writeId = RegInit(0.U(4.W))

  val writeAddress = MuxCase(
    writeAddrReg,
    Seq(
      io.axi.w.bits.strb(1) -> (writeAddrReg + 1.U),
      io.axi.w.bits.strb(2) -> (writeAddrReg + 2.U),
      io.axi.w.bits.strb(3) -> (writeAddrReg + 3.U)
    )
  )
  val writeData = MuxCase(
    io.axi.w.bits.data(7, 0),
    Seq(
      io.axi.w.bits.strb(1) -> io.axi.w.bits.data(15, 8),
      io.axi.w.bits.strb(2) -> io.axi.w.bits.data(23, 16),
      io.axi.w.bits.strb(3) -> io.axi.w.bits.data(31, 24)
    )
  )
  switch(wState) {
    is(wIdle) {
      when(io.axi.aw.fire()) {
        wState       := wTransfer
        writeAddrReg := io.axi.aw.bits.addr(2, 0)
        writeId      := io.axi.aw.bits.id
      }
    }
    is(wTransfer) {
      when(io.axi.w.fire()) {
        wState := bResp
        switch(writeAddress) {
          is(0.U) {
            when(useTL === 0.U) {
              datReg := writeData
            }.otherwise {
              tllReg := writeData
            }
          }
          is(1.U) {
            when(useTL === 0.U) {
              ierReg := writeData
            }.otherwise {
              tlhReg := writeData
            }
          }
          is(2.U) {
            fifoReg := writeData
          }
          is(3.U) {
            lcrReg := writeData
          }
          is(4.U) {
            mcrReg := writeData
          }
          is(5.U) {
//            lsrReg := writeData // readonly
          }
          is(6.U) {
//            msrReg := writeData // readonly
          }
        }
      }
    }
    is(bResp) {
      when(io.axi.b.fire()) {
        wState := wIdle
      }
    }
  }

  io.axi.aw.ready    := wState === wIdle
  io.axi.w.ready     := wState === wTransfer
  io.axi.b.valid     := wState === bResp
  io.axi.b.bits.id   := writeId
  io.axi.b.bits.resp := 0.U

  io.outputData.bits  := writeData
  io.outputData.valid := wState === wTransfer && io.axi.w.fire() && !useTL && writeAddress === 0.U

  io.inputData.ready := lsrReg(0) === 0.U
  when(io.inputData.fire()) {
    datReg := io.inputData.bits
    lsrReg := lsrReg | "b00000001".U(8.W)
  }.elsewhen(io.axi.r.fire() && readAddrReg === 0.U && !useTL) {
    lsrReg := lsrReg & "b11111110".U(8.W)
  }

  io.interrupt := lsrReg(0) === 1.U

  def write(oldData: UInt, newData: UInt, mask: UInt): Unit = {
    require(oldData.getWidth == 8)
    require(newData.getWidth == 8)
    require(mask.getWidth == 8)
  }
}
