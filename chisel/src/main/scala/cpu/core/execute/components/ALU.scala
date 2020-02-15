// See README.md for license details.

package cpu.core.execute.components

import chisel3._
import chisel3.util._
import cpu.core.Constants._

class ALU extends Module {
  val io = IO(new Bundle {
    val op1 = Input(UInt(dataLen.W))
    val op2 = Input(UInt(dataLen.W))
    val operation = Input(UInt(opLen.W))
    val result = Output(UInt(dataLen.W))
    val overflow = Output(Bool())
  })

  val op2Com = (~io.op2).asUInt() + 1.U   // 补码 two's complement
  val add_result = io.op1 + io.op2
  val sub_result = io.op1 - io.op2

  val addOverflow = (io.operation === ALU_ADD) &&
    ((io.op1(31) & io.op2(31) & !add_result(31)) ||
      (!io.op1(31) & !io.op2(31) & add_result(31)))
  val subOverflow = (io.operation === ALU_SUB) &&
    ((io.op1(31) & op2Com(31) & !sub_result(31)) ||
      (!io.op1(31) & !op2Com(31) & sub_result(31)))

  io.overflow := addOverflow || subOverflow
  io.result := MuxLookup(io.operation, 0.U,
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
      ALU_SLT  -> (io.op1.asSInt < io.op2.asSInt).asUInt,
      ALU_SLTU -> (io.op1 < io.op2),
      ALU_ADD  -> add_result,
      ALU_ADDU -> add_result,
      ALU_SUB  -> sub_result,
      ALU_SUBU -> sub_result
      // @formatter:on
    )
  )
//  io.result.valid := io.operation =/= ALU_N
}
