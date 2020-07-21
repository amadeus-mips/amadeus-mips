// See README.md for license details.

package cpu.core.execute.components

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import cpu.core.Constants._
import shared.{ValidBundle, Util}

class Branch extends Module {
  val io = IO(new Bundle {
    val op1 = Input(UInt(dataLen.W))
    val op2 = Input(UInt(dataLen.W))
    val operation = Input(UInt(opLen.W))
    val imm26 = Input(UInt(26.W))
    val pc = Input(UInt(addrLen.W))
    val branch = Output(ValidBundle(32))
  })
  val pcPlus4 = io.pc + 4.U
  val imm16 = io.imm26(15, 0)
  val BImmExt = Cat(Util.signedExtend(imm16, to = 30), 0.U(2.W))
  val BTarget = pcPlus4 + BImmExt
  val JTarget = Cat(pcPlus4(31, 28), io.imm26, 0.U(2.W))

  io.branch.valid := MuxLookup(io.operation, false.B,
    Array(
      // @formatter:off
      BR_JR     -> true.B,
      BR_JALR   -> true.B,
      BR_J      -> true.B,
      BR_JAL    -> true.B,
      BR_EQ     -> (io.op1 === io.op2),
      BR_NE     -> (io.op1 =/= io.op2),
      BR_GTZ    -> (!io.op1(31) && (io.op1 =/= 0.U)),
      BR_GEZ    -> (!io.op1(31)),
      BR_GEZAL  -> (!io.op1(31)),
      BR_LTZ    -> io.op1(31),
      BR_LTZAL  -> io.op1(31),
      BR_LEZ    -> (!(!io.op1(31) && (io.op1 =/= 0.U)))
      // @formatter:on
    )
  )
  io.branch.bits := MuxLookup(io.operation, BTarget,
    Array(
      BR_JR -> io.op1,
      BR_JALR -> io.op1,
      BR_J -> JTarget,
      BR_JAL -> JTarget
    )
  )

}
