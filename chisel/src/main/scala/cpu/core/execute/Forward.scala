// See README.md for license details.

package cpu.core.execute

import chisel3._
import chisel3.util.MuxCase
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles._

/**
  * For CP0 and HILO.
  */
class Forward(nHILO: Int, nCP0: Int)(implicit c: CPUConfig) extends Module {
  val io = IO(new Bundle {

    /** from HILO regfile */
    val rawHILO = Input(new HILOBundle)
    val fwHILO  = Input(Vec(nHILO, new HILOValidBundle))

    /** control from decode, data from CP0 */
    val rawCP0 = Input(Vec(c.decodeWidth, new CPBundle))
    val fwCP0  = Input(Vec(nCP0, new CPBundle))

    val outHILO = Output(new HILOBundle)
    val outCP0  = Output(Vec(c.decodeWidth, UInt(dataLen.W)))
  })

  io.outHILO.hi := MuxCase(io.rawHILO.hi, io.fwHILO.map(e => e.hi.valid -> e.hi.bits))
  io.outHILO.lo := MuxCase(io.rawHILO.lo, io.fwHILO.map(e => e.lo.valid -> e.lo.bits))

  private def cp0Forward(cp0: CPBundle, order: Int): Bool = {
    require(order == 0 || order == 1)
    val raw = io.rawCP0(order)
    cp0.enable && cp0.addr === raw.addr && cp0.sel === raw.sel
  }
  io.outCP0(0) := MuxCase(io.rawCP0(0).data, io.fwCP0.map(e => cp0Forward(e, 0) -> e.data))
  io.outCP0(1) := MuxCase(io.rawCP0(1).data, io.fwCP0.map(e => cp0Forward(e, 1) -> e.data))

}
