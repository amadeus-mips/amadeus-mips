package cpu.components

import chisel3._

/**
  * naming convention:
  * if there are 2 stages, then value is read
  * from the stage registers
  * if there are 1 stage, then value is read
  * by the output of that stage ( not the product
  * of the previous stage
  */
class HazardUnitIn extends Bundle {

  // rs and rt register in decode stage, for stalling
  val idRs = Input(UInt(5.W))
  val idRt = Input(UInt(5.W))

  // id to ex rs and rt, for bypassing
  val idEXRs = Input(UInt(5.W))
  val idEXRt = Input(UInt(5.W))

  // mem read signal in id-ex pass, for stalling
  val idEXMemread = Input(Bool())
  // dst reg passed by stage reg
  val idEXDstReg = Input(UInt(5.W))

  // whether take branch in execute stage, for flushing
  val exBranchTake = Input(Bool())
  // whether take the jump in execute stage, for flushing
  val exIsJump = Input(Bool())

  // WB data in memory stage
  // the instruction between execute and memory stage
  // 's dst reg address and write back enable
  // for bypassing
  val exMemRegDst = Input(UInt(5.W))
  val exMemRegWriteEnable = Input(Bool())

  // WB data in write back stage
  // the instruction between memory and write back stage
  //'s dst reg address and write back enable
  // for bypassing
  val memWBRegDst = Input(UInt(5.W))
  val memWBRegWriteEnable = Input(Bool())

  val isException = Input(Bool())
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
    * when pcWrite is 3, then pc = J target
    */
  val pcWrite = Output(UInt(2.W))

  // don't update ifID stage reg
  val ifIDStall = Output(Bool())
  // flush the ifID stage Reg
  val ifIDFlush = Output(Bool())
  // insert a bubble to stage reg
  val idEXFlush = Output(Bool())

  val exMemFlush = Output(Bool())

  // forward signals
  //----------------------------------------------------------------------------------------
  // ALU bypass, jump bypass and branch bypass ( in exe stage )
  //----------------------------------------------------------------------------------------
  // the mux control signal at op A
  val forwardRs = Output(UInt(2.W))
  // the mux control signal for Op B
  val forwardRt = Output(UInt(2.W))

  //----------------------------------------------------------------------------------------
  // memory bypass
  //----------------------------------------------------------------------------------------
  val forwardMemWriteData = Output(Bool())

  val memWBFlush = Output(Bool())
}

// note: detect as early as possible
// only way to do stalling: flush. Cannot enter next stage ( exceptions )
class HazardUnit extends Module {
  val io = IO(new Bundle {
    val input = new HazardUnitIn
    val output = new HazardUnitOut
  })

  // default actions, overriden by later assignments
  io.output.pcWrite := 0.U
  io.output.ifIDStall := false.B
  io.output.ifIDFlush := false.B
  io.output.idEXFlush := false.B
  io.output.exMemFlush := false.B
  io.output.memWBFlush := false.B

  //----------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------
  // stalling
  //----------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------

  // Load to use hazard.
  // this resolves alu, branching and jump
  // " this instruction " is in decode stage
  // the ld instruction is in execute stage
  // source operands matches one of its dst regs
  when(
    io.input.idEXMemread &&
      (io.input.idEXDstReg === io.input.idRs || io.input.idEXDstReg === io.input.idRt)
  ) {
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
    io.output.pcWrite := 2.U
    io.output.ifIDStall := true.B
    io.output.idEXFlush := true.B
  }

  // branch flush
  // this is done in the instruction decoding stage
  when(io.input.exBranchTake) {

    /**
      * |  IF  |  ID  |  EXE  |  MEM  |  WB
      * pc + 8 | delay|  br   |   g   |  ->
      *        F      G       G       G
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
      */
    io.output.pcWrite := 1.U
    io.output.ifIDFlush := true.B
  }

  // jump flush
  // this is done in the decoding stage
  when(io.input.exIsJump) {

    /**
      *  IF  |  ID  |  EX  |  MEM  |  WB
      *  pc+8| delay|  j   |   -   |  -
      *      F            -       -
      */
    /**
      * similar to branch
      * change PC to target
      * discard result of IF, which is PC + 4
      * discard result of ID, which is jump
      */
    io.output.pcWrite := 3.U
    io.output.ifIDFlush := true.B
    // pc+4 gets flushed and never enters ex, j proceeds
  }

  //----------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------
  //bypassing
  //----------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------

  //----------------------------------------------------------------------------------------
  // ALU bypass and Branch Bypass
  //----------------------------------------------------------------------------------------
  //TODO: how bypass play along with hazard detection:
  // on a lw -> add, the value gets bypassed AND NOT UPDATED, because it got stalled

  // this is the case where the register rs is the same as the register to write to in the alu output
  // the forward the alu output to substitute register rs
  // the second case is where rs is the same as dst register passed from memory out
  // note: search in the result directly from exe-mem first
  when((io.input.idEXRs === io.input.exMemRegDst) && io.input.exMemRegDst.orR && io.input.exMemRegWriteEnable) {
    io.output.forwardRs := 1.U
  }.elsewhen(
      (io.input.idEXRs === io.input.memWBRegDst) && io.input.memWBRegDst.orR && io.input.memWBRegWriteEnable
    ) {
      io.output.forwardRs := 2.U
    }
    .otherwise {
      io.output.forwardRs := 0.U
    }

  // note : search in the result directly from exe-mem first
  // you should also exclude register zero in the hazard detection unit
  when((io.input.idEXRt === io.input.exMemRegDst) && io.input.exMemRegDst.orR && io.input.exMemRegWriteEnable) {
    io.output.forwardRt := 1.U
  }.elsewhen(
      (io.input.idEXRt === io.input.memWBRegDst) && io.input.memWBRegDst.orR && io.input.memWBRegWriteEnable
    ) {
      io.output.forwardRt := 2.U
    }
    .otherwise {
      io.output.forwardRt := 0.U
    }

  //----------------------------------------------------------------------------------------
  // Memory bypass
  //----------------------------------------------------------------------------------------
  // lw $t1, 0($t0)
  // sw $t1, 0($t0)
  // or
  // add $t1, $t2, $t3
  // sw $t1, 0($t0)
  when(
    (io.input.exMemRegDst === io.input.memWBRegDst) && io.input.memWBRegDst.orR && io.input.memWBRegWriteEnable
  ) {
    io.output.forwardMemWriteData := true.B
  }.otherwise {
    io.output.forwardMemWriteData := false.B
  }

  //----------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------
  // exceptions
  //----------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------
  // exception has the highest priority over them.
  when(io.input.isException) {
    io.output.ifIDFlush := true.B
    io.output.idEXFlush := true.B
    io.output.exMemFlush := true.B
    io.output.memWBFlush := true.B
  }
}
