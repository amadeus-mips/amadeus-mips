// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.Decoupled
import chisel3.util.experimental.loadMemoryFromFile
import cpu.CPUConfig
import cpu.core.bundles.stages.IdExeBundle
import cpu.core.bundles.{InstructionFIFOEntry, WriteBundle}
import cpu.core.components.RegFileReadIO
import cpu.core.decode.Issue
import firrtl.annotations.MemoryLoadFileType

class DecodeTop(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val ins     = Vec(conf.decodeWidth, Flipped(Decoupled(new InstructionFIFOEntry())))
    val flush = Input(Bool())
    val stalled = Input(Bool())

    val forwards = Input(
      Vec(
        conf.decodeWidth,
        new Bundle {
          val exeWR  = new WriteBundle
          val mem0WR = new WriteBundle
          val mem1WR = new WriteBundle
          val mem2WR = new WriteBundle
          val wbWR   = new WriteBundle
        }
      )
    )
    // register file
    val operands = Vec(conf.decodeWidth + conf.decodeBufferNum, Flipped(new RegFileReadIO))

    val out      = Output(Vec(conf.decodeWidth, new IdExeBundle))
    val stallReq = Output(Bool()) // to pipeLine control
  })

  val ins = io.ins.map(in => Mux(in.valid, in.bits, 0.U.asTypeOf(in.bits)))

  val hazards  = Seq.fill(conf.decodeWidth + conf.decodeBufferNum)(Module(new cpu.core.decode.Hazard(2 * 5)))
  val decodes  = Seq.fill(conf.decodeWidth)(Module(new cpu.core.decode.Decode))
  val controls = Seq.fill(conf.decodeWidth)(Module(new cpu.core.decode.Control))

  val issue = Module(new Issue())

  hazards.zip(issue.io.operands).zip(io.operands).foreach {
    case ((hazard, operand), inData) =>
      hazard.io.ops(0).addr   := operand.rs.bits
      hazard.io.ops(0).valid  := operand.rs.valid
      hazard.io.ops(0).inData := inData.rsData
      hazard.io.ops(1).addr   := operand.rt.bits
      hazard.io.ops(1).valid  := operand.rt.valid
      hazard.io.ops(1).inData := inData.rtData

      hazard.io.wrs(0) := io.forwards(1).exeWR
      hazard.io.wrs(1) := io.forwards(0).exeWR
      hazard.io.wrs(2) := io.forwards(1).mem0WR
      hazard.io.wrs(3) := io.forwards(0).mem0WR
      hazard.io.wrs(4) := io.forwards(1).mem1WR
      hazard.io.wrs(5) := io.forwards(0).mem1WR
      hazard.io.wrs(6) := io.forwards(1).mem2WR
      hazard.io.wrs(7) := io.forwards(0).mem2WR
      hazard.io.wrs(8) := io.forwards(1).wbWR
      hazard.io.wrs(9) := io.forwards(0).wbWR
  }
  for (i <- 0 until conf.decodeWidth) {
    val rt    = ins(i).inst(20, 16)
    val rd    = ins(i).inst(15, 11)
    val sa    = ins(i).inst(10, 6)
    val sel   = ins(i).inst(2, 0)
    val imm16 = ins(i).inst(15, 0)

    decodes(i).io.inst := ins(i).inst

    controls(i).io.inst.rt    := rt
    controls(i).io.inst.rd    := rd
    controls(i).io.inst.sa    := sa
    controls(i).io.inst.imm16 := imm16
    controls(i).io.inst.sel   := sel
    controls(i).io.signal     := decodes(i).io.out
    controls(i).io.inExcept   := ins(i).except

    issue.io.ins(i).bits := ins(i)
    issue.io.ins(i).valid := io.ins(i).valid

    issue.io.decodeResults(i).operation := decodes(i).io.out.operation
    issue.io.decodeResults(i).op1Type   := decodes(i).io.out.op1Type
    issue.io.decodeResults(i).op2Type   := decodes(i).io.out.op2Type
    issue.io.decodeResults(i).instType  := decodes(i).io.out.instType
    issue.io.decodeResults(i).write     := controls(i).io.write
    issue.io.decodeResults(i).cp0       := controls(i).io.cp0
    issue.io.decodeResults(i).imm32     := controls(i).io.imm32
    issue.io.decodeResults(i).except    := controls(i).io.except
  }
  issue.io.flush := io.flush
  issue.io.stalled := io.stalled

  io.out := issue.io.out

  io.operands.zip(issue.io.operands).foreach {
    case (out, issue) =>
      out.rs := issue.rs.bits
      out.rt := issue.rt.bits
  }
  issue.io.operands.zip(hazards).foreach {
    case (issue, hazard) =>
      issue.op1 := hazard.io.ops(0).outData
      issue.op2 := hazard.io.ops(1).outData
  }

  io.ins.zip(issue.io.ins).map {
    case (in, issue) => in.ready := issue.ready
  }

  io.stallReq := issue.io.stallReq

  if (conf.compareRamDirectly) {
    val veriMem = Mem(BigInt("4FFFF", 16), UInt(32.W))
    loadMemoryFromFile(veriMem, conf.memoryFile, MemoryLoadFileType.Hex)
    ins.foreach(in => {
      val error =
        !(!in.instValid || in.instValid && in.inst === veriMem.read(in.pc(19, 2))) && in.pc >= "h9fc00000".U
      when(error) {
        printf(
          p"the request is 0x${Hexadecimal(in.pc)}, " +
            p"the wrong instruction is 0x${Hexadecimal(in.inst)}, " +
            p"the correct instruction should be ${Hexadecimal(veriMem.read(in.pc(19, 2)))}"
        )
      }
      assert(!error)
    })
  }
}
