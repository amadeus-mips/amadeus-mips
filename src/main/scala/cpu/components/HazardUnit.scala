package cpu.components

import chisel3._
import chisel3.util._

/**
  * naming convention:
  * if there are 2 stages, then value is read
  * from the stage registers
  * if there are 1 stage, then value is read
  * by the output of that stage ( not the product
  * of the previous stage
  */

class HazardUnitIn extends Bundle {
  val idRs          = Input(UInt(5.W))
  val idRt          = Input(UInt(5.W))
  val idEXMemread = Input(Bool())
  val idBranchTake  = Input(Bool())
  val idIsJump = Input(Bool())
  val idEXRd     = Input(UInt(5.W))
}

class HazardUnitOut extends Bundle {

  /**
    * when pcWrite is 0, then pc = pc + 4
    * when pcWrite is 1, then pc = branchTarget
    * when pcWrite is 2, then pc = pc
    * when pcWrite is 4, then pc = J target
    */
  val pcWrite      = Output(UInt(3.W))

  // don't update ifID stage reg
  val ifIDBubble  = Output(Bool())
  // flush the ifID stage Reg
  val ifIDFlush   = Output(Bool())
  // insert a bubble to stage reg
  val idEXBubble  = Output(Bool())

  /**
    * difference between a bubble and a flush:
    */
}

// note: detect as early as possible
class HazardUnit extends Module {
  val io = IO(new Bundle {
    val input = new HazardUnitIn
    val output = new HazardUnitOut
 })

  // default actions, overriden by later assignments
  io.output.pcWrite      := 0.U
  io.output.ifIDBubble  := false.B
  io.output.idEXBubble  := false.B
  io.output.ifIDFlush   := false.B

  // Load to use hazard.

  // " this instruction " is in decode stage
  // the ld instruction is in execute stage
  // source operands matches one of its dst regs
  when (io.input.idEXMemread &&
    (io.input.idEXRd === io.input.idRs || io.input.idEXRd === io.input.idRt)) {
    // make pc stay the same
    /**
      * |  IF  |  ID  |  EXE  |  MEM  |  WB  |
      * pc + 4 | add  |   ld  |   ->  |  ->  |
      * F      S       G       G      G
      * flush instruction from pc+4, redo the fetch
      * don't update instruction in stall ( don't discard, still need
      * in later stages)
      */
    io.output.pcWrite     := 2.U
    io.output.ifIDBubble := true.B
    io.output.idEXBubble := true.B
  }

  // branch flush
  // this is done in the instruction decoding stage
  when (io.input.idBranchTake) {
    /**
      * |  IF  |  ID  |  EXE  |  MEM  |  WB  |
      * pc + 4 | b Y  |   ->  |   ->  |  ->  |
      *        F      F       G       G      G
      *        flush IF, ID becomes no-op
      *        because I don't do it in later stages
      */
    /**
      * essentially flushing the results from stage IF and ID
      * change PC to target
      * IF: PC + 4 (discard)
      * ID: Branch ( discard ): don't pass to exe, pass nop
      * this is for the pipeline where branch write back is
      * in the mem stage
      * notice: putting branching in stage 2 is really expensive
      */
    io.output.pcWrite := 1.U
    io.output.ifIDFlush  := true.B
    io.output.idEXBubble  := true.B
//    io.output.exMemBubble := true.B
  }

  // jump flush
  // this is done in the decoding stage
  when (io.input.idIsJump) {
    /**
      * similar to branch
      * change PC to target
      * discard result of IF, which is PC + 4
      * discard result of ID, which is jump
      */
    io.output.pcWrite := 4.U
    io.output.ifIDFlush := true.B
    io.output.idEXBubble := true.B
  }
}