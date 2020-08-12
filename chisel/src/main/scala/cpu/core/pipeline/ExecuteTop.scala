// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.Valid
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles._
import cpu.core.bundles.stages.{ExeMemBundle, IdExeBundle}
import cpu.core.components.{BrPrUpdateBundle, Div, Mult}
import cpu.core.execute.components._
import shared.ValidBundle

class ExecuteTop(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val ins = Input(Vec(conf.decodeWidth, new IdExeBundle))

    /** For multi-cycle multiplication and division. */
    val flush = Input(Bool())

    val stalled = Input(Bool())

    // hilo forward
    val rawHILO = Input(new HILOBundle)
    val hiloForwards = Input(
      Vec(
        conf.decodeWidth,
        new Bundle {
          val mem0HILO = new HILOValidBundle
          val mem1HILO = new HILOValidBundle
        }
      )
    )

    // cp0 forward
    val cp0Data = Input(Vec(conf.decodeWidth, UInt(dataLen.W))) // from cp0
    val mem0CP0 = Input(new CPBundle)
    val mem1CP0 = Input(new CPBundle)

    // op forward
    val mem0Op = Input(UInt(opLen.W))
    val mem1Op = Input(UInt(opLen.W))

    val out = Output(Vec(conf.decodeWidth, new ExeMemBundle))

    val branch     = Output(new ValidBundle) // back to `Fetch`
    val predUpdate = Valid(new BrPrUpdateBundle)
    val stallReq   = Output(Bool())
    val isBranch   = Output(Bool())
  })

  val alu  = Seq.fill(2)(Module(new ALU))
  val move = Seq.fill(2)(Module(new Move))
  // only one cp0 or mult/div issue
  val writeOther = Module(new MultDiv)
  // now only one memory in an issue packet, but it might be in the first slot or the second slot
  val memory = Seq.fill(2)(Module(new Memory))

  val mult = Module(new Mult)
  val div  = Module(new Div)

  // only first slot will be the branch instruction
  val branch = Module(new Branch)

  /** Only used in `move` module */
  val forward = Module(new cpu.core.execute.Forward(2 * conf.decodeWidth, 2))

  val control = Seq.fill(2)(Module(new cpu.core.execute.Control))

  val hiloSlot = Mux(opIsHILOWrite(io.ins(0).operation), 0.U, 1.U)

  for (i <- 0 until conf.decodeWidth) {
    val op1       = io.ins(i).op1
    val op2       = io.ins(i).op2
    val operation = io.ins(i).operation

    alu(i).io.op1       := op1
    alu(i).io.op2       := op2
    alu(i).io.operation := operation
    alu(i).io.lo        := writeOther.io.outHILO.lo.bits

    move(i).io.op1         := op1
    move(i).io.op2         := op2
    move(i).io.operation   := operation
    move(i).io.hilo        := forward.io.outHILO
    move(i).io.cp0Data     := forward.io.outCP0(i)
    memory(i).io.op1       := op1
    memory(i).io.op2       := op2
    memory(i).io.imm16     := io.ins(0).imm26(15, 0)
    memory(i).io.operation := operation
    memory(i).io.rt        := io.ins(0).imm26(20, 16)

    control(i).io.instType := io.ins(i).instType
    control(i).io.inWrite  := io.ins(i).write
    control(i).io.pc       := io.ins(i).pc
    control(i).io.inExcept := io.ins(i).except

    control(i).io.aluResult    := alu(i).io.result
    control(i).io.aluOverflow  := alu(i).io.overflow
    control(i).io.trap         := alu(i).io.trap
    control(i).io.exceptLoad   := memory(i).io.exceptLoad
    control(i).io.exceptSave   := memory(i).io.exceptSave
    control(i).io.moveResult   := move(i).io.result
    control(i).io.moveWe       := move(i).io.we
    control(i).io.memWriteData := io.ins(i).op2

    io.out(i).write       := control(i).io.outWrite
    io.out(i).instType    := io.ins(i).instType
    io.out(i).operation   := io.ins(i).operation
    io.out(i).inDelaySlot := io.ins(i).inDelaySlot
    io.out(i).except      := control(i).io.outExcept
    io.out(i).pc          := io.ins(i).pc
    io.out(i).memAddr     := memory(i).io.memAddr
    io.out(i).memData     := op2
    io.out(i).cacheOp     := memory(i).io.cacheOp
    io.out(i).instValid   := io.ins(i).instValid
  }
  io.out(0).cp0       := io.ins(0).cp0
  io.out(0).cp0.data  := io.ins(0).op2
  io.out(0).cp0.valid := true.B
  io.out(1).cp0       := 0.U.asTypeOf(io.out(1).cp0)

  io.out(0).hilo        := 0.U.asTypeOf(io.out(0).hilo)
  io.out(1).hilo        := 0.U.asTypeOf(io.out(1).hilo)
  io.out(hiloSlot).hilo := writeOther.io.outHILO

  forward.io.rawHILO   := io.rawHILO
  forward.io.fwHILO(0) := io.hiloForwards(1).mem0HILO
  forward.io.fwHILO(1) := io.hiloForwards(0).mem0HILO
  forward.io.fwHILO(2) := io.hiloForwards(1).mem1HILO
  forward.io.fwHILO(3) := io.hiloForwards(0).mem1HILO

  // cp0 come from decode, data come from CP0 regfile
  forward.io.rawCP0(0)      := io.ins(0).cp0
  forward.io.rawCP0(0).data := io.cp0Data(0)
  forward.io.rawCP0(1)      := io.ins(1).cp0
  forward.io.rawCP0(1).data := io.cp0Data(1)
  forward.io.fwCP0(0)       := io.mem0CP0
  forward.io.fwCP0(1)       := io.mem1CP0

  writeOther.io.op1       := io.ins(hiloSlot).op1
  writeOther.io.op2       := io.ins(hiloSlot).op2
  writeOther.io.operation := io.ins(hiloSlot).operation
  writeOther.io.flush     := io.flush
  writeOther.io.mult      <> mult.io
  writeOther.io.div       <> div.io

  branch.io.op1       := io.ins(0).op1
  branch.io.op2       := io.ins(0).op2
  branch.io.operation := io.ins(0).operation
  branch.io.imm26     := io.ins(0).imm26
  branch.io.pc        := io.ins(0).pc

  val brPrFail =
    (branch.io.branch.valid ^ io.ins(0).brPredict.valid) ||
      (branch.io.branch.valid && branch.io.branch.bits =/= io.ins(0).brPredict.bits)

  io.branch.bits  := Mux(branch.io.branch.valid, branch.io.branch.bits, io.ins(0).pc + 8.U)
  io.branch.valid := brPrFail && !io.stalled

  io.predUpdate.bits.pc      := io.ins(0).pc
  io.predUpdate.bits.jump    := branch.io.branch.valid
  io.predUpdate.bits.history := io.ins(0).brPrHistory
  io.predUpdate.valid        := io.ins(0).instType === INST_BR && !io.stalled

  io.isBranch := io.ins(0).instType === INST_BR

  io.stallReq := writeOther.io.stallReq ||
    (io.ins(0).operation === MV_MFC0 || io.ins(1).operation === MV_MFC0) && VecInit(io.mem0Op, io.mem1Op)
      .contains(TLB_WI) ||
    VecInit(TLB_R, TLB_P, WO_MTC0).contains(io.mem0Op) || VecInit(TLB_R, TLB_P, WO_MTC0).contains(io.mem1Op)

  //===-----------------------------------------------------------===
  // performance monitor, only used in simulation
  //===-----------------------------------------------------------===
  val brPrTotal = RegInit(0.U.asTypeOf(new BrPrPerfBundle))
  val brPrJ     = RegInit(0.U.asTypeOf(new BrPrPerfBundle))
  val brPrB     = RegInit(0.U.asTypeOf(new BrPrPerfBundle))

  val pcReg = RegInit(0.U(addrLen.W))
  when(io.ins(0).instType === INST_BR && pcReg =/= io.ins(0).pc) {
    val isJ = VecInit(Seq(BR_JR, BR_JALR, BR_J, BR_JAL)).contains(io.ins(0).operation)
    pcReg := io.ins(0).pc
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
