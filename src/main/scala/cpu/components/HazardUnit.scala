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
  // rs and rt register in decode stage
  val idRs          = Input(UInt(5.W))
  val idRt          = Input(UInt(5.W))
  // is jump signal in instruction decode stage
  val idIsJump = Input(Bool())

  // mem read signal in id-ex pass
  val idEXMemread = Input(Bool())
  // dst reg passed by stage reg
  val idEXDstReg     = Input(UInt(5.W))

  // branchtake signal result in execute stage
  val exMemBranchTake  = Input(Bool())
}

/**
  * difference between a bubble and a flush:
  * a bubble is a nop, does not destroy the data, only alter control
  * flush flushes data and control
  */
class HazardUnitOut extends Bundle {

  /**
    * when pcWrite is 0, then pc = pc + 4
    * when pcWrite is 1, then pc = branchTarget
    * when pcWrite is 2, then pc = pc
    * when pcWrite is 4, then pc = J target
    */
  val pcWrite      = Output(UInt(3.W))

  // don't update ifID stage reg
  val ifIDStall  = Output(Bool())
  // flush the ifID stage Reg
  val ifIDFlush   = Output(Bool())
  // insert a bubble to stage reg
  val idEXFlush  = Output(Bool())

  val exMemFlush = Output(Bool())

}

// note: detect as early as possible
class HazardUnit extends Module {
  val io = IO(new Bundle {
    val input = new HazardUnitIn
    val output = new HazardUnitOut
 })

  // default actions, overriden by later assignments
  io.output.pcWrite      := 0.U
  io.output.ifIDStall  := false.B
  io.output.ifIDFlush   := false.B
  io.output.idEXFlush  := false.B
  io.output.exMemFlush := false.B
  // Load to use hazard.

  // " this instruction " is in decode stage
  // the ld instruction is in execute stage
  // source operands matches one of its dst regs
  when (io.input.idEXMemread &&
    (io.input.idEXDstReg === io.input.idRs || io.input.idEXDstReg === io.input.idRt)) {
    // make pc stay the same
    /**
      * |  IF  |  ID  |  EXE  |  MEM  |  WB  |
      * pc + 4 | add  |   ld  |   ->  |  ->  |
      *        S      F       G       G      G
      * flush instruction from pc+4, halt the unit, redo the fetch
      * re-pipe the data from if to id
      * at the end of this cycle, id-ex is flushed, if-id unchanged, pc unchanged
      * don't update instruction in stall ( don't discard, still need
      * in later stages)
      */
    io.output.pcWrite     := 2.U
    io.output.ifIDStall := true.B
    io.output.idEXFlush := true.B
  }

  // branch flush
  // this is done in the instruction decoding stage
  when (io.input.exMemBranchTake) {
    /**
      * |  IF  |  ID  |  EXE  |  MEM  |  WB
      * pc + 12| pc+8 |  pc+4 |  br   |  ->
      *        F      F       F       G
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
    io.output.idEXFlush  := true.B
    io.output.exMemFlush := true.B
  }

  // jump flush
  // this is done in the decoding stage
  when (io.input.idIsJump) {
    /**
      *  IF  |  ID  |  EX  |  MEM  |  WB
      *  npc |  J   |  -   |   -   |  -
      *      F      F      -       -
      */
    /**
      * similar to branch
      * change PC to target
      * discard result of IF, which is PC + 4
      * discard result of ID, which is jump
      */
    io.output.pcWrite := 4.U
    io.output.ifIDFlush := true.B
    io.output.idEXFlush := true.B
  }
}