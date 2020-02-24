package cpu.components

import chisel3._
import chisel3.util._

object ALUTypes {
  val nop :: add :: sub :: and :: slt :: lui :: nor :: or :: xor :: slv :: sli :: srlv :: srli :: srav :: srai :: plusFour :: Nil =
    Enum(16)
}

class ALUIn extends Bundle {
  val inputA = Input(UInt(32.W))
  val inputB = Input(UInt(32.W))
  val shamt = Input(UInt(5.W))
  // the width of control signal should be equal to the log2 ceil number of instructions
  val aluOp = Input(UInt(4.W))
}

class ALUOut extends Bundle {
  val aluOutput = Output(UInt(32.W))
//  val overflow = Output(UInt(32.W))
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
      slv -> (io.input.inputB << io.input.inputA(4, 0)),
      sli -> (io.input.inputB << io.input.shamt),
      srlv -> (io.input.inputB >> io.input.inputA(4, 0)),
      srli -> (io.input.inputB >> io.input.shamt),
      srav -> (io.input.inputB.asSInt >> io.input.inputA(4, 0)).asUInt(),
      srai -> (io.input.inputB.asSInt >> io.input.shamt).asUInt(),
      plusFour -> (io.input.inputB + 4.U)
    )
  )

//  io.output.overflow := MuxLookup(
//    io.input.aluOp,
//    false.B,
//    Array(
//      add -> (Cat(io.input.inputA(31), io.input.inputA),)
//    )
//  )
}
