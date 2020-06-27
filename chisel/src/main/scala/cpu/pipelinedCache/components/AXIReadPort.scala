package cpu.pipelinedCache.components

import axi.AXIIO
import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._

/**
  * AXI read port is an interface to AXI Bus
  *
  * @param addrReqWidth : how wide should the address request be
  * @param AXIID        : what is the AXI ID for this port (hard coded)
  * @param burstLen     : What's the address length for the axi port (hard coded)
  */
@chiselName
class AXIReadPort(addrReqWidth: Int = 32, AXIID: Int, burstLen: Int) extends Module {
  val io = IO(new Bundle {
    val axi = AXIIO.master()
    val addrReq = Flipped(Decoupled(UInt(addrReqWidth.W)))
    val transferData = Valid(UInt(32.W))
  })

  // requirements for the parameters
  require(addrReqWidth <= 32, "address should be less than 32 bits wide")
  require(burstLen <= 16, "burst length should not be larger than 16 in axi3")

  val readIdle :: readWaitForAR :: readTransfer :: Nil = Enum(3)
  val readState = RegInit(readIdle)
  val readAddressReg = Reg(UInt(32.W))

  // axi signals
  io.axi.ar.bits.id := AXIID.U
  io.axi.ar.bits.addr := readAddressReg
  io.axi.ar.bits.len := (burstLen - 1).U(4.W)
  io.axi.ar.bits.size := "b010".U(3.W) // always 4 bytes
  if (burstLen == 1) {
    io.axi.ar.bits.burst := "b01".U(2.W)
  } else {
    io.axi.ar.bits.burst := "b10".U(2.W)
  } // axi wrap burst

  io.axi.ar.bits.lock := 0.U
  io.axi.ar.bits.cache := 0.U
  io.axi.ar.bits.prot := 0.U

  // valid and ready signals
  io.axi.ar.valid := readState === readWaitForAR
  io.axi.r.ready := readState === readTransfer

  io.addrReq.ready := readState === readIdle
  io.transferData.valid := readState === readTransfer && io.axi.r.fire()
  io.transferData.bits := io.axi.r.bits.data

  switch(readState) {
    is(readIdle) {
      when(io.addrReq.valid) {
        readState := readWaitForAR
        readAddressReg := io.addrReq.bits
      }
    }
    is(readWaitForAR) {
      when(io.axi.ar.fire) {
        readState := readTransfer
      }
    }
    is(readTransfer) {
      when(io.axi.r.fire && io.axi.r.bits.last) {
        readState := readIdle
      }
    }
  }
}
