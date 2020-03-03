// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles._
import cpu.core.bundles.stage5.{ExeMemBundle, IdExeBundle}
import cpu.core.components.{Div, Mult}
import cpu.core.execute._
import cpu.core.execute.components._

class ExecuteTop extends Module {
  val io = IO(new Bundle {
    val in = Input(new IdExeBundle)

    /** For multi-cycle multiplication and division. */
    val flush = Input(Bool())

    val rawHILO = Input(new HILOBundle)
    val memHILO = Input(new HILOValidBundle)
    val wbHILO = Input(new HILOValidBundle)

    val cp0Data = Input(UInt(dataLen.W)) // from cp0
    val memCP0 = Input(new CPBundle)
    val wbCP0 = Input(new CPBundle)

    val out = Output(new ExeMemBundle)
    val stallReq = Output(Bool())
  })

  val alu = Module(new ALU)
  val move = Module(new Move)
  val writeOther = Module(new WriteOther)
  val memory = Module(new Memory)

  val mult = Module(new Mult)
  val div = Module(new Div)

  /** Only used in `move` module */
  val forward = Module(new Forward)
  val control = Module(new Control)

  forward.io.rawHILO <> io.rawHILO
  forward.io.memHILO <> io.memHILO
  forward.io.wbHILO <> io.wbHILO

  /** cp0 come from decode */
  forward.io.rawCP0 <> io.in.cp0
  /** cp0 data come from CP0 regfile */
  forward.io.rawCP0.data := io.cp0Data
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
  writeOther.io.inCP0 <> io.in.cp0
  writeOther.io.mult <> mult.io
  writeOther.io.div <> div.io

  memory.io.op1 := io.in.op1
  memory.io.op2 := io.in.op2
  memory.io.imm16 := io.in.imm26(15,0)
  memory.io.operation := io.in.operation

  control.io.instType := io.in.instType
  control.io.inWrite <> io.in.write
  control.io.pc := io.in.pc
  control.io.inExcept := io.in.except

  control.io.aluResult := alu.io.result
  control.io.aluOverflow := alu.io.overflow
  control.io.exceptLoad := memory.io.exceptLoad
  control.io.exceptSave := memory.io.exceptSave
  control.io.moveResult := move.io.result

  io.out.write <> control.io.outWrite
  io.out.operation := io.in.operation
  io.out.cp0 <> writeOther.io.outCP0
  io.out.hilo <> writeOther.io.outHILO
  io.out.inDelaySlot := io.in.inDelaySlot
  io.out.except <> control.io.outExcept
  io.out.pc := io.in.pc
  io.out.memAddr := memory.io.memAddr
  io.out.memData := io.in.op2
  io.stallReq := writeOther.io.stallReq
}
