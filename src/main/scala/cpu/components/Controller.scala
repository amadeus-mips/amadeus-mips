package cpu.components

import chisel3._
import chisel3.util._
import cpu.components.ControlSignalConstants._
import cpu.components.Instructions._

  class ControllerOutIO extends Bundle(){
    // whether the PC takes the branch
    // true is take the branch, false is don't take the branch
    val PC_isBranch = Output(Bool())
    // whether the PC takes the jump
    // true is take the jump, false is don't take the jump
    // case when isJump and isBranch is undefined, should not happen
    val PC_isJump = Output(Bool())
    // which is the dst register, rd or rt
    // false is rt, true is rd
    val DstRegSelect = Output(Bool())
    // whether write to the regfile
    // true is write back, false is don't write back
    val WBEnable = Output(Bool())
    // select the operand B
    // true is Reg(rt), false is sign extended offset(16bit)
    val OpBSelect = Output(Bool())
    // what is the ALU OP
    val AluOp = Output(UInt(4.W))
    // enable write to data memory
    val MemWriteEnable = Output(Bool())
    // select write back from read mem and alu output
    // if true, select from alu; if false, select from memory
    val WBSelect = Output(Bool())
  }

  class ControllerInIO extends Bundle() {
    val instr = Input(UInt(32.W))
    val alu_branch_take = Input(Bool())
  }


  class Controller extends Module {
    val io = IO(new Bundle() {
      val input = new ControllerInIO
      val output = new ControllerOutIO
    })
    io := DontCare

    val controlSignals =
      ListLookup(io.input.instr,
        // if mem read is enabled, then there is no point reading from alu
        // in other words, memReadEnable = WB MemRead
                   List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,     OPBRT,       ALU_NOP,           MEM_WRITE_N,   WB_ALU),
        Array(           //branch,  jump,   dst reg, wb enable,ALU op b, A    LU func, mem         write enable,
          NOP  ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,     OPBRT,       ALU_NOP,           MEM_WRITE_N,   WB_ALU),
          ADD  ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,     OPBRT,       ALU_ADD,           MEM_WRITE_N,   WB_ALU),
          // ADD rd, rs, rt
          SUB  ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,     OPBRT,       ALU_SUB,           MEM_WRITE_N,   WB_ALU),
          // sub rd, rs, rt
          AND  ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,     OPBRT,       ALU_AND,           MEM_WRITE_N,   WB_ALU),
          // AND rd, rs, rt
          ADDI ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,     OPBOFFSET,   ALU_ADD,           MEM_WRITE_N,   WB_ALU),
          // addi rt, rs, immediate
          BEQ  ->  List(BRANCH_Y,    JUMP_N,   DSTRD,   WB_N,     OPBRT,       ALU_CMP_EQ,        MEM_WRITE_N,   WB_ALU),
          //beq rs, rt, offset
          J    ->  List(BRANCH_N,    JUMP_Y,   DSTRD,   WB_N,     OPBRT,       ALU_NOP,           MEM_WRITE_N,   WB_ALU),
          // J immediate
          LW   ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,     OPBOFFSET,   ALU_ADD,           MEM_WRITE_N,   WB_MEM),
          // lw rt, rs, immediate
          SW   ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,     OPBRT,       ALU_NOP,           MEM_WRITE_Y,   WB_ALU)
          // sw rt, rs, immediate
        ))

    val (cs_PC_isBranch: Bool) :: (cs_PC_isJump: Bool) :: (cs_DstRegSelect:Bool) :: (cs_WBEnable: Bool) :: (cs_OpBSelect: Bool) :: (cs_AluOp: UInt) :: (cs_MemWriteEnable: Bool) :: (cs_WBSelect: Bool) :: Nil = controlSignals
    // branch logic
    val control_PC_isBranch = Mux((cs_PC_isBranch && io.input.alu_branch_take), BRANCH_Y, BRANCH_N)

    io.output.PC_isJump := cs_PC_isJump
    io.output.PC_isBranch := control_PC_isBranch
    io.output.DstRegSelect := cs_DstRegSelect
    io.output.WBEnable := cs_WBEnable
    io.output.OpBSelect := cs_OpBSelect
    io.output.AluOp := cs_AluOp
    io.output.MemWriteEnable := cs_MemWriteEnable
    io.output.WBSelect := cs_WBSelect

  }
