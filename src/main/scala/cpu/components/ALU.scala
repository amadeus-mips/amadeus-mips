package cpu.components

import chisel3._
import chisel3.util._

object ALUTypes {
  val nop :: add :: sub :: and :: comp_is_equal :: comp_not_equal :: comp_greater_than_z :: comp_greater_than_or_e_z :: comp_less_than_z :: comp_less_than_or_e_z :: Nil = Enum(11)
}

class ALUIn extends Bundle {
  val inputA = Input(UInt(32.W))
  val inputB = Input(UInt(32.W))
  // the width of control signal should be equal to the log2 ceil number of instructions
  val aluOp = Input(UInt(4.W))
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

  // omitting nop and passthrough, as they are the default: input A
  io.output.aluOutput := MuxLookup(io.input.aluOp, io.input.inputA,Array(
    add -> (io.input.inputA + io.input.inputB),
    sub -> (io.input.inputA - io.input.inputB),
    and -> (io.input.inputA & io.input.inputB),
  ))

  io.output.branchTake := MuxLookup(io.input.aluOp, false.B, Array(
    comp_is_equal -> (io.input.inputA === io.input.inputB),
    comp_not_equal -> !(io.input.inputA === io.input.inputB),
    comp_greater_than_z -> (io.input.inputA > 0.U) ,
    comp_greater_than_or_e_z -> (io.input.inputA >= 0.U),
    comp_less_than_z -> !(io.input.inputA >= 0.U),
    comp_less_than_or_e_z -> !(io.input.inputA > 0.U)
  ))

}
