// See README.md for license details.

package cpu.core.execute.components

import chisel3._
import chisel3.util._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, HILOBundle, HILOValidBundle}
import cpu.core.components.MultDivIO

/**
  * write to other register file, such as CP0, HILO,
  * include multiply and divide.
  *
  * Will stall pipeline
  */
class WriteOther extends Module {
  val io = IO(new Bundle {
    val op1       = Input(UInt(dataLen.W))
    val op2       = Input(UInt(dataLen.W))
    val operation = Input(UInt(opLen.W))

    val inHILO = Input(new HILOBundle)

    val flush = Input(Bool())

    val inCP0 = Input(new CPBundle)

    val outCP0   = Output(new CPBundle)
    val outHILO  = Output(new HILOValidBundle)
    val stallReq = Output(Bool())

    val mult = Flipped(new MultDivIO)
    val div  = Flipped(new MultDivIO)
  })

  io.outCP0       := io.inCP0
  io.outCP0.data  := io.op2
  io.outCP0.valid := true.B

  io.outHILO.hi.valid := io.operation === WO_MTHI
  io.outHILO.hi.bits  := io.op1
  io.outHILO.lo.valid := io.operation === WO_MTLO
  io.outHILO.lo.bits  := io.op1

  io.mult.op1    := io.op1
  io.mult.op2    := io.op2
  io.mult.enable := false.B
  io.mult.flush  := io.flush
  io.mult.signed := io.operation === WO_MULT || io.operation === WO_MADD || io.operation === WO_MSUB
  io.div.op1     := io.op1
  io.div.op2     := io.op2
  io.div.enable  := false.B
  io.div.flush   := io.flush
  io.div.signed  := io.operation === WO_DIV
  io.stallReq    := false.B

  val hiReg = RegNext(io.mult.result.hi.bits, 0.U(32.W))
  val loReg = RegNext(io.mult.result.lo.bits, 0.U(32.W))
  val res = Mux(
    io.operation === WO_MADD || io.operation === WO_MADDU,
    Cat(io.inHILO.hi, io.inHILO.lo) + Cat(hiReg, loReg),
    Cat(io.inHILO.hi, io.inHILO.lo) - Cat(hiReg, loReg)
  )
  when(io.operation === WO_MULT || io.operation === WO_MULTU || io.operation === ALU_MUL) {
    io.mult.enable := !io.mult.result.hi.valid
    io.stallReq    := !io.mult.result.hi.valid && !io.flush
    io.outHILO     := io.mult.result
  }.elsewhen(VecInit(WO_MADD, WO_MADDU, WO_MSUB, WO_MSUBU).contains(io.operation)) {
      io.mult.enable := !io.mult.result.hi.valid
      io.stallReq    := !RegNext(io.mult.result.hi.valid) && !io.flush
      io.outHILO     := RegNext(io.mult.result)
      io.outHILO.hi.bits := res(63, 32)
      io.outHILO.lo.bits := res(31, 0)
    }
    .elsewhen(io.operation === WO_DIV || io.operation === WO_DIVU) {
      io.div.enable := !io.div.result.hi.valid
      io.stallReq   := !io.div.result.hi.valid && !io.flush
      io.outHILO    := io.div.result
    }
}
