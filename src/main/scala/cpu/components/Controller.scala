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
    val aluOp = Output(UInt(5.W))
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
                      List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,    OPBRT,       ALU_NOP,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
        Array(           //branch,  jump,   dst reg, wb enabl    func, mem   MEM_MASK_WORD,  ME            M_SEXT_N,      write enable,
          NOP     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,    OPBRT,       ALU_NOP,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          ADD     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_ADD,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          ADDU    ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_ADD,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          ADDI    ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          ADDIU   ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SUB     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_SUB,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SUBU    ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_SUB,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SLT     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_SLT,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SLTU    ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_SLT,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SLTI    ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_SLT,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SLTIU   ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_SLT,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          AND     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_AND,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          ANDI    ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_AND,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          LUI     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_LUI,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          NOR     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_NOR,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          OR      ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_OR,                   MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          ORI     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_OR,                   MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          XOR     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBRT,       ALU_XOR,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          XORI    ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_XOR,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SLL     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_SLI,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SLLV    ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_SLV,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SRL     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_SRLI,                 MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SRLV    ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_SRLV,                 MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SRA     ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_SRAI,                 MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          SRAV    ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_Y,    OPBOFFSET,   ALU_SRAV,                 MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          BEQ     ->  List(BRANCH_Y,    JUMP_N,   DSTRD,   WB_N,    OPBRT,       ALU_CMP_EQ,               MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          BNE     ->  List(BRANCH_Y,    JUMP_N,   DSTRD,   WB_N,    OPBRT,       ALU_CMP_N_EQ,             MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          BGEZ    ->  List(BRANCH_Y,    JUMP_N,   DSTRD,   WB_N,    OPBRT,       ALU_CMP_GREATER_EQ_Z,     MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          BGTZ    ->  List(BRANCH_Y,    JUMP_N,   DSTRD,   WB_N,    OPBRT,       ALU_CMP_GREATER_Z,        MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          BLEZ    ->  List(BRANCH_Y,    JUMP_N,   DSTRD,   WB_N,    OPBRT,       ALU_CMP_LESS_EQ_Z,        MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          BLTZ    ->  List(BRANCH_Y,    JUMP_N,   DSTRD,   WB_N,    OPBRT,       ALU_CMP_LESS_EQ_Z,        MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          J       ->  List(BRANCH_N,    JUMP_Y,   DSTRD,   WB_N,    OPBRT,       ALU_NOP,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
          LW      ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,                  MEM_MASK_WORD,  MEM_SEXT_Y,     MEM_WRITE_N,   WB_MEM),
          SW      ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,    OPBOFFSET,   ALU_ADD,                  MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_Y,   WB_ALU),
          LH      ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,                  MEM_MASK_HALF,  MEM_SEXT_Y,     MEM_WRITE_N,   WB_MEM),
          LHU     ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,                  MEM_MASK_HALF,  MEM_SEXT_N,     MEM_WRITE_N,   WB_MEM),
          SH      ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,    OPBOFFSET,   ALU_ADD,                  MEM_MASK_HALF,  MEM_SEXT_N,     MEM_WRITE_Y,   WB_ALU),
          LB      ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,                  MEM_MASK_BYTE,  MEM_SEXT_Y,     MEM_WRITE_N,   WB_MEM),
          LBU     ->  List(BRANCH_N,    JUMP_N,   DSTRT,   WB_Y,    OPBOFFSET,   ALU_ADD,                  MEM_MASK_BYTE,  MEM_SEXT_N,     MEM_WRITE_N,   WB_MEM),
          SB      ->  List(BRANCH_N,    JUMP_N,   DSTRD,   WB_N,    OPBOFFSET,   ALU_ADD,                  MEM_MASK_BYTE,  MEM_SEXT_N,     MEM_WRITE_Y,   WB_ALU)
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
