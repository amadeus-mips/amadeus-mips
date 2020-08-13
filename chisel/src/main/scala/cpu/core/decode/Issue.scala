package cpu.core.decode

import Chisel.Decoupled
import chisel3._
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.stages.IdExeBundle
import cpu.core.bundles.{CPBundle, InstructionFIFOEntry, WriteBundle}
import shared.ValidBundle

class DecodeResult extends Bundle {
  val operation = UInt(opLen.W)
  val op1Type   = UInt(1.W)
  val op2Type   = UInt(1.W)
  val instType  = UInt(instTypeLen.W)
  val write     = new WriteBundle
  val cp0       = new CPBundle
  val imm32     = UInt(dataLen.W)
  val except    = Vec(exceptAmount, Bool())
}

class Issue(implicit c: CPUConfig) extends Module {
  val n = c.decodeWidth
  val io = IO(new Bundle() {
    val ins           = Vec(n, Flipped(Decoupled(new InstructionFIFOEntry())))
    val decodeResults = Input(Vec(n, new DecodeResult))

    val flush   = Input(Bool())
    val stalled = Input(Bool())

    /**
      * for n inputs and two buffer
      */
    val operands = Vec(
      n + c.decodeBufferNum,
      new Bundle {
        val rs  = Output(new ValidBundle(regAddrLen))
        val rt  = Output(new ValidBundle(regAddrLen))
        val op1 = Input(new ValidBundle())
        val op2 = Input(new ValidBundle())
      }
    )

    val out      = Output(Vec(n, new IdExeBundle()))
    val stallReq = Output(Bool())
  })

  val buffer      = RegInit(0.U.asTypeOf(new DecodeResult))
  val bufferIn    = RegInit(0.U.asTypeOf(new InstructionFIFOEntry()))
  val bufferValid = RegInit(false.B)

  /**
    * buffer | in(0) | in(1) <br/>
    * choose first two valid
    */
  val current = VecInit(
    Mux(bufferValid, buffer, io.decodeResults(0)),
    Mux(bufferValid, io.decodeResults(0), io.decodeResults(1))
  )
  val currentIn = VecInit(
    Mux(bufferValid, bufferIn, io.ins(0).bits),
    Mux(bufferValid, io.ins(0).bits, io.ins(1).bits)
  )
  val currentRs = VecInit(
    Mux(bufferValid, io.operands(0).rs, io.operands(1).rs),
    Mux(bufferValid, io.operands(1).rs, io.operands(2).rs)
  )
  val currentRt = VecInit(
    Mux(bufferValid, io.operands(0).rt, io.operands(1).rt),
    Mux(bufferValid, io.operands(1).rt, io.operands(2).rt)
  )
  val currentOp1 = VecInit(
    Mux(bufferValid, io.operands(0).op1, io.operands(1).op1),
    Mux(bufferValid, io.operands(1).op1, io.operands(2).op1)
  )
  val currentOp2 = VecInit(
    Mux(bufferValid, io.operands(0).op2, io.operands(1).op2),
    Mux(bufferValid, io.operands(1).op2, io.operands(2).op2)
  )
  val currentValid = VecInit(
    bufferValid || io.ins(0).valid,
    bufferValid && io.ins(0).valid || !bufferValid && io.ins(1).valid
  )

  val isMem       = current.map(in => { INST_MEM === in.instType })
  val isBranch    = current.map(in => { INST_BR === in.instType })
  val isEret      = current.map(in => { EXC_ER === in.operation })
  val isHILOWrite = current.map(in => { opIsHILOWrite(in.operation) })
  val isC0Write   = current.map(in => { opIsC0Write(in.operation) })

  val hiloHazard =
    current(0).operation === WO_MTHI && current(1).operation === MV_MFHI ||
      current(0).operation === WO_MTLO && current(1).operation === MV_MFLO ||
      VecInit(WO_DIV, WO_DIVU, WO_MULT, ALU_MUL, WO_MULTU).contains(current(0).operation) &&
        VecInit(MV_MFHI, MV_MFLO).contains(current(1).operation)

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
    isC0Write(0) || (!currentOp1(1).valid || !currentOp2(1).valid) || regFileHazard || hiloHazard

  //===---------------------------------------------------===
  // buffer controller
  //===---------------------------------------------------===
  assert(!(currentValid(1) && !currentValid(0)))
  io.ins(0).ready := !io.stalled
  io.ins(1).ready := !io.stalled && !(bufferValid && currentValid(1) && secondNotIssue)
  when(!io.stalled) {
    when(currentValid(0)) {
      when(bufferValid) {
        // issue buffer
        when(currentValid(1)) {
          when(secondNotIssue) {
            // only issue buffer
            buffer   := io.decodeResults(0)
            bufferIn := io.ins(0).bits
          }.otherwise {
            // issue two instruction
            when(io.ins(1).valid) {
              buffer   := io.decodeResults(1)
              bufferIn := io.ins(1).bits
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
            buffer      := io.decodeResults(1)
            bufferIn    := io.ins(1).bits
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
      out.imm26       := currentIn(i).inst(25, 0)
      out.pc          := currentIn(i).pc
      out.inDelaySlot := (if (i == 0) false.B else isBranch(0))
      out.brPredict   := currentIn(i).brPredict
      out.brPrHistory := currentIn(i).brPrHistory
      out.instValid   := currentIn(i).instValid
      out.pcValid     := currentValid(i)
  }
  when(secondNotIssue) {
    io.out(1) := 0.U.asTypeOf(io.out(1))
  }
  assert(io.ins(0).bits.instValid || !io.ins(0).bits.inst.orR())
  assert(io.ins(1).bits.instValid || !io.ins(1).bits.inst.orR())

  io.operands
    .zip(bufferIn +: io.ins.map(_.bits))
    .zip(buffer +: io.decodeResults)
    .zip(bufferValid +: io.ins.map(_.valid))
    .foreach {
      case (((operands, entry), decodeResult), valid) =>
        operands.rs.bits  := entry.inst(25, 21)
        operands.rt.bits  := entry.inst(20, 16)
        operands.rs.valid := decodeResult.op1Type === OPn_RF && valid
        operands.rt.valid := decodeResult.op2Type === OPn_RF && valid
    }

  io.stallReq := !currentValid(0) || (!currentOp1(0).valid || !currentOp2(0).valid) ||
    isBranch(0) && (!currentValid(1) || (!currentOp1(1).valid || !currentOp2(1).valid))
}
