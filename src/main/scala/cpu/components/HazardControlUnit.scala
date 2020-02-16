package cpu.components

import chisel3._

class HazardControlUnitIO extends Bundle {
  // input: when an exception occurs
  val overflow = Input(Bool())

  // output the hazard control signals
  val pcSelect = Output(UInt(2.W))
  val IF_ID_Bubble = Output(Bool())
  val ID_EX_Bubble = Output(Bool())
  val EX_MEM_Bubble = Output(Bool())
  val IF_ID_Flush = Output(Bool())
}

class HazardControlUnit extends Module{
  val io = IO(new HazardControlUnitIO)
  //TODO: for testing purpose, to change
  io := DontCare
}
