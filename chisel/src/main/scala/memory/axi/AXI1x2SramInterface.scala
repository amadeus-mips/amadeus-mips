// See README.md for license details.
package memory.axi

import chisel3._
import shared.{AXIIO, Constants}

class AXI1x2SramInterface extends Module {
  val io = IO(new Bundle {
    val bus = AXIIO.slave()
    val iram = new SimpleSramIO
    val dram = new SimpleSramIO
  })

  val iCon = Module(new AXIToSram(Constants.INST_ID, 10))
  val dCon = Module(new AXIToSram(Constants.DATA_ID, 10))

  // ar
  iCon.io.bus.ar <> io.bus.ar
  dCon.io.bus.ar <> io.bus.ar
  val arIid = io.bus.ar.bits.id === Constants.INST_ID
  io.bus.ar.ready := Mux(arIid, iCon.io.bus.ar.ready, dCon.io.bus.ar.ready)
  iCon.io.bus.ar.valid := arIid && io.bus.ar.valid
  dCon.io.bus.ar.valid := !arIid && io.bus.ar.valid

  // r
  val dIid = dCon.io.bus.r.valid
  when(!dIid){
    io.bus.r <> iCon.io.bus.r
  }.otherwise {
    io.bus.r <> dCon.io.bus.r
  }
  iCon.io.bus.r.ready := !dIid && io.bus.r.ready
  dCon.io.bus.r.ready := dIid && io.bus.r.ready

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
