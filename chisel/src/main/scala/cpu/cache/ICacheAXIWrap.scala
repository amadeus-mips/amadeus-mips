// See README.md for license details.

package cpu.cache

import chisel3._
import chisel3.util.{log2Ceil, Cat}
import common.AXIIO
import common.Constants._
import cpu.common.NiseSramReadIO
import cpu.common.DefaultConfig._

class ICacheAXIWrap(depth: Int = 128, bankAmount: Int = 16) extends Module {
  val io = IO(new Bundle {
    val axi = AXIIO.master()
    val rInst = Flipped(new NiseSramReadIO)
    val flush = Input(Bool())
  })
  // this is not a circuit component, so use require ( at elaboration time ) instead of assert
  require(bankAmount <= 16 && bankAmount >= 1, s"bank amount is $bankAmount! Need between 1 and 16")

  val addr = io.rInst.addr
  val cachedTrans = true.B

  val iCache = Module(new ICache(depth, bankAmount))

  io.axi := DontCare

  io.axi.ar.bits.id := INST_ID
  io.axi.ar.bits.addr := Mux(
    cachedTrans,
    Cat(0.U(3.W), addr(28, 2 + log2Ceil(bankAmount)), 0.U((2 + log2Ceil(bankAmount)).W)),
    virToPhy(addr)
  )
  io.axi.ar.bits.len := Mux(cachedTrans, (bankAmount - 1).U(4.W), 0.U(4.W)) // 8 or 1
  io.axi.ar.bits.size := "b010".U(3.W) // 4 Bytes
  io.axi.ar.bits.burst := "b01".U(2.W) // Incrementing-address burst
  io.axi.ar.bits.lock := 0.U
  io.axi.ar.bits.cache := 0.U
  io.axi.ar.bits.prot := 0.U
  io.axi.ar.valid := !io.flush && Mux(cachedTrans, iCache.io.miss, io.rInst.enable)

  io.rInst.valid := cachedTrans && iCache.io.hit
  io.rInst.data := iCache.io.inst

  iCache.io.busData.bits := io.axi.r.bits.data
  iCache.io.busData.valid := io.axi.r.bits.id === INST_ID && io.axi.r.valid
  iCache.io.flush := io.flush
  iCache.io.addr.bits := io.rInst.addr
  iCache.io.addr.valid := io.rInst.enable

  /** just erase high 3 bits */
  def virToPhy(addr: UInt): UInt = {
    require(addr.getWidth == addrLen)
    Cat(0.U(3.W), addr(28, 0))
  }
}
