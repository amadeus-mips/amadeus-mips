package cpu.components

import chisel3._
import chisel3.util._

class CP0In extends Bundle {
  val writeEnable = Input(Bool())
  val readEnable = Input(Bool())
  // select from one of 32 status registers
  val regSelect = Input(UInt(5.W))
  // subfield of status register select
  // like reg 16.1
  val sel = Input(UInt(2.W))

  val pc = Input(UInt(32.W))
  // if the instruction is in the branch delay slot
  val isBranchDelaySlot = Input(Bool())
}

class CP0Out extends Bundle {
  val epc = Output(UInt(32.W))
}

//TODO: initialization values
class CP0 extends Module {
  val io = IO(new Bundle() {
    val input = new CP0In
    val output = new CP0Out
  })
  // register 9
  // count register, work with compare ( 11 )
  val regCount = Reg(UInt(32.W))

  // register 11
  // compare register, work with count ( 9 )
  val regCompare = Reg(UInt(32.W))
  // register 12
  // initialize the status register
  val regSR = Reg(UInt(32.W))
  // register 13
  // cause of exception ( interrupt )
  val regCause = Reg(UInt(32.W))
  // register 14
  // initialize the epc register
  val regEPC = Reg(UInt(32.W))
}
