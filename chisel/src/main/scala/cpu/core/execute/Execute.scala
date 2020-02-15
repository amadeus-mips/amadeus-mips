// See README.md for license details.

package cpu.core.execute

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles._
import cpu.core.components.{Div, Mult}
import cpu.core.execute.control.{Control, Forward}
import cpu.core.execute.data.{ALU, Move, WriteOther}

class Execute extends Module {
  val io = IO(new Bundle {
    val in = Input(new IDEXBundle)

    val rawHILO = Input(new HILOBundle)
    val memHILO = Input(new HILOValidBundle)
    val wbHILO = Input(new HILOValidBundle)

    val cp0Data = Input(UInt(dataLen.W))   // from cp0
    val memCP0 = Input(new CPBundle)
    val wbCP0 = Input(new CPBundle)

    val out = Output(new EXMEMBundle)
  })

  val alu = Module(new ALU)
  val move = Module(new Move)
  val writeOther = Module(new WriteOther)

  val mult = Module(new Mult)
  val div = Module(new Div)

  /** Only used in `move` module */
  val forward = Module(new Forward)
  val control = Module(new Control)

  forward.io.rawHILO <> io.rawHILO
  forward.io.memHILO <> io.memHILO
  forward.io.wbHILO <> io.wbHILO

  // cp0 control come from decode
  forward.io.rawCP0.control <> io.in.cp0Control
  // cp0 data come from CP0 regfile
  forward.io.rawCP0.data <> io.cp0Data
  forward.io.memCP0 <> io.memCP0
  forward.io.wbCP0 <> io.wbCP0

  alu.io.op1 := io.in.op1
  alu.io.op2 := io.in.op2
  alu.io.operation := io.in.operation

  move.io.operation := io.in.operation
  move.io.hilo <> forward.io.outHILO
  move.io.cp0Data <> forward.io.outCP0

  writeOther.io.op1 := io.in.op1
  writeOther.io.op2 := io.in.op2
  writeOther.io.operation := io.in.operation
  writeOther.io.inCP0Control <> io.in.cp0Control
  writeOther.io.mult <> mult.io
  writeOther.io.div <> div.io

  control.io.in <> io.in
  control.io.aluResult := alu.io.result
  control.io.aluOverflow := alu.io.overflow
  control.io.moveResult := move.io.result

}
