package cpu.components

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._

// TODO: change this to ChiselEnum someday
// too bad it doesn't work now
object branchOpEnum {
  val comp_dontcare :: comp_is_equal :: comp_not_equal :: comp_greater_than_z :: comp_greater_than_or_e_z :: comp_less_than_z :: comp_less_than_or_e_z :: Nil = Enum(7)
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

  //TODO: seems that a lot of stuff are available here for optimization
  io.output.branchTake := MuxLookup(io.input.branchOp, false.B, Array(
    comp_is_equal -> (io.input.regRs === io.input.regRt),
    comp_not_equal -> !(io.input.regRs === io.input.regRt),
    comp_greater_than_z -> (io.input.regRs > 0.U) ,
    comp_greater_than_or_e_z -> (io.input.regRs >= 0.U),
    comp_less_than_z -> !(io.input.regRs >= 0.U),
    comp_less_than_or_e_z -> !(io.input.regRs > 0.U)
  ))

}
