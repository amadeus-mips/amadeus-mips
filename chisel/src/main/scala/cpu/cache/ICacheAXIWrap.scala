// See README.md for license details.

package cpu.cache

import chisel3._
import chisel3.util.Cat
import common.AXIMasterIO
import common.Constants._
import cpu.common.NiseSramReadIO
import cpu.common.DefaultConfig._

class ICacheAXIWrap extends Module {
  val io = IO(new Bundle {
    val axi = new AXIMasterIO
    val rInst = Flipped(new NiseSramReadIO)
    val flush = Input(Bool())
  })

  val addr = io.rInst.addr
  val cachedTrans = true.B

  val iCache = Module(new ICache)

  io.axi := DontCare

  io.axi.ar.id := INST_ID
  io.axi.ar.addr := Mux(cachedTrans, Cat(0.U(3.W), addr(28, 5), 0.U(5.W)), virToPhy(addr))
  io.axi.ar.len := Mux(cachedTrans, "b0111".U(4.W), 0.U(4.W)) // 8 or 1
  io.axi.ar.size := "b010".U(3.W) // 4 Bytes
  io.axi.ar.burst := "b01".U(2.W) // Incrementing-address burst
  io.axi.ar.lock := 0.U
  io.axi.ar.cache := 0.U
  io.axi.ar.prot := 0.U
  io.axi.ar.valid := !io.flush && Mux(cachedTrans, iCache.io.miss, io.rInst.enable)

  io.rInst.valid := cachedTrans && iCache.io.hit
  io.rInst.data := iCache.io.inst

  iCache.io.busData.bits := io.axi.r.data
  iCache.io.busData.valid := io.axi.r.id === INST_ID && io.axi.r.valid
  iCache.io.flush := io.flush
  iCache.io.addr.bits := io.rInst.addr
  iCache.io.addr.valid := io.rInst.enable

  /** just erase high 3 bits */
  def virToPhy(addr: UInt): UInt = {
    require(addr.getWidth == addrLen)
    Cat(0.U(3.W), addr(28,0))
  }
}
