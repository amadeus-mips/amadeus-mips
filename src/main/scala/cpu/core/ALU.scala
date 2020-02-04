package cpu.core
{
  import chisel3._
  // TODO: Don't import them individually! had problem with switch when imported that way
  import chisel3.util._
  import cpu.common.PhoenixConfiguration

  //TODO: compare is unimplemented
  object ALUTypes {
    val nop :: add :: sub :: and :: or :: xor :: passthrough :: shl :: shr :: Nil = Enum(9)
  }

  import ALUTypes._
  /**
    * a subset of features:
    * no op, add, sub, and, or, xor, passthrough (mem ops),
    * shift left, shift right
    *
    * there are still a lot of unimplemented features
    *
    * the ALU of the CPU
    * @param config: phoenix configuation
    */
  class ALU(implicit config: PhoenixConfiguration) extends Module {
    val io = IO(new Bundle() {
      val inputA = Input(UInt(config.regLen.W))
      val inputB = Input(UInt(config.regLen.W))
      val controlSignal = Input(UInt(4.W))
      val output = Output(UInt(config.regLen.W))
    })

    switch(io.controlSignal) {
      // imported from the constants class
      is(nop) {
        io.output := DontCare
      }
      is(add) {
        io.output := io.inputA + io.inputB
      }
      is(sub) {
        io.output := io.inputA - io.inputB
      }
      is(and) {
        io.output := io.inputA & io.inputB
      }
      is(or) {
        io.output := io.inputA | io.inputB
      }
      is(xor) {
        io.output := io.inputA ^ io.inputB
      }
      is(passthrough) {
        // note: this passes input A
        io.output := io.inputA
      }
      is(shl) {
        io.output := io.inputA << io.inputB
      }
      is(shr) {
        io.output := io.inputA >> io.inputB
      }
    }
  }
}
