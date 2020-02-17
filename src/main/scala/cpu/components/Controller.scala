package cpu.components

import chisel3._
import chisel3.util._
import cpu.components.ControlSignalConstants._
import cpu.components.Instructions._

  class ControllerOutIO extends Bundle(){
    // whether the PC takes the branch
    // true is take the branch, false is don't take the branch
    val pcIsBranch= Output(Bool())
    // whether the PC takes the jump
    // true is take the jump, false is don't take the jump
    // case when isJump and isBranch is undefined, should not happen
    val pcIsJump = Output(Bool())
    // which is the dst register, rd or rt
    // false is rt, true is rd
    val dstRegSelect = Output(Bool())
    // whether write to the regfile
    // true is write back, false is don't write back
    val wbEnable = Output(Bool())
    // select the operand B
    // true is Reg(rt), false is sign extended offset(16bit)
    val opBSelect = Output(Bool())
    // what is the ALU OP
    val aluOp = Output(UInt(4.W))
    // memory mask mode
    val memMask = Output(UInt(2.W))
    // memory sign extension
    val memSext = Output(Bool())
    // enable write to data memory
    val memWriteEnable = Output(Bool())
    // select write back from read mem and alu output
    // if true, select from alu; if false, select from memory
    val wbSelect = Output(Bool())
  }

  class ControllerInIO extends Bundle() {
    val instr = Input(UInt(32.W))
  }


  class Controller extends Module {
    val io = IO(new Bundle() {
      val input = new ControllerInIO
      val output = new ControllerOutIO
    })
    // intentional dontcare to connect in other modules
    io := DontCare

    val controlSignals =
      ListLookup(io.input.instr,
        // if mem read is enabled, then there is no point reading from alu
        // in other words, memReadEnable = WB MemRead
        //TODO: introduce don't care symbols
                   List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,    OPBRT,       ALU_NOP,      MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
        Array(           //branch,  jump,   dst reg, wb enabl    func, mem   MEM_MASK_WORD,  MEM_SEXT_N,      write enable,
          NOP  ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,    OPBRT,       ALU_NOP,      MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          ADD  ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_ADD,      MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          // ADD rd, rs, rt
          SUB  ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_SUB,      MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          // sub rd, rs, rt
          AND  ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_AND,      MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          // AND rd, rs, rt
          ADDI ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,      MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          // addi rt, rs, immediate
          BEQ  ->  List(BRANCH_Y,    JUMP_N,   DSTRD,   WB_N,    OPBRT,       ALU_CMP_EQ,   MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          //beq rs, rt, offset
          J    ->  List(BRANCH_N,    JUMP_Y,   DSTRD,   WB_N,    OPBRT,       ALU_NOP,      MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          // J immediate
          LW   ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,      MEM_MASK_WORD,  MEM_SEXT_Y,     MEM_WRITE_N,   WB_MEM),
          // lw rt, rs, immediate
          SW   ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,    OPBOFFSET,   ALU_ADD,      MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_Y,   WB_ALU),
          // sw rt, rs, immediate
          LH   ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,      MEM_MASK_HALF,  MEM_SEXT_Y,     MEM_WRITE_N,   WB_MEM),
          // lh rt, rs, immediate
          LHU   ->  List(BRANCH_N,   JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,      MEM_MASK_HALF,  MEM_SEXT_N,     MEM_WRITE_N,   WB_MEM),
          // lhu rt, rs, immediate
          SH   ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,    OPBOFFSET,   ALU_ADD,      MEM_MASK_HALF,  MEM_SEXT_N,     MEM_WRITE_Y,   WB_ALU),
          // sh rt, rs, immediate
          LB   ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,      MEM_MASK_BYTE,  MEM_SEXT_Y,     MEM_WRITE_N,   WB_MEM),
          // lb rt, rs, immediate
          LBU   ->  List(BRANCH_N,   JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,      MEM_MASK_BYTE,  MEM_SEXT_N,     MEM_WRITE_N,   WB_MEM),
          // lbu rt, rs, immediate
          SB   ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,    OPBOFFSET,   ALU_ADD,      MEM_MASK_BYTE,  MEM_SEXT_N,     MEM_WRITE_Y,   WB_ALU)
          // sb rt, rs ,immediate
        ))

    val (csPcIsBranch: Bool) :: (csPcIsJump: Bool) :: (csDstRegSelect:Bool):: (csWBEnable:Bool) :: (csOpBSelect : Bool) :: (csAluOp : UInt) :: (csMemMask : UInt)::(csMemSext:Bool)::(csMemWriteEnable:Bool):: (csWBSelect: Bool) :: Nil = controlSignals
    // branch logic

    io.output.pcIsJump := csPcIsJump
    io.output.pcIsBranch := csPcIsBranch
    io.output.dstRegSelect := csDstRegSelect
    io.output.wbEnable := csWBEnable
    io.output.opBSelect := csOpBSelect
    io.output.aluOp := csAluOp
    io.output.memMask := csMemMask
    io.output.memSext := csMemSext
    io.output.memWriteEnable := csMemWriteEnable
    io.output.wbSelect := csWBSelect

  }
