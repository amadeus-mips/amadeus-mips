// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.experimental.loadMemoryFromFile
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.stages.IdExeBundle
import cpu.core.bundles.{InstructionFIFOEntry, WriteBundle}
import firrtl.annotations.MemoryLoadFileType

class DecodeTop(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val in      = Input(new InstructionFIFOEntry())
    val inValid = Input(Bool())

    val exeWR = Input(new WriteBundle)
    val mem0WR = Input(new WriteBundle)
    val mem1WR = Input(new WriteBundle)
    val mem2WR = Input(new WriteBundle)
    val wbWR  = Input(new WriteBundle)

    val rsData = Input(UInt(dataLen.W)) // from register-file
    val rtData = Input(UInt(dataLen.W)) // ^

    val out = Output(new IdExeBundle)

    val stallReq = Output(Bool()) // to pipeLine control
  })

  val in = Mux(io.inValid, io.in, 0.U.asTypeOf(io.in))

  val hazard  = Module(new cpu.core.decode.Hazard(5))
  val decode  = Module(new cpu.core.decode.Decode)
  val control = Module(new cpu.core.decode.Control)

  val rs    = in.inst(25, 21)
  val rt    = in.inst(20, 16)
  val rd    = in.inst(15, 11)
  val sa    = in.inst(10, 6)
  val imm16 = in.inst(15, 0)
  val imm26 = in.inst(25, 0)

  hazard.io.wrs(0) := io.exeWR
  hazard.io.wrs(1) := io.mem0WR
  hazard.io.wrs(2) := io.mem1WR
  hazard.io.wrs(3) := io.mem2WR
  hazard.io.wrs(4) := io.wbWR

  hazard.io.ops(0).addr   := rs
  hazard.io.ops(0).inData := io.rsData
  hazard.io.ops(0).typ    := decode.io.out.op1Type

  hazard.io.ops(1).addr   := rt
  hazard.io.ops(1).inData := io.rtData
  hazard.io.ops(1).typ    := decode.io.out.op2Type

  /** 根据指令解码获取控制信号 */
  decode.io.inst := in.inst

  control.io.inst.rt    := rt
  control.io.inst.rd    := rd
  control.io.inst.sa    := sa
  control.io.inst.imm16 := imm16
  control.io.inst.sel   := in.inst(2, 0)

  control.io.signal   := decode.io.out
  control.io.inExcept := in.except
  control.io.rsData   := hazard.io.ops(0).outData
  control.io.rtData   := hazard.io.ops(1).outData

  io.out.instType  := decode.io.out.instType
  io.out.operation := decode.io.out.operation

  io.out.op1    := control.io.op1
  io.out.op2    := control.io.op2
  io.out.write  := control.io.write
  io.out.cp0    := control.io.cp0
  io.out.except := control.io.except

  io.out.pc          := in.pc
  io.out.imm26       := imm26
  io.out.inDelaySlot := in.inDelaySlot
  io.out.brPredict   := in.brPredict
  io.out.instValid   := in.valid

  io.stallReq := hazard.io.stallReq

  if (conf.compareRamDirectly) {
    val veriMem = Mem(BigInt("4FFFF", 16), UInt(32.W))
    loadMemoryFromFile(veriMem, conf.memoryFile, MemoryLoadFileType.Hex)
    val error = !(!io.in.instValid || io.in.instValid && io.in.inst === veriMem.read(io.in.pc(19, 2))) && io.in.pc >= "h9fc00000".U
    when (error) {
      printf(p"the request is ${io.in.pc}, the wrong instruction is ${io.in.inst}, the correct instruction should be ${veriMem.read(io.in.pc(19, 2))}")
    }
    assert(!error)
  }
}
