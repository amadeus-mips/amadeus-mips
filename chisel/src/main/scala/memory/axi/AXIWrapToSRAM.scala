package memory.axi

import chisel3._
import chisel3.util._
import shared.{AXIIO, CircularShifter}

/**
  * a ram axi slave end with fixed burst length and fixed burst type of wrap
  * @param id the id in the axi graph
  * @param burstLength in beats, the length of the burst
  */
class AXIWrapToSRAM(id: UInt, burstLength: Int = 16) extends Module {
  val io = IO(new Bundle {
    val bus = AXIIO.slave()
    val ram = new SimpleSramIO
  })
//  assert(
//    io.bus.ar.valid && (io.bus.ar.bits.burst(1) || io.bus.ar.bits.burst(0)),
//    "when ar is valid, the burst type must be wrap or incr"
//  )
  require(burstLength == 16, "the burst length should be 16")

  // this is a single port, cannot read and write at the same cycle
  // a global FSM to determine read/write
  val portIdle :: portRead :: portWrite :: Nil = Enum(3)
  val portState = RegInit(portIdle)
  switch(portState) {
    is(portIdle) {
      // when read valid is asserted, serve read first
      when(io.bus.ar.valid) {
        portState := portRead
        // when write valid is asserted, serve write if read valid is not asserted
      }.elsewhen(io.bus.aw.valid) {
        //TODO: WARNING: write could starve!
        portState := portWrite
      }
    }
    is(portRead) {
      when(readState === rIdle) {
        portState := portIdle
      }
    }
    is(portWrite) {
      // at the response stage, write does not use the write port anymore
      when(writeState === wResponse) {
        portState := portIdle
      }
    }
  }
  //-----------------------------------------------------------------------------
  //------------------set up the fsm--------------------------------------
  //-----------------------------------------------------------------------------
  val rIdle :: rWaitForR :: rTransfer :: Nil = Enum(3)
  val readState = RegInit(rIdle)
  val readCheckCounter = Reg(UInt(log2Ceil(burstLength).W))
  val readAddrReg = Reg(UInt(32.W))
  //-----------------------------------------------------------------------------
  //------------------set up the default IO--------------------------------------
  //-----------------------------------------------------------------------------
  io.bus := DontCare
  io.ram := DontCare
  // ar ready has a signal of low by default
  io.bus.ar.ready := readState === rIdle && (portState === portIdle || portState === portRead)
  // is always valid during the transfer
  io.bus.r.bits.last := false.B
  // response should always be ok
  io.bus.r.bits.resp := 0.U
  io.bus.r.valid := readState === rTransfer
  io.ram.read.enable := false.B
  //-----------------------------------------------------------------------------
  //------------------switch the readStates--------------------------------------
  //-----------------------------------------------------------------------------
  switch(readState) {
    is(rIdle) {
      when(io.bus.ar.fire) {
        readState := rWaitForR
        readAddrReg := io.bus.ar.bits.addr
        readCheckCounter := 0.U
      }
    }
    is(rWaitForR) {
      when(io.bus.r.ready) {
        readState := rTransfer
      }
    }
    is(rTransfer) {
      when(io.bus.r.fire) {
        io.ram.read.enable := true.B
        io.ram.read.addr := readAddrReg
        assert(io.ram.read.valid === true.B, "combinational ram should always have output")
        readCheckCounter := readCheckCounter + 1.U;
        assert(readAddrReg(1, 0) === 0.U, "address should be aligned")
//        assert(readAddrReg.getWidth == 20, "address width should be 20")
        readAddrReg := Cat(readAddrReg(31, 6), (readAddrReg(5, 2) + 1.U), 0.U(2.W))
        io.bus.r.bits.data := io.ram.read.data
      }
      when(readCheckCounter === 15.U) {
        io.bus.r.bits.last := true.B
        readState := rIdle
      }
    }
  }

  //-----------------------------------------------------------------------------
  //------------------here is the write ports------------------------------------
  //-----------------------------------------------------------------------------
  require(isPow2(burstLength), " burst length should be a power of 2")
  // assume the write has burst type of Incr, because wrap does not make much sense
  assert(io.bus.aw.bits.burst === 2.U && io.bus.aw.valid, "burst type should be incr")
  // the idle readState is xIdle, and ready is asserted
  // when the it is no longer idle, it enters the wait/transfer stage
  // after the transfer/handshake, it stays at the commit stage
  // after aw and w idle is at the commit stage, b idle is asserted
  val wIdle :: wTransfer :: wResponse :: Nil = Enum(4)
  val writeState = RegInit(wIdle)

  val writeAddrReg = Reg(UInt(32.W))
  val writeOffsetReg = Reg(UInt(log2Ceil(burstLength).W))
  //-----------------------------------------------------------------------------
  //------------------default signals--------------------------------------
  //-----------------------------------------------------------------------------
  //TODO: can't both be high by default
  io.bus.aw.ready := portState === portWrite

  io.bus.w.ready := writeState === wTransfer && portState === portWrite

  io.bus.b.valid := writeState === wResponse
  io.bus.b.bits.resp := 0.U // fixed OK
  io.bus.b.bits.id := id

  io.ram.write.enable := false.B
  io.ram.write.sel := io.bus.w.bits.strb
  io.ram.write.data := io.bus.w.bits.data
  //-----------------------------------------------------------------------------
  //------------------the fsm --------------------------------------
  //-----------------------------------------------------------------------------

  switch(writeState) {
    is(wIdle) {
      when (io.bus.aw.fire()) {
        writeState := wTransfer
      }
    }
    is(wTransfer) {
      when (io.bus.w.fire()) {
        io.ram.write.addr := writeAddrReg
        io.ram.write.enable := true.B
      }
      when (io.bus.w.bits.last) {
        writeState := wResponse
      }
    }
    is(wResponse) {
      when(io.bus.b.fire()) {
        writeState := wIdle
      }
    }
  }
}
