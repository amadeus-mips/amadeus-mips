package cpu.components

import chisel3._
import chisel3.util._

  object ALUTypes {
    val nop :: add :: sub :: and :: Nil = Enum(4)
  }

class ALUInputIO extends Bundle {
  val inputA = Input(UInt(32.W))
  val inputB = Input(UInt(32.W))
  // the width of control signal should be equal to the log2 ceil number of instructions
  val aluOp = Input(UInt(3.W))
}

class ALUOutputIO extends Bundle {
  val aluOutput = Output(UInt(32.W))
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
    val input = new ALUInputIO
    val output = new ALUOutputIO
 })

  // omitting nop and passthrough, as they are the default: input A
  io.output.aluOutput := MuxLookup(io.input.aluOp, io.input.inputA,Array(
    add -> (io.input.inputA + io.input.inputB),
    sub -> (io.input.inputA - io.input.inputB),
    and -> (io.input.inputA & io.input.inputB),
  ))



  }
