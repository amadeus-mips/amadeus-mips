// See README.md for license details.

package cpu.core.execute.components

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, HILOValidBundle}
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

    val inCP0 = Input(new CPBundle)

    val outCP0 = Output(new CPBundle)
    val outHILO = Output(new HILOValidBundle)
    val stallReq = Output(Bool())

    val mult = Flipped(new MultDivIO)
    val div = Flipped(new MultDivIO)
  })

  io.outCP0 <> io.inCP0
  io.outCP0.data := io.op2
  io.outCP0.valid := true.B

  io.outHILO.hi.valid := io.operation === WO_MTHI
  io.outHILO.hi.bits := io.op1
  io.outHILO.lo.valid := io.operation === WO_MTLO
  io.outHILO.lo.bits := io.op1

  io.mult.op1 := io.op1
  io.mult.op2 := io.op2
  io.mult.enable := false.B
  io.mult.signed := io.operation === WO_MULT
  io.div.op1 := io.op1
  io.div.op2 := io.op2
  io.div.enable := false.B
  io.div.signed := io.operation === WO_DIV
  io.stallReq := false.B
  when(io.operation === WO_MULT || io.operation === WO_MULTU){
    io.mult.enable := !io.mult.result.hi.valid
    io.stallReq := !io.mult.result.hi.valid
    io.outHILO <> io.mult.result
  }.elsewhen(io.operation === WO_DIV || io.operation === WO_DIVU){
    io.div.enable := !io.div.result.hi.valid
    io.stallReq := !io.div.result.hi.valid
    io.outHILO <> io.div.result
  }
}
