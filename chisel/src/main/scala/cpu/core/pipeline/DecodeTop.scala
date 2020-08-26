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
    val flush   = Input(Bool())
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

  val ins = io.ins.map(_.bits)

  val hazards  = Seq.fill(conf.decodeWidth + conf.decodeBufferNum)(Module(new cpu.core.decode.Hazard(2 * 5)))
  val decodes  = Seq.fill(conf.decodeWidth)(Module(new cpu.core.decode.Decode))
  val controls = Seq.fill(conf.decodeWidth)(Module(new cpu.core.decode.Control))

  val issue = Module(new Issue())

  hazards.zip(issue.io.operands).zip(io.operands).foreach {
    case ((hazard, issue), outer) =>
      hazard.io.ops(0).addr   := issue.rs
      hazard.io.ops(0).inData := outer.rsData
      hazard.io.ops(1).addr   := issue.rt
      hazard.io.ops(1).inData := outer.rtData

      outer.rs := issue.rs
      outer.rt := issue.rt

      issue.op1 := hazard.io.ops(0).outData
      issue.op2 := hazard.io.ops(1).outData

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
    val rs    = ins(i).inst(25, 21)
    val rt    = ins(i).inst(20, 16)
    val rd    = ins(i).inst(15, 11)
    val sa    = ins(i).inst(10, 6)
    val sel   = ins(i).inst(2, 0)
    val imm16 = ins(i).inst(15, 0)
    val imm26 = ins(i).inst(25, 0)

    decodes(i).io.inst := ins(i).inst

    controls(i).io.inst.rt    := rt
    controls(i).io.inst.rd    := rd
    controls(i).io.inst.sa    := sa
    controls(i).io.inst.imm16 := imm16
    controls(i).io.inst.sel   := sel
    controls(i).io.signal     := decodes(i).io.out
    controls(i).io.inExcept   := ins(i).except

    issue.io.ins(i).valid := io.ins(i).valid
    io.ins(i).ready       := issue.io.ins(i).ready

    issue.io.ins(i).bits.instType    := decodes(i).io.out.instType
    issue.io.ins(i).bits.operation   := decodes(i).io.out.operation
    issue.io.ins(i).bits.op1Type     := decodes(i).io.out.op1Type
    issue.io.ins(i).bits.op2Type     := decodes(i).io.out.op2Type
    issue.io.ins(i).bits.write       := controls(i).io.write
    issue.io.ins(i).bits.cp0         := controls(i).io.cp0
    issue.io.ins(i).bits.imm32       := controls(i).io.imm32
    issue.io.ins(i).bits.except      := controls(i).io.except
    issue.io.ins(i).bits.imm26       := imm26
    issue.io.ins(i).bits.rs          := rs
    issue.io.ins(i).bits.rt          := rt
    issue.io.ins(i).bits.pc          := ins(i).pc
    issue.io.ins(i).bits.brPredict   := ins(i).brPredict
    issue.io.ins(i).bits.brPrHistory := ins(i).brPrHistory
    issue.io.ins(i).bits.instValid   := ins(i).instValid
  }
  issue.io.flush   := io.flush
  issue.io.stalled := io.stalled

  io.out := issue.io.out

  io.stallReq := issue.io.stallReq


  //===---------------------------------------------------------------------------===
  // for simulation only
  //===---------------------------------------------------------------------------===
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
