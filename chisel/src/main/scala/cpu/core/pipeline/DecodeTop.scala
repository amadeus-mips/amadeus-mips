// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.experimental.loadMemoryFromFile
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.InstructionFIFOEntry
import cpu.core.bundles.stages.IdExeBundle
import firrtl.annotations.MemoryLoadFileType

class DecodeTop(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val in      = Input(new InstructionFIFOEntry())
    val inValid = Input(Bool())

    val out = Output(new IdExeBundle)

    val stallReq = Output(Bool()) // to pipeLine control
  })

  val in = Mux(io.inValid, io.in, 0.U.asTypeOf(io.in))

  val decode  = Module(new cpu.core.decode.Decode)
  val control = Module(new cpu.core.decode.Control)

  val rs    = in.inst(25, 21)
  val rt    = in.inst(20, 16)
  val rd    = in.inst(15, 11)
  val sa    = in.inst(10, 6)
  val imm16 = in.inst(15, 0)
  val imm26 = in.inst(25, 0)

  /** 根据指令解码获取控制信号 */
  decode.io.inst := in.inst

  control.io.inst.rt    := rt
  control.io.inst.rd    := rd
  control.io.inst.sa    := sa
  control.io.inst.imm16 := imm16
  control.io.inst.sel   := in.inst(2, 0)
  control.io.signal     := decode.io.out
  control.io.inExcept   := in.except

  io.out.instType  := decode.io.out.instType
  io.out.operation := decode.io.out.operation

  io.out.imm      := control.io.imm
  io.out.rs.bits  := rs
  io.out.rt.bits  := rt
  io.out.rs.valid := decode.io.out.op1Type === OPn_RF
  io.out.rt.valid := decode.io.out.op2Type === OPn_RF
  io.out.write    := control.io.write
  io.out.cp0      := control.io.cp0
  io.out.except   := control.io.except

  io.out.pc          := in.pc
  io.out.imm26       := imm26
  io.out.inDelaySlot := in.inDelaySlot
  io.out.brPredict   := in.brPredict
  io.out.instValid   := in.valid

  io.stallReq := false.B

  if (conf.compareRamDirectly) {
    val veriMem = Mem(BigInt("4FFFF", 16), UInt(32.W))
    loadMemoryFromFile(veriMem, conf.memoryFile, MemoryLoadFileType.Hex)
    val error = !(!in.valid || in.valid && in.inst === veriMem.read(in.pc(19, 2))) && io.in.pc >= "h9fc00000".U
    when(error) {
      printf(
        p"the request is 0x${Hexadecimal(in.pc)}, " +
          p"the wrong instruction is 0x${Hexadecimal(in.inst)}, " +
          p"the correct instruction should be ${Hexadecimal(veriMem.read(io.in.pc(19, 2)))}"
      )
    }
    assert(!error)
  }
}
