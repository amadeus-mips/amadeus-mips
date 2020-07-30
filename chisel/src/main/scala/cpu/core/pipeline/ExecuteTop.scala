// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles._
import cpu.core.bundles.stages.{ExeMemBundle, IdExeBundle}
import cpu.core.components.{BrPrUpdateBundle, Div, Mult}
import cpu.core.execute.components._
import shared.ValidBundle

class ExecuteTop(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val in = Input(new IdExeBundle)

    /** For multi-cycle multiplication and division. */
    val flush = Input(Bool())

    val decodeValid = Input(Bool())

    val rawHILO  = Input(new HILOBundle)
    val mem0HILO = Input(new HILOValidBundle)
    val mem1HILO = Input(new HILOValidBundle)

    val cp0Data = Input(UInt(dataLen.W)) // from cp0
    val mem0CP0 = Input(new CPBundle)
    val mem1CP0 = Input(new CPBundle)

    val mem0Op = Input(UInt(opLen.W))
    val mem1Op = Input(UInt(opLen.W))

    val out        = Output(new ExeMemBundle)
    val branch     = Output(new ValidBundle) // back to `Fetch`
    val predUpdate = Output(new BrPrUpdateBundle)
    val stallReq   = Output(Bool())
    val waitingDS  = Output(Bool())
    val isBranch   = Output(Bool())
  })

  val alu        = Module(new ALU)
  val move       = Module(new Move)
  val writeOther = Module(new WriteOther)
  val memory     = Module(new Memory)

  val mult = Module(new Mult)
  val div  = Module(new Div)

  val branch = Module(new Branch)

  /** Only used in `move` module */
  val forward = Module(new cpu.core.execute.Forward(2, 2))
  val control = Module(new cpu.core.execute.Control)

  forward.io.rawHILO   := io.rawHILO
  forward.io.fwHILO(0) := io.mem0HILO
  forward.io.fwHILO(1) := io.mem1HILO

  // cp0 come from decode, data come from CP0 regfile
  forward.io.rawCP0      := io.in.cp0
  forward.io.rawCP0.data := io.cp0Data
  forward.io.fwCP0(0)    := io.mem0CP0
  forward.io.fwCP0(1)    := io.mem1CP0

  alu.io.op1       := io.in.op1
  alu.io.op2       := io.in.op2
  alu.io.operation := io.in.operation
  alu.io.lo        := writeOther.io.outHILO.lo.bits

  move.io.op1       := io.in.op1
  move.io.op2       := io.in.op2
  move.io.operation := io.in.operation
  move.io.hilo      := forward.io.outHILO
  move.io.cp0Data   := forward.io.outCP0

  writeOther.io.op1       := io.in.op1
  writeOther.io.op2       := io.in.op2
  writeOther.io.operation := io.in.operation
  writeOther.io.flush     := io.flush
  writeOther.io.inCP0     := io.in.cp0
  writeOther.io.mult      <> mult.io
  writeOther.io.div       <> div.io

  memory.io.op1       := io.in.op1
  memory.io.op2       := io.in.op2
  memory.io.imm16     := io.in.imm26(15, 0)
  memory.io.operation := io.in.operation
  memory.io.rt        := io.in.imm26(20, 16)

  branch.io.op1       := io.in.op1
  branch.io.op2       := io.in.op2
  branch.io.operation := io.in.operation
  branch.io.imm26     := io.in.imm26
  branch.io.pc        := io.in.pc

  control.io.instType := io.in.instType
  control.io.inWrite  := io.in.write
  control.io.pc       := io.in.pc
  control.io.inExcept := io.in.except

  control.io.aluResult   := alu.io.result
  control.io.aluOverflow := alu.io.overflow
  control.io.exceptLoad  := memory.io.exceptLoad
  control.io.exceptSave  := memory.io.exceptSave
  control.io.moveResult  := move.io.result
  control.io.moveWe      := move.io.we

  io.out.write       := control.io.outWrite
  io.out.operation   := io.in.operation
  io.out.cp0         := writeOther.io.outCP0
  io.out.hilo        := writeOther.io.outHILO
  io.out.inDelaySlot := io.in.inDelaySlot
  io.out.except      := control.io.outExcept
  io.out.pc          := io.in.pc
  io.out.memAddr     := memory.io.memAddr
  io.out.memData     := io.in.op2
  io.out.cacheOp     := memory.io.cacheOp
  io.out.instValid   := io.in.instValid

  val brPrFail =
    (branch.io.branch.valid ^ io.in.brPredict.valid) || (branch.io.branch.valid && branch.io.branch.bits =/= io.in.brPredict.bits)

  io.branch.bits  := Mux(branch.io.branch.valid, branch.io.branch.bits, io.in.pc + 8.U)
  io.branch.valid := brPrFail

  io.predUpdate.pc     := io.in.pc
  io.predUpdate.target := branch.io.branch.bits
  io.predUpdate.taken  := branch.io.branch.valid

  io.waitingDS := io.in.instType === INST_BR && !io.decodeValid
  io.isBranch  := io.in.instType === INST_BR

  io.stallReq := writeOther.io.stallReq ||
    io.in.operation === MV_MFC0 && VecInit(io.mem0Op, io.mem1Op).contains(TLB_WI) ||
    VecInit(TLB_R, TLB_P, WO_MTC0).contains(io.mem0Op) || VecInit(TLB_R, TLB_P, WO_MTC0).contains(io.mem1Op)

  // performance
  val brPrTotal = RegInit(0.U.asTypeOf(new BrPrPerfBundle))
  val brPrJ     = RegInit(0.U.asTypeOf(new BrPrPerfBundle))
  val brPrB     = RegInit(0.U.asTypeOf(new BrPrPerfBundle))

  val pcReg = RegInit(0.U(addrLen.W))
  when(io.in.instType === INST_BR && pcReg =/= io.in.pc) {
    val isJ = VecInit(Seq(BR_JR, BR_JALR, BR_J, BR_JAL)).contains(io.in.operation)
    pcReg := io.in.pc
    when(io.branch.valid) {
      brPrTotal.fail := brPrTotal.fail + 1.U
      when(isJ) {
        brPrJ.fail := brPrJ.fail + 1.U
      }.otherwise {
        brPrB.fail := brPrB.fail + 1.U
      }
    }.otherwise {
      brPrTotal.success := brPrTotal.success + 1.U
      when(isJ) {
        brPrJ.success := brPrJ.success + 1.U
      }.otherwise {
        brPrB.success := brPrB.success + 1.U
      }
    }
  }
}

class BrPrPerfBundle extends Bundle {
  val success = UInt(64.W)
  val fail    = UInt(64.W)
}
