// See README.md for license details.

package cpu.cache

import chisel3._
import chisel3.util.Cat
import common.AXIMasterIO
import common.Constants._
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import cpu.common.DefaultConfig._

class DCacheAXIWrap extends Module {
  val io = IO(new Bundle {
    val axi = new AXIMasterIO
    val rData = Flipped(new NiseSramReadIO)
    val wData = Flipped(new NiseSramWriteIO)
    val exeAddr = Input(UInt(addrLen.W))
  })
  /** The address of rData and wData will be the same */
  val addr = io.rData.addr
  val cachedTrans = addr(31, 29) =/= "b101".U

  val dCache = Module(new DCache)

  io.axi := DontCare

  io.axi.ar.id := DATA_ID
  io.axi.ar.addr := Mux(cachedTrans, Cat(0.U(3.W), addr(28, 5), 0.U(5.W)), virToPhy(addr))
  io.axi.ar.len := Mux(cachedTrans, "b0111".U(4.W), 0.U(4.W)) // 8 or 1
  io.axi.ar.size := "b010".U(3.W) // 4 Bytes
  io.axi.ar.burst := "b01".U(2.W) // Incrementing-address burst
  io.axi.ar.lock := 0.U
  io.axi.ar.cache := 0.U
  io.axi.ar.prot := 0.U
  io.axi.ar.valid := Mux(cachedTrans, dCache.io.miss, io.rData.enable)

  io.rData.data := Mux(cachedTrans, dCache.io.cpu_data, io.axi.r.data)
  io.rData.valid :=
    Mux(cachedTrans,dCache.io.hit,
      Mux(io.axi.r.id === DATA_ID, io.axi.r.valid,
        false.B))

  io.axi.aw.id := DATA_ID
  io.axi.aw.addr := Mux(cachedTrans, Cat(0.U(3.W), dCache.io.bus_wAddr(28,0)), virToPhy(addr))
  io.axi.aw.len := Mux(cachedTrans, "b0111".U(4.W), 0.U(4.W))
  io.axi.aw.size := "b010".U(3.W)
  io.axi.aw.burst := "b01".U(2.W)
  io.axi.aw.lock := 0.U
  io.axi.aw.cache := 0.U
  io.axi.aw.prot := 0.U
  io.axi.aw.valid := Mux(cachedTrans, dCache.io.bus_writeBack, io.wData.enable)

  io.axi.w.id := DATA_ID
  io.axi.w.data := Mux(cachedTrans, dCache.io.bus_wData, io.wData.data)
  io.axi.w.strb := Mux(cachedTrans, "b1111".U(4.W), io.wData.sel)
  io.axi.w.last := Mux(cachedTrans, dCache.io.bus_wLast, true.B)
  io.axi.w.valid := Mux(cachedTrans, dCache.io.bus_wValid, io.wData.enable)

  io.wData.valid :=
    Mux(cachedTrans, dCache.io.wResp,
      Mux(io.axi.b.id === DATA_ID, io.axi.b.valid,
        false.B))

  dCache.io.bus_rData := io.axi.r.data
  dCache.io.bus_rValid := io.axi.r.id === DATA_ID && io.axi.r.valid
  dCache.io.bus_wReady := io.axi.w.ready
  dCache.io.bus_bValid := io.axi.b.valid

  dCache.io.flush := DontCare // TODO remove
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
