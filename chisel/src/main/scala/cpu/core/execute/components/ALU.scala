// See README.md for license details.

package cpu.core.execute.components

import chisel3._
import chisel3.util._
import cpu.core.Constants._

class ALU extends Module {
  val io = IO(new Bundle {
    val op1       = Input(UInt(dataLen.W))
    val op2       = Input(UInt(dataLen.W))
    val sa        = Input(UInt(2.W))
    val operation = Input(UInt(opLen.W))
    val result    = Output(UInt(dataLen.W))
    val overflow  = Output(Bool())
    val trap = Output(Bool())

    val lo = Input(UInt(dataLen.W))
  })

  val add_result = io.op1 + io.op2
  val sub_result = io.op1 - io.op2

  io.overflow := MuxLookup(
    io.operation,
    false.B,
    Array(
      ALU_ADD -> ((io.op1(31) === io.op2(31)) & (io.op1(31) ^ add_result(31))),
      ALU_SUB -> ((io.op1(31) ^ io.op2(31)) & (io.op1(31) ^ sub_result(31)))
    )
  )

  val clo = WireInit(32.U)
  val clz = WireInit(32.U)
  for(i <- 0 until 32) {
    when(!io.op1(i)) {
      clo := (31 - i).U
    }.otherwise {
      clz := (31 - i).U
    }
  }

  val lt  = io.op1.asSInt() < io.op2.asSInt()
  val ltu = io.op1 < io.op2
  val eq  = io.op1 === io.op2

  io.result := MuxLookup(
    io.operation,
    0.U,
    Array(
      // @formatter:off
      OP_N    -> 0.U,
      ALU_OR   -> (io.op1 | io.op2),
      ALU_AND  -> (io.op1 & io.op2),
      ALU_XOR  -> (io.op1 ^ io.op2),
      ALU_NOR  -> ~(io.op1 | io.op2),
      ALU_SLL  -> (io.op2 << io.op1(4, 0)),
      ALU_SRL  -> (io.op2 >> io.op1(4, 0)),
      ALU_SRA  -> (io.op2.asSInt >> io.op1(4, 0)).asUInt,
      ALU_SLT  -> lt,
      ALU_SLTU -> ltu,
      ALU_ADD  -> add_result,
      ALU_ADDU -> add_result,
      ALU_SUB  -> sub_result,
      ALU_SUBU -> sub_result,
      ALU_MUL  -> io.lo,
      ALU_CLO  -> clo,
      ALU_CLZ  -> clz,
      ALU_LSA  -> (io.op1 << (io.sa + 1.U) + io.op2)
      // @formatter:on
    )
  )

  io.trap := MuxLookup(
    io.operation,
    false.B,
    Seq(
      TRAP_EQ  -> eq,
      TRAP_GE  -> !lt,
      TRAP_GEU -> !ltu,
      TRAP_LT  -> lt,
      TRAP_LTU -> ltu,
      TRAP_NE  -> !eq
    )
  )
}
