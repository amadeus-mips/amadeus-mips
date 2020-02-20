package cpu.components

import chisel3._
import chisel3.util._

class AddressUnitIn extends Bundle {
  val pcPlusFour = Input(UInt(32.W))
  val branchOffSet = Input(UInt(16.W))
  // control signal determines whether it's a branch or a jump.
}

class AddressUnitOut extends Bundle {
  val branchTarget = Output(UInt(32.W))
}

class AddressUnit extends Module {
  val io = IO(new Bundle() {
    val input = new AddressUnitIn
    val output = new AddressUnitOut
  })
  io.output.branchTarget := io.input.pcPlusFour + Cat(
    Fill(14, io.input.branchOffSet(15)),
    io.input.branchOffSet,
    Fill(2, 0.U)
  )
}
