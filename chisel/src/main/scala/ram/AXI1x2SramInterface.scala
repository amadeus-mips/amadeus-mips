// See README.md for license details.
package ram

import chisel3._
import common.{AXIMasterIO, Constants}

class AXI1x2SramInterface extends Module {
  val io = IO(new Bundle {
    val bus = Flipped(new AXIMasterIO)
    val iram = new SimpleSramIO
    val dram = new SimpleSramIO
  })

  val iCon = Module(new AXIToSram(Constants.INST_ID, 10))
  val dCon = Module(new AXIToSram(Constants.DATA_ID, 10))

  // ar
  iCon.io.bus.ar <> io.bus.ar
  dCon.io.bus.ar <> io.bus.ar
  val arIid = io.bus.ar.id === Constants.INST_ID
  io.bus.ar.ready := Mux(arIid, iCon.io.bus.ar.ready, dCon.io.bus.ar.ready)
  iCon.io.bus.ar.valid := arIid
  dCon.io.bus.ar.valid := !arIid

  // r
  val rIid = iCon.io.bus.r.valid
  when(rIid){
    io.bus.r <> iCon.io.bus.r
  }.otherwise {
    io.bus.r <> dCon.io.bus.r
  }
  iCon.io.bus.r.ready := rIid && io.bus.r.ready
  dCon.io.bus.r.ready := !rIid && io.bus.r.ready

  // aw
  dCon.io.bus.aw <> io.bus.aw
  iCon.io.bus.aw <> DontCare

  // w
  dCon.io.bus.w <> io.bus.w
  iCon.io.bus.w <> DontCare

  // b
  dCon.io.bus.b <> io.bus.b
  iCon.io.bus.b <> DontCare

  // ram
  io.iram <> iCon.io.ram
  io.dram <> dCon.io.ram
}
