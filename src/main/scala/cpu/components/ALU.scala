package cpu.components

import chisel3._
import chisel3.util._

object ALUTypes {
  val nop :: add :: sub :: and :: slt :: lui :: nor :: or :: xor :: slv :: sli :: srlv :: srli :: srav :: srai :: plusFour :: addu :: subu :: opB :: Nil =
    Enum(19)
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
  val overflow = Output(Bool())
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

  val addResult = (io.input.inputA + io.input.inputB)
  val subResult = (io.input.inputA - io.input.inputB)

  // omitting nop and passthrough, as they are the default: input A
  io.output.aluOutput := MuxLookup(
    io.input.aluOp,
    io.input.inputA,
    Array(
      add -> addResult,
      addu -> addResult,
      sub -> subResult,
      subu -> subResult,
      and -> (io.input.inputA & io.input.inputB),
      slt -> (io.input.inputA < io.input.inputB),
      lui -> (io.input.inputB << 16),
      nor -> ~(io.input.inputA | io.input.inputB),
      or -> (io.input.inputA | io.input.inputB),
      xor -> (io.input.inputA ^ io.input.inputB),
      // shift instructions
      slv -> (io.input.inputB << io.input.inputA(4, 0)),
      sli -> (io.input.inputB << io.input.shamt),
      srlv -> (io.input.inputB >> io.input.inputA(4, 0)),
      srli -> (io.input.inputB >> io.input.shamt),
      srav -> (io.input.inputB.asSInt >> io.input.inputA(4, 0)).asUInt(),
      srai -> (io.input.inputB.asSInt >> io.input.shamt).asUInt(),
      plusFour -> (io.input.inputB + 4.U),
      opB -> io.input.inputB
    )
  )

  //logic behind this:
  //overflow: can't represent with the current hardware
  //if add A and B are different signs, overflow cannot occur
  //if add A and B have different signs, overflow can occur: when the result sign is not the A sign
  // sub is similar
  io.output.overflow := MuxLookup(
    io.input.aluOp,
    false.B,
    Array(
      add -> (!((io.input.inputA(31) ^ io.input.inputB(31)).asBool) && (io.input.inputA(31) ^ addResult(31)).asBool),
      sub -> ((io.input.inputA(31) ^ io.input.inputB(31)) & (io.input.inputA(31) ^ subResult(31)))
    )
  )
}
