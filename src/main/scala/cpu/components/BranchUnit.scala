package cpu.components

import chisel3._
import chisel3.util._

object branchOpEnum {
  val comp_is_equal :: comp_not_equal :: comp_greater_than_z :: comp_greater_than_or_e_z :: comp_less_than_z :: comp_less_than_or_e_z :: Nil =
    Enum(6)
}

class BranchUnitIn extends Bundle {
  // the value for register rs
  val regRs = Input(UInt(32.W))
  // the value for register rt
  val regRt = Input(UInt(32.W))
  // I think there are more than 4
  // branching options?
  val branchOp = Input(UInt(3.W))

  // whether the branch gets taken or not
}

import branchOpEnum._
class BranchUnitOut extends Bundle {
  val branchTake = Output(Bool())
}

class BranchUnit extends Module {
  val io = IO(new Bundle() {
    val input = new BranchUnitIn
    val output = new BranchUnitOut
  })

  val notEqual = (io.input.regRs - io.input.regRt).orR
  val equalToZero = io.input.regRs.orR
  // if the MSB is 0, then it's greater than zero
  val greaterThanZero = !io.input.regRs(31)
  io.output.branchTake := MuxLookup(
    io.input.branchOp,
    false.B,
    Array(
      comp_is_equal -> !notEqual,
      comp_not_equal -> notEqual,
      comp_greater_than_z -> greaterThanZero,
      comp_greater_than_or_e_z -> (greaterThanZero | equalToZero),
      comp_less_than_z -> !(greaterThanZero | equalToZero),
      comp_less_than_or_e_z -> !greaterThanZero
    )
  )

}
