// See README.md for license details.

package cpu.core.execute

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._
import cpu.core.bundles._

/**
  * For CP0 and HILO.
  */
class Forward(nHILO: Int, nCP0: Int) extends Module {
  val io = IO(new Bundle {
    /** from HILO regfile */
    val rawHILO = Input(new HILOBundle)
    val fwHILO  = Input(Vec(nHILO, new HILOValidBundle))

    /** control from decode, data from CP0 */
    val rawCP0 = Input(new CPBundle)
    val fwCP0  = Input(Vec(nCP0, new CPBundle))

    val outHILO = Output(new HILOBundle)
    val outCP0  = Output(UInt(dataLen.W))
  })

  io.outHILO.hi := MuxCase(io.rawHILO.hi, io.fwHILO.map(e => e.hi.valid -> e.hi.bits))
  io.outHILO.lo := MuxCase(io.rawHILO.lo, io.fwHILO.map(e => e.lo.valid -> e.lo.bits))

  private def cp0Forward(cp0: CPBundle): Bool = {
    val raw = io.rawCP0
    cp0.enable && cp0.addr === raw.addr && cp0.sel === raw.sel
  }
  io.outCP0 := MuxCase(io.rawCP0.data, io.fwCP0.map(e => cp0Forward(e) -> e.data))

}
