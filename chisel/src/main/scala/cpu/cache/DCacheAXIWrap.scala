// See README.md for license details.

package cpu.cache

import chisel3._
import chisel3.util.Cat
import common.AXIIO
import common.Constants._
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import cpu.common.DefaultConfig._

class DCacheAXIWrap extends Module {
  val io = IO(new Bundle {
    val axi = AXIIO.master()
    val rData = Flipped(new NiseSramReadIO)
    val wData = Flipped(new NiseSramWriteIO)
    val exeAddr = Input(UInt(addrLen.W))
  })
  /** The address of rData and wData will be the same */
  val addr = io.rData.addr
  val cachedTrans = addr(31, 29) =/= "b101".U

  val dCache = Module(new DCache)

  io.axi := DontCare

  io.axi.ar.bits.id := DATA_ID
  io.axi.ar.bits.addr := Mux(cachedTrans, Cat(0.U(3.W), addr(28, 5), 0.U(5.W)), virToPhy(addr))
  io.axi.ar.bits.len := Mux(cachedTrans, "b0111".U(4.W), 0.U(4.W)) // 8 or 1
  io.axi.ar.bits.size := "b010".U(3.W) // 4 Bytes
  io.axi.ar.bits.burst := "b01".U(2.W) // Incrementing-address burst
  io.axi.ar.bits.lock := 0.U
  io.axi.ar.bits.cache := 0.U
  io.axi.ar.bits.prot := 0.U
  io.axi.ar.valid := Mux(cachedTrans, dCache.io.miss, io.rData.enable)

  io.rData.data := Mux(cachedTrans, dCache.io.cpu_data, io.axi.r.bits.data)
  io.rData.valid :=
    Mux(cachedTrans,dCache.io.hit,
      Mux(io.axi.r.bits.id === DATA_ID, io.axi.r.valid,
        false.B))

  io.axi.aw.bits.id := DATA_ID
  io.axi.aw.bits.addr := Mux(cachedTrans, Cat(0.U(3.W), dCache.io.bus_wAddr(28,0)), virToPhy(addr))
  io.axi.aw.bits.len := Mux(cachedTrans, "b0111".U(4.W), 0.U(4.W))
  io.axi.aw.bits.size := "b010".U(3.W)
  io.axi.aw.bits.burst := "b01".U(2.W)
  io.axi.aw.bits.lock := 0.U
  io.axi.aw.bits.cache := 0.U
  io.axi.aw.bits.prot := 0.U
  io.axi.aw.valid := Mux(cachedTrans, dCache.io.bus_writeBack, io.wData.enable)

  io.axi.w.bits.id := DATA_ID
  io.axi.w.bits.data := Mux(cachedTrans, dCache.io.bus_wData, io.wData.data)
  io.axi.w.bits.strb := Mux(cachedTrans, "b1111".U(4.W), io.wData.sel)
  io.axi.w.bits.last := Mux(cachedTrans, dCache.io.bus_wLast, true.B)
  io.axi.w.valid := Mux(cachedTrans, dCache.io.bus_wValid, io.wData.enable)

  io.wData.valid :=
    Mux(cachedTrans, dCache.io.wResp,
      Mux(io.axi.b.bits.id === DATA_ID, io.axi.b.valid,
        false.B))

  dCache.io.bus_rData := io.axi.r.bits.data
  dCache.io.bus_rValid := io.axi.r.bits.id === DATA_ID && io.axi.r.valid
  dCache.io.bus_wReady := io.axi.w.ready
  dCache.io.bus_bValid := io.axi.b.valid
  
  dCache.io.cpu_addr := addr
  dCache.io.cpu_ren := cachedTrans && io.rData.enable
  dCache.io.cpu_wen := cachedTrans && io.wData.enable
  dCache.io.cpu_wData := io.wData.data
  dCache.io.cpu_wSel := io.wData.sel
  dCache.io.cpu_exeAddr := io.exeAddr

  /** just erase high 3 bits */
  def virToPhy(addr: UInt): UInt = {
    require(addr.getWidth == addrLen)
    Cat(0.U(3.W), addr(28,0))
  }
}
