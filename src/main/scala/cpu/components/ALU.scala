package cpu.components

import chisel3._
import chisel3.util._

object ALUTypes {
  val nop :: add :: sub :: and :: slt :: lui :: nor :: or :: xor :: slv :: sli :: srlv :: srli :: srav :: srai :: sra :: comp_is_equal :: comp_not_equal :: comp_greater_than_z :: comp_greater_than_or_e_z :: comp_less_than_z :: comp_less_than_or_e_z :: Nil =
    Enum(21)
}

class ALUIn extends Bundle {
  val inputA = Input(UInt(32.W))
  val inputB = Input(UInt(32.W))
  val shamt = Input(UInt(5.W))
  // the width of control signal should be equal to the log2 ceil number of instructions
  val aluOp = Input(UInt(5.W))
}

class ALUOut extends Bundle {
  val aluOutput = Output(UInt(32.W))
  val branchTake = Output(Bool())
}

import cpu.components.ALUTypes._

/**
  * a subset of features:
  * no op, add, sub, and, passthrough (mem ops),
  *
  * there are still a lot of unimplemented features
  *
  * the ALU of the CPU
  */
class ALU extends Module {
  val io = IO(new Bundle() {
    val input = new ALUIn
    val output = new ALUOut
  })

  io.output.aluOutput := io.input.inputA
  io.output.branchTake := false.B

  // omitting nop and passthrough, as they are the default: input A
  io.output.aluOutput := MuxLookup(
    io.input.aluOp,
    io.input.inputA,
    Array(
      add -> (io.input.inputA + io.input.inputB),
      sub -> (io.input.inputA - io.input.inputB),
      and -> (io.input.inputA & io.input.inputB),
      slt -> (io.input.inputA < io.input.inputB),
      lui -> (io.input.inputB << 16),
      nor -> ~(io.input.inputA | io.input.inputB),
      or -> (io.input.inputA | io.input.inputB),
      xor -> (io.input.inputA ^ io.input.inputB),
      // shift instructions
      slv -> (io.input.inputB << io.input.inputA(5, 0)),
      sli -> (io.input.inputB << io.input.shamt),
      srlv -> (io.input.inputB.asSInt() >> io.input.inputA(5, 0)),
      srli -> (io.input.inputB.asSInt() >> io.input.shamt),
      srav -> (io.input.inputB.asSInt() >> io.input.inputA(5, 0)),
      srai -> (io.input.inputB.asSInt() >> io.input.shamt)
    )
  )

  io.output.branchTake := MuxLookup(
    io.input.aluOp,
    false.B,
    Array(
      comp_is_equal -> (io.input.inputA === io.input.inputB),
      comp_not_equal -> !(io.input.inputA === io.input.inputB),
      comp_greater_than_z -> (io.input.inputA > 0.U),
      comp_greater_than_or_e_z -> (io.input.inputA >= 0.U),
      comp_less_than_z -> !(io.input.inputA >= 0.U),
      comp_less_than_or_e_z -> !(io.input.inputA > 0.U)
    )
  )

}
