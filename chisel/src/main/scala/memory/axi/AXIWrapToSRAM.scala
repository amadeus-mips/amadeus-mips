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

  //-----------------------------------------------------------------------------
  //------------------set up the fsm--------------------------------------
  //-----------------------------------------------------------------------------
  val sIdle :: sWaitForR :: sTransfer :: Nil = Enum(3)
  val state = RegInit(sIdle)
  val checkCounter = Reg(UInt(log2Ceil(burstLength).W))
  val addrReg = Reg(UInt(32.W))
  //-----------------------------------------------------------------------------
  //------------------set up the default IO--------------------------------------
  //-----------------------------------------------------------------------------
  io.bus := DontCare
  io.ram := DontCare
  io.bus.ar.ready := state === sIdle
  // is always valid during the transfer
  io.bus.r.bits.last := false.B
  // response should always be ok
  io.bus.r.bits.resp := 0.U
  io.bus.r.valid := state === sTransfer
  io.ram.read.enable := false.B
  // disable write completely
  io.ram.write := DontCare
  //-----------------------------------------------------------------------------
  //------------------switch the states--------------------------------------
  //-----------------------------------------------------------------------------
  switch(state) {
    is(sIdle) {
      when(io.bus.ar.fire) {
        state := sWaitForR
        addrReg := io.bus.ar.bits.addr
        checkCounter := 0.U
      }
    }
    is(sWaitForR) {
      when(io.bus.r.ready) {
        state := sTransfer
      }
    }
    is(sTransfer) {
      when(io.bus.r.fire) {
        io.ram.read.enable := true.B
        io.ram.read.addr := addrReg
        assert(io.ram.read.valid === true.B, "combinational ram should always have output")
        checkCounter := checkCounter + 1.U;
        assert(addrReg(1, 0) === 0.U, "address should be aligned")
//        assert(addrReg.getWidth == 20, "address width should be 20")
        addrReg := Cat(addrReg(31, 6), (addrReg(5, 2) + 1.U), 0.U(2.W))
        io.bus.r.bits.data := io.ram.read.data
      }
      when(checkCounter === 15.U) {
        io.bus.r.bits.last := true.B
        state := sIdle
      }
    }
  }

  //-----------------------------------------------------------------------------
  //------------------here is the write ports------------------------------------
  //-----------------------------------------------------------------------------
  require(isPow2(burstLength), " burst length should be a power of 2")
  // assume the write has burst type of Incr, because wrap does not make much sense
  assert(io.bus.aw.bits.burst === 2.U && io.bus.aw.valid, "burst type should be incr")
  // the idle state is xIdle, and ready is asserted
  // when the it is no longer idle, it enters the wait/transfer stage
  // after the transfer/handshake, it stays at the commit stage
  // after aw and w idle is at the commit stage, b idle is asserted
  val awIdle :: awCommit :: Nil = Enum(2)
  val wIdle :: wRefill :: wCommit :: Nil = Enum(3)
  val bIdle :: bCommit :: Nil = Enum(2)

  val awState = RegInit(awIdle)
  val wState = RegInit(wIdle)
  val bState = RegInit(bIdle)

  // the register to hold the address
  val awAddressReg = Reg(UInt(32.W))
  // as aw size is a fixed version of 4 byte, the line is burst len * 4 * 8
  val wCacheLineReg = Reg(Vec(burstLength, UInt(32.W)))
  // only support incr write, so write mask always starts at 0
  val writeMask = RegInit(0.U(log2Ceil(burstLength).W))

  val isBothCommit = (awState === awCommit) && (wState === wCommit)

  io.bus.aw.ready := true.B
  switch(awState) {
    is(awIdle) {
      when(io.bus.aw.fire()) {
        awState := awCommit
        awAddressReg := io.bus.aw.bits.addr
      }
    }
    is(awCommit) {
      when(isBothCommit) {
        awState := awIdle
      }
    }
  }

  /**
    * the structure of the ram is not really good out-of-order
    * aw and w channel because it could only handle one request
    * every cycle
    */
  io.bus.aw.ready := true.B
  switch(wState) {
    is(wIdle) {
      when(io.bus.w.fire()) {
        wCacheLineReg(writeMask) := io.bus.w.bits.data
        writeMask := writeMask + 1.U
      }
      when(io.bus.w.bits.last) {
        wState := wCommit
      }
    }
    is(wCommit) {
      when(isBothCommit) {
        awState := awIdle
      }
    }
  }

  io.bus.w.ready := true.B
}
