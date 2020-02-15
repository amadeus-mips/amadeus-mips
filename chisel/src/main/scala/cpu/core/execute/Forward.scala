// See README.md for license details.

package cpu.core.execute

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._
import cpu.core.bundles._

/**
 * For CP0 and HILO.
 */
class Forward extends Module {
  val io = IO(new Bundle {
    val rawHILO = Input(new HILOBundle)     // from HILO regfile
    val memHILO = Input(new HILOValidBundle)
    val wbHILO = Input(new HILOValidBundle)

    val rawCP0 = Input(new CPBundle)  // control from decode, data from CP0
    val memCP0 = Input(new CPBundle)
    val wbCP0 = Input(new CPBundle)

    val outHILO = Output(new HILOBundle)
    val outCP0 = Output(UInt(dataLen.W))
  })

  io.outHILO.hi := MuxCase(io.rawHILO.hi,
    Array(
      io.memHILO.hi.valid -> io.memHILO.hi.bits,
      io.wbHILO.hi.valid -> io.wbHILO.hi.bits
    )
  )
  io.outHILO.lo := MuxCase(io.rawHILO.lo,
    Array(
      io.memHILO.lo.valid -> io.memHILO.lo.bits,
      io.wbHILO.lo.valid -> io.wbHILO.lo.bits
    )
  )

  def cp0Forward(cp0: CPControlBundle): Bool = {
    val raw = io.rawCP0.control
    cp0.enable && cp0.address === raw.address && cp0.sel === raw.sel
  }
  io.outCP0 := MuxCase(io.rawCP0.data,
    Array(
      cp0Forward(io.memCP0.control) -> io.memCP0.data,
      cp0Forward(io.wbCP0.control) -> io.wbCP0.data
    )
  )

}
