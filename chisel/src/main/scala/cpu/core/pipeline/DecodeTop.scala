// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.experimental.loadMemoryFromFile
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.WriteBundle
import cpu.core.bundles.stages.{IdExeBundle, If1IdBundle}
import firrtl.annotations.MemoryLoadFileType

class DecodeTop(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val in = Input(new If1IdBundle)

    val exeWR = Input(new WriteBundle)
    val memWR = Input(new WriteBundle)
    val wbWR  = Input(new WriteBundle)

    val rsData = Input(UInt(dataLen.W)) // from register-file
    val rtData = Input(UInt(dataLen.W)) // ^

    val out = Output(new IdExeBundle)

    val stallReq = Output(Bool()) // to pipeLine control
  })

  val inst = io.in.inst

  val hazard  = Module(new cpu.core.decode.Hazard)
  val decode  = Module(new cpu.core.decode.Decode)
  val control = Module(new cpu.core.decode.Control)

  val rs    = inst(25, 21)
  val rt    = inst(20, 16)
  val rd    = inst(15, 11)
  val sa    = inst(10, 6)
  val imm16 = inst(15, 0)
  val imm26 = inst(25, 0)

  hazard.io.wrs(0) := io.exeWR
  hazard.io.wrs(1) := io.memWR
  hazard.io.wrs(2) := io.wbWR

  hazard.io.ops(0).addr   := rs
  hazard.io.ops(0).inData := io.rsData
  hazard.io.ops(0).typ    := decode.io.out.op1Type

  hazard.io.ops(1).addr   := rt
  hazard.io.ops(1).inData := io.rtData
  hazard.io.ops(1).typ    := decode.io.out.op2Type

  /** 根据指令解码获取控制信号 */
  decode.io.inst := inst

  control.io.inst.rt    := rt
  control.io.inst.rd    := rd
  control.io.inst.sa    := sa
  control.io.inst.imm16 := imm16
  control.io.inst.sel   := inst(2, 0)

  control.io.signal   := decode.io.out
  control.io.inExcept := io.in.except
  control.io.rsData   := hazard.io.ops(0).outData
  control.io.rtData   := hazard.io.ops(1).outData

  io.out.instType  := decode.io.out.instType
  io.out.operation := decode.io.out.operation

  io.out.op1    := control.io.op1
  io.out.op2    := control.io.op2
  io.out.write  := control.io.write
  io.out.cp0 := control.io.cp0
  io.out.except := control.io.except

  io.out.pc := io.in.pc
  io.out.imm26 := imm26
  io.out.inDelaySlot := io.in.inDelaySlot
  io.out.brPredict := io.in.brPredict

  io.stallReq := hazard.io.stallReq

  if (!conf.build) {
    val veriMem = Mem(BigInt("4FFFF", 16), UInt(32.W))
    loadMemoryFromFile(veriMem, conf.memoryFile, MemoryLoadFileType.Hex)
    when (!(!io.in.instValid || io.in.instValid && io.in.inst === veriMem.read(io.in.pc(19, 2)))) {
      printf(p"the address is ${io.in.pc}, the wrong instruction is ${io.in.inst}, the correct instruction should be ${veriMem.read(io.in.pc(19, 2))}")
    }
    assert(!io.in.instValid || io.in.instValid && io.in.inst === veriMem.read(io.in.pc(19, 2)))
  }
}
