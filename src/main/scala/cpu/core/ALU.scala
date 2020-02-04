package cpu.core
{
  import chisel3._
  import chisel3.util._
  import cpu.common._

  object ALUTypes {
    val nop :: add :: sub :: and :: comp_is_equal :: comp_not_equal :: comp_greater_than_z :: comp_greater_than_or_e_z :: comp_less_than_z :: comp_less_than_or_e_z :: passthrough :: Nil = Enum(11)
  }

  class ALUInputIO(implicit config: PhoenixConfiguration ) extends Bundle {
    val inputA = Input(UInt(config.regLen.W))
    val inputB = Input(UInt(config.regLen.W))
    // the width of control signal should be equal to the log2 ceil number of instructions
    val controlSignal = Input(UInt(log2Ceil(config.AluOps).W))
  }

  class ALUOutputIO(implicit config: PhoenixConfiguration) extends Bundle {
    val aluOutput = Output(UInt(config.regLen.W))
    val branchTake = Output(Bool())
  }

  import ALUTypes._
  /**
    * a subset of features:
    * no op, add, sub, and, passthrough (mem ops),
    *
    * there are still a lot of unimplemented features
    *
    * the ALU of the CPU
    * @param config: phoenix configuation
    */
  class ALU(implicit config: PhoenixConfiguration) extends Module {
    val io = IO(new Bundle() {
                  val input = new ALUInputIO
                  val output = new ALUOutputIO
         })

    switch(io.input.controlSignal) {
      // imported from the constants class
      is(nop) {
        io.output.aluOutput := DontCare
        io.output.branchTake := DontCare
      }
      is(add) {
        io.output.aluOutput := io.input.inputA + io.input.inputB
        io.output.branchTake := DontCare
      }
      is(sub) {
        io.output.aluOutput := io.input.inputA - io.input.inputB
        io.output.branchTake := DontCare
      }
      is(and) {
        io.output.aluOutput := io.input.inputA & io.input.inputB
        io.output.branchTake := DontCare
      }
      is(comp_is_equal) {
        io.output.branchTake := (io.input.inputA === io.input.inputB)
        io.output.aluOutput := DontCare
      }
      is(comp_not_equal) {
        io.output.branchTake := (io.input.inputA =/= io.input.inputB)
        io.output.aluOutput := DontCare
      }
      is(comp_greater_than_z) {
        io.output.branchTake := (io.input.inputA > 0.U)
        io.output.aluOutput := DontCare
      }
      is(comp_greater_than_or_e_z) {
        io.output.branchTake := (io.input.inputA >= 0.U)
        io.output.aluOutput := DontCare
      }
      is(comp_less_than_z) {
        io.output.branchTake := (io.input.inputA < 0.U)
        io.output.aluOutput := DontCare
      }
      is(comp_less_than_or_e_z) {
        io.output.branchTake := (io.input.inputA <= 0.U)
        io.output.aluOutput := DontCare
      }
      is(passthrough) {
        // note: this passes input A
        io.output.aluOutput := io.input.inputA
        io.output.branchTake := DontCare
     }
    }
  }
}
