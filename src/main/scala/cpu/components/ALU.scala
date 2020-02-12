package cpu.components

import chisel3._
import chisel3.util._

  object ALUTypes {
    val nop :: add :: sub :: and :: comp_is_equal :: comp_not_equal :: comp_greater_than_z :: comp_greater_than_or_e_z :: comp_less_than_z :: comp_less_than_or_e_z :: passthrough :: Nil = Enum(11)
  }

  class ALUInputIO extends Bundle {
    val inputA = Input(UInt(32.W))
    val inputB = Input(UInt(32.W))
    // the width of control signal should be equal to the log2 ceil number of instructions
    val controlSignal = Input(UInt(4.W))
  }

  class ALUOutputIO extends Bundle {
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
    * @param config: phoenix configuation
    */
  class ALU extends Module {
    val io = IO(new Bundle() {
                  val input = new ALUInputIO
                  val output = new ALUOutputIO
         })

    // omitting nop and passthrough, as they are the default: input A
    io.output.aluOutput := MuxLookup(io.input.controlSignal, io.input.inputA,Array(
      add -> (io.input.inputA + io.input.inputB),
      sub -> (io.input.inputA - io.input.inputB),
      and -> (io.input.inputA & io.input.inputB),
      comp_is_equal -> DontCare,
      comp_not_equal -> DontCare,
      comp_greater_than_z -> DontCare,
      comp_greater_than_or_e_z -> DontCare,
      comp_less_than_z -> DontCare,
      comp_less_than_or_e_z -> DontCare
    ))

    io.output.branchTake := MuxLookup(io.input.controlSignal, false.B, Array(
      comp_is_equal -> (io.input.inputA === io.input.inputB),
      comp_not_equal -> !(io.input.inputA === io.input.inputB),
      comp_greater_than_z -> (io.input.inputA > 0.U) ,
      comp_greater_than_or_e_z -> (io.input.inputA >= 0.U),
      comp_less_than_z -> !(io.input.inputA >= 0.U),
      comp_less_than_or_e_z -> !(io.input.inputA > 0.U)
    ))

  }
