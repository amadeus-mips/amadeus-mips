// See README.md for license details.

package cpu.core.execute.components

import chisel3._
import chisel3.util._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, HILOBundle, HILOValidBundle}

/**
 * some Move instruction, include MFC0, MFHI, MFLO
 */
class MoveIO extends Bundle {
  val operation = Input(UInt(opLen.W))
  val hilo = Input(new HILOBundle)
  val cp0Data = Input(UInt(dataLen.W))

  val result = Output(UInt(dataLen.W))
}

class Move extends Module {
  val io = IO(new MoveIO)

  io.result := MuxLookup(io.operation, 0.U,
    Array(
      MV_MFHI -> io.hilo.hi,
      MV_MFLO -> io.hilo.lo,
      MV_MFC0 -> io.cp0Data
    )
  )
//  io.result.valid := io.operation =/= MV_N
}
