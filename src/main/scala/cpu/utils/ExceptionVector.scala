package cpu.utils

import chisel3._

class exceptionVector extends Bundle {
  val isBranchDelaySlot = Bool()
  val fetchException = Bool()
  val decodeException = Bool()
  val overflowException = Bool()
}

class exceptionOrR extends Module {
  val io = IO(new Bundle() {
    val input = Input(new exceptionVector)
    val output = Output(Bool())
  })
  // note: Don't take is branch delay slot
  io.output := io.input.fetchException | io.input.decodeException | io.input.overflowException
}

object isException {
  def apply(vector: exceptionVector) = {
    val o = Module(new exceptionOrR)
    vector <> o.io.input
    o.io.output
  }
}
