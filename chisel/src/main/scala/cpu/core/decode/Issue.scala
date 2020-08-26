package cpu.core.decode

import Chisel.Decoupled
import chisel3._
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.stages.IdExeBundle
import cpu.core.bundles.{CPBundle, WriteBundle}
import shared.ValidBundle

class IssueInputBundle(implicit conf: CPUConfig) extends Bundle {
  val instType    = UInt(instTypeLen.W)
  val operation   = UInt(opLen.W)
  val op1Type     = UInt(1.W)
  val op2Type     = UInt(1.W)
  val write       = new WriteBundle
  val cp0         = new CPBundle
  val imm32       = UInt(dataLen.W)
  val except      = Vec(exceptAmount, Bool())
  val imm26       = UInt(26.W)
  val rs          = UInt(regAddrLen.W)
  val rt          = UInt(regAddrLen.W)
  val pc          = UInt(addrLen.W)
  val brPredict   = ValidBundle(addrLen)
  val brPrHistory = UInt(conf.branchPredictorHistoryLen.W)
  val instValid   = Bool()

  override def cloneType: IssueInputBundle.this.type = new IssueInputBundle().asInstanceOf[this.type]
}

class Issue(implicit c: CPUConfig) extends Module {
  val n = c.decodeWidth
  val io = IO(new Bundle() {
    val ins = Vec(n, Flipped(Decoupled(new IssueInputBundle)))

    val flush   = Input(Bool())
    val stalled = Input(Bool())

    /**
      * for n inputs and two buffer
      */
    val operands = Vec(
      n + c.decodeBufferNum,
      new Bundle {
        val rs  = Output(UInt(regAddrLen.W))
        val rt  = Output(UInt(regAddrLen.W))
        val op1 = Input(new ValidBundle())
        val op2 = Input(new ValidBundle())
      }
    )

    val out      = Output(Vec(n, new IdExeBundle()))
    val stallReq = Output(Bool())
  })
  val stalled = io.stalled || io.stallReq

  val buffer      = RegInit(0.U.asTypeOf(new IssueInputBundle))
  val bufferValid = RegInit(false.B)

  /**
    * buffer | in(0) | in(1) <br/>
    * choose first two valid
    */
  val current = VecInit(
    Mux(bufferValid, buffer, io.ins(0).bits),
    Mux(bufferValid, io.ins(0).bits, io.ins(1).bits)
  )
  val currentValid = VecInit(
    bufferValid || io.ins(0).valid,
    bufferValid && io.ins(0).valid || !bufferValid && io.ins(1).valid
  )
  val (currentRs, currentRt) =
    current
      .zip(currentValid)
      .map {
        case (cur, valid) =>
          (ValidBundle(valid && cur.op1Type === OPn_RF, cur.rs), ValidBundle(valid && cur.op2Type === OPn_RF, cur.rt))
      }
      .unzip

  val currentOp1 = VecInit(
    Mux(bufferValid, io.operands(0).op1, io.operands(1).op1),
    Mux(bufferValid, io.operands(1).op1, io.operands(2).op1)
  )
  val currentOp2 = VecInit(
    Mux(bufferValid, io.operands(0).op2, io.operands(1).op2),
    Mux(bufferValid, io.operands(1).op2, io.operands(2).op2)
  )

  //noinspection DuplicatedCode
  val currentOp1Ready = currentRs.zip(currentOp1).map { case (rs, op1) => !rs.valid || op1.valid }
  //noinspection DuplicatedCode
  val currentOp2Ready = currentRt.zip(currentOp2).map { case (rt, op2) => !rt.valid || op2.valid }

  val isWait      = current.map(_.operation === EXC_WAIT)
  val isMem       = current.map(in => { INST_MEM === in.instType })
  val isBranch    = current.map(in => { INST_BR === in.instType })
  val isEret      = current.map(in => { EXC_ER === in.operation })
  val isHILOWrite = current.map(in => { opIsHILOWrite(in.operation) })
  val isC0Write   = current.map(in => { opIsC0Write(in.operation) })

  val hiloHazard =
    isHILOWrite(0) && VecInit(MV_MFHI, MV_MFLO).contains(current(1).operation)

  val regFileHazard = current(0).write.enable &&
    (currentRs(1).valid && currentRs(1).bits === current(0).write.address ||
      currentRt(1).valid && currentRt(1).bits === current(0).write.address)

  /**
    * conditions:
    *   <ul>both are memory</ul>
    *   <ul>second is branch</ul>
    *   <ul>both are eret</ul>
    *   <ul>both will write hilo</ul>
    *   <ul>first will write cp0</ul>
    *   <ul>second not ready</ul>
    *   <ul>have hazard(regFile or hilo)</ul>
    */
  val secondNotIssue = isMem.reduce(_ && _) || isBranch(1) || isEret.reduce(_ && _) || isHILOWrite.reduce(_ && _) ||
    isC0Write(0) || isWait(0) || (!currentOp1Ready(1) || !currentOp2Ready(1)) || regFileHazard || hiloHazard

  //===---------------------------------------------------===
  // buffer controller
  //===---------------------------------------------------===
  assert(!(currentValid(1) && !currentValid(0)))
  io.ins(0).ready := !stalled
  io.ins(1).ready := !stalled && !(bufferValid && currentValid(1) && secondNotIssue)
  when(!stalled) {
    when(currentValid(0)) {
      when(bufferValid) {
        // issue buffer
        when(currentValid(1)) {
          when(secondNotIssue) {
            // only issue buffer
            buffer := io.ins(0).bits
          }.otherwise {
            // issue two instruction
            when(io.ins(1).valid) {
              buffer := io.ins(1).bits
            }.otherwise {
              bufferValid := false.B
            }
          }
        }.otherwise {
          bufferValid := false.B
        }
      }.otherwise {
        // issue ins
        when(currentValid(1)) {
          when(secondNotIssue) {
            // only issue first in
            bufferValid := true.B
            buffer      := io.ins(1).bits
          }.otherwise {
            // issue two ins, don't need to enter buffer
          }
        }.otherwise {
          // only one valid, issued, don't need to enter buffer
        }
      }
    }
  }
  when(io.flush) {
    bufferValid := false.B
  }

  io.out.zip(current).zipWithIndex.foreach {
    case ((out, current), i) =>
      out.instType    := current.instType
      out.operation   := current.operation
      out.op1         := Mux(current.op1Type === OPn_RF, currentOp1(i).bits, current.imm32)
      out.op2         := Mux(current.op2Type === OPn_RF, currentOp2(i).bits, current.imm32)
      out.write       := current.write
      out.cp0         := current.cp0
      out.except      := current.except
      out.imm26       := current.imm26
      out.pc          := current.pc
      out.inDelaySlot := (if (i == 0) false.B else isBranch(0))
      out.brPredict   := current.brPredict
      out.brPrHistory := current.brPrHistory
      out.instValid   := current.instValid
      out.pcValid     := currentValid(i)
  }
  when(!currentValid(0)) {
    io.out(0) := 0.U.asTypeOf(io.out(0))
  }
  when(secondNotIssue || !currentValid(1)) {
    io.out(1) := 0.U.asTypeOf(io.out(1))
  }

  io.operands
    .zip(buffer +: io.ins.map(_.bits))
    .foreach {
      case (operands, entry) =>
        operands.rs := entry.rs
        operands.rt := entry.rt
    }

  io.stallReq := !currentValid(0) || (!currentOp1Ready(0) || !currentOp2Ready(0)) ||
    isBranch(0) && (!currentValid(1) || (!currentOp1Ready(1) || !currentOp2Ready(1)))
}
