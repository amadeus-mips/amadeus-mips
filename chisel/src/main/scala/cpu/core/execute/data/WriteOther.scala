// See README.md for license details.

package cpu.core.execute.data

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, CPControlBundle, HILOValidBundle}
import cpu.core.components.MultDivIO

/**
 * write to other register file, such as CP0, HILO,
 * include multiply and divide.
 *
 * Will stall pipeline
 */
class WriteOther extends Module {
  val io = IO(new Bundle {
    val op1 = Input(UInt(dataLen.W))
    val op2 = Input(UInt(dataLen.W))
    val operation = Input(UInt(opLen.W))

    val inCP0Control = Input(new CPControlBundle)

    val outCP0 = Output(new CPBundle)
    val hiloOut = Output(new HILOValidBundle)
    val stallReq = Output(Bool())

    val div = Flipped(new MultDivIO)
    val mult = Flipped(new MultDivIO)
  })

  io.outCP0.control <> io.inCP0Control
  io.outCP0.data := io.op2

  io.hiloOut.hi.valid := io.operation === WO_MTHI
  io.hiloOut.hi.bits := io.op1
  io.hiloOut.lo.valid := io.operation === WO_MTLO
  io.hiloOut.lo.bits := io.op1

  io.mult.op1 := io.op1
  io.mult.op2 := io.op2
  io.div.op1 := io.op1
  io.div.op2 := io.op2
  io.mult.signed := io.operation === WO_MULT || io.operation === WO_DIV
  when(io.operation === WO_MULT || io.operation === WO_MULTU){
    io.mult.enable := !io.mult.result.hi.valid
    io.stallReq := !io.mult.result.hi.valid
    io.hiloOut <> io.mult
  }.elsewhen(io.operation === WO_DIV || io.operation === DIVU){
    io.div.enable := !io.div.result.hi.valid
    io.stallReq := !io.div.result.hi.valid
    io.hiloOut <> io.div
  }
}
