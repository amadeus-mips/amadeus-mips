package memory.axi

import chisel3._
import chisel3.util._
import shared.{AXIIO, CircularShifter}

/**
  * a ram axi slave end with fixed burst length and fixed burst type of wrap
  * @param id the id in the axi graph
  * @param qSize the length of the address passed in
  * @param burstLength in beats, the length of the burst
  * @param burstSize in bytes, the size of the burst
  */
class AXIWrapToSRAM(id: UInt, burstLength: Int = 16, burstSize: Int = 4) extends Module {
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
//  val readCounter = Counter(burstLength)
//  val readCounter = Reg(UInt(log2Ceil(burstLength).W))
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
}
