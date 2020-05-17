package memory.axi

import chisel3._
import chisel3.util._
import shared.{AXIIO, CircularShifter}

/**
  * a ram axi slave end with fixed burst length and fixed burst type of wrap
  * the write port has a fixed burst type of incr
  * @param id the id in the axi graph
  * @param burstLength in beats, the length of the burst
  */
class AXIWrapToSRAM(id: UInt, burstLength: Int = 16) extends Module {
  val io = IO(new Bundle {
    val bus = AXIIO.slave()
    val ram = new SimpleSramIO
  })
//  require(burstLength == 16, "the burst length should be 16")

  //-----------------------------------------------------------------------------
  //------------------set up the fsm--------------------------------------
  //-----------------------------------------------------------------------------
  // determines read or write takes control
  val portIdle :: portRead :: portWrite :: Nil = Enum(3)
  val portState = RegInit(portIdle)
  // the state of read
  val rIdle :: rTransfer :: rFinish :: Nil = Enum(3)
  val readState = RegInit(rIdle)

  val readCheckCounter = Reg(UInt(log2Ceil(burstLength).W))
  val readAddrReg = Reg(UInt(32.W))
  // the state of write
  val wIdle :: wTransfer :: wResponse :: wFinish :: Nil = Enum(4)
  val writeState = RegInit(wIdle)

  val writeAddrReg = Reg(UInt(32.W))

  //-----------------------------------------------------------------------------
  //------------------set up the default IO--------------------------------------
  //-----------------------------------------------------------------------------
  io.bus := DontCare
  io.ram := DontCare

  // default signals for read
  io.bus.ar.ready := readState === rIdle && portState === portRead
  io.bus.r.bits.last := false.B

  // response should always be ok
  io.bus.r.bits.resp := 0.U

  io.bus.r.valid := readState === rTransfer
  io.ram.read.enable := false.B

  assert(
    (readState === rIdle && portState =/= portRead) || (portState === portRead),
    "when port state is not port read, the read state should always be idle"
  )
  assert(!io.bus.ar.fire || (io.bus.ar.fire && io.bus.ar.bits.len === burstLength.U))
  assert(!io.bus.aw.fire || (io.bus.aw.fire && io.bus.aw.bits.len === burstLength.U))

  // default signals for write
  // NO interleaving, assumes in order transfer
  io.bus.aw.ready := writeState === wIdle && portState === portWrite

  io.bus.w.ready := writeState === wTransfer && portState === portWrite

  io.bus.b.valid := writeState === wResponse
  io.bus.b.bits.resp := 0.U // fixed OK
  io.bus.b.bits.id := id

  io.ram.write.addr := writeAddrReg
  io.ram.write.enable := false.B
  io.ram.write.sel := io.bus.w.bits.strb
  io.ram.write.data := io.bus.w.bits.data
  assert(
    (writeState === wIdle && portState =/= portWrite) || (portState === portWrite),
    "when the port state is not port write, the write state should always be idle"
  )
  //-----------------------------------------------------------------------------
  //------------------the FSM determines who gets which port---------------------
  //-----------------------------------------------------------------------------

  // this is a single port, cannot read and write at the same cycle
  // a global FSM to determine read/write

  switch(portState) {
    is(portIdle) {
      // when read valid is asserted, serve read first
      when(io.bus.ar.valid) {
        portState := portRead
        // when write valid is asserted, serve write if read valid is not asserted
      }.elsewhen(io.bus.aw.valid) {
        // write could starve, but in reality, if icache keep sending read, and dcache
        // keep sending read, the write channel will be available in the end
        portState := portWrite
      }
    }
    is(portRead) {
      when(readState === rFinish) {
        portState := portIdle
      }
    }
    is(portWrite) {
      // at the response stage, write does not use the write port anymore
      when(writeState === wFinish) {
        portState := portIdle
      }
    }
  }
  //-----------------------------------------------------------------------------
  //------------------switch the readStates--------------------------------------
  //-----------------------------------------------------------------------------
  switch(readState) {
    is(rIdle) {
      when(io.bus.ar.fire) {
        readState := rTransfer
        readAddrReg := io.bus.ar.bits.addr
        readCheckCounter := 0.U
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
        readAddrReg := Cat(readAddrReg(31, 2+log2Ceil(burstLength)), (readAddrReg(1+log2Ceil(burstLength), 2) + 1.U), 0.U(2.W))
        io.bus.r.bits.data := io.ram.read.data
      }
      when(readCheckCounter === (burstLength-1).U) {
        io.bus.r.bits.last := true.B
        readState := rFinish
      }
    }
    is(rFinish) {
      readState := rIdle
    }
  }

  //-----------------------------------------------------------------------------
  //------------------here is the write ports------------------------------------
  //-----------------------------------------------------------------------------
  require(isPow2(burstLength), " burst length should be a power of 2")
  // assume the write has burst type of Incr, because wrap does not make much sense
//  assert(io.bus.aw.bits.burst === 1.U && io.bus.aw.valid && portState === portWrite, "burst type should be incr")

  // the idle readState is xIdle, and ready is asserted
  // when the it is no longer idle, it enters the wait/transfer stage
  // after the transfer/handshake, it stays at the commit stage
  // after aw and w idle is at the commit stage, b idle is asserted

  //-----------------------------------------------------------------------------
  //------------------the fsm --------------------------------------
  //-----------------------------------------------------------------------------

  switch(writeState) {
    is(wIdle) {
      when(io.bus.aw.fire()) {
        writeState := wTransfer
        writeAddrReg := io.bus.aw.bits.addr
        assert(io.bus.aw.bits.addr(4,2) === 0.U, "the burst type should be INCR")
      }
    }
    is(wTransfer) {
      // this assumes that the underlying memory is always available
      when(io.bus.w.fire()) {
        writeAddrReg := writeAddrReg + 4.U
        io.ram.write.enable := true.B
//        io.ram.write.data := io.bus.w.bits.data
      }
      when(io.bus.w.bits.last) {
        writeState := wResponse
      }
    }
    is(wResponse) {
      when(io.bus.b.fire()) {
        writeState := wFinish
      }
    }
    is(wFinish) {
      writeState := wIdle
    }
  }
}
