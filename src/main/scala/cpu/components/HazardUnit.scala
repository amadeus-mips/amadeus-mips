package cpu.components

import chisel3._
import chisel3.util._

class HazardUnitIn extends Bundle {
  val rs          = Input(UInt(5.W))
  val rt          = Input(UInt(5.W))
  val idEXMemread = Input(Bool())
  val idExBranchTake  = Input(Bool())
  val idEXRd     = Input(UInt(5.W))
}

class HazardUnitOut extends Bundle {
  val pcwrite      = Output(UInt(2.W))
  val ifIDBubble  = Output(Bool())
  val idEXBubble  = Output(Bool())
  val exMemBubble = Output(Bool())
  val ifIDFlush   = Output(Bool())
}

class HazardUnit extends Module {
  val io = IO(new Bundle {
    val input = new HazardUnitIn
    val output = new HazardUnitOut
 })

  // default
  io.output.pcwrite      := 0.U
  io.output.ifIDBubble  := false.B
  io.output.idEXBubble  := false.B
  io.output.exMemBubble := false.B
  io.output.ifIDFlush   := false.B

  // Load to use hazard.
  when (io.input.idEXMemread &&
    (io.input.idEXRd === io.input.rs || io.input.idEXRd === io.input.rt)) {
    io.output.pcwrite     := 2.U
    io.output.ifIDBubble := true.B
    io.output.idEXBubble := true.B
  }

  // branch flush
  when (io.input.idExBranchTake) {
    io.output.pcwrite := 1.U // use the PC from mem stage
    io.output.ifIDFlush  := true.B
    io.output.idEXBubble  := true.B
    io.output.exMemBubble := true.B
  }
}