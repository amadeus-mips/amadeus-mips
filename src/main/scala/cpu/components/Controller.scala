package cpu.components

import chisel3._
import chisel3.util._
import cpu.components.ControlSignalConstants._
import cpu.components.Instructions._

  class ControllerOutIO extends Bundle(){
    // whether the PC takes the branch
    // true is take the branch, false is don't take the branch
    val isBranch= Output(Bool())
    // whether the PC takes the jump
    // true is take the jump, false is don't take the jump
    // case when isJump and isBranch is undefined, should not happen
    val isJump = Output(Bool())
    // where does it jump to, the address immediate or the rs
    // which is the dst register, rd or rt
    // false is rt, true is rd
    val dstRegSelect = Output(UInt(2.W))
    // whether write to the regfile
    // true is write back, false is don't write back
    val wbEnable = Output(Bool())
    // select the operand B
    // true is Reg(rt), false is sign extended offset(16bit)
    val opBSelect = Output(UInt(2.W))
    // what is the ALU OP
    val aluOp = Output(UInt(5.W))

    val branchOp = Output(UInt(3.W))

    val jumpOp = Output(Bool())

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
                      List(BRANCH_N,    JUMP_N,       DST_RD,   WB_N,    OPB_RT,          ALU_NOP,         BRANCH_CMP_EQ,               JUMP_REG,       MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_N,   WB_ALU),
        Array(           //branch,  jump,   dst     reg, wb enabl    func, _mem   MEM_   MASK_WORD,      BRANCH_CMP_EQ,               JUMP_REG,       MEM_SEXT_N,      write enable,
          NOP     ->  List(BRANCH_N,    JUMP_N,       DST_X,    WB_N,    OPB_RT,          ALU_NOP,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          ADD     ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_ADD,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          ADDU    ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_ADDU,        BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          ADDI    ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_ADDU,        BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          ADDIU   ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_ADDU,        BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SUB     ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_SUB,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SUBU    ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_SUBU,        BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SLT     ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_SLT,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SLTU    ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_SLT,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SLTI    ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_SLT,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SLTIU   ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_SLT,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          AND     ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_AND,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          ANDI    ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_AND,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          LUI     ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_LUI,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          NOR     ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_NOR,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          OR      ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_OR,          BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          ORI     ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_OR,          BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          XOR     ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_XOR,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          XORI    ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_XOR,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SLL     ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_SLI,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SLLV    ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_SLV,         BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SRL     ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_SRLI,        BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SRLV    ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_SRLV,        BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SRA     ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_SRAI,        BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          SRAV    ->  List(BRANCH_N,    JUMP_N,       DST_RD,   WB_Y,    OPB_RT,          ALU_SRAV,        BRANCH_X,                    JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          BEQ     ->  List(BRANCH_Y,    JUMP_N,       DST_X,    WB_N,    OPB_RT,          ALU_ADDU,        BRANCH_CMP_EQ,               JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          BNE     ->  List(BRANCH_Y,    JUMP_N,       DST_X,    WB_N,    OPB_RT,          ALU_ADDU,        BRANCH_CMP_N_EQ,             JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          BGEZ    ->  List(BRANCH_Y,    JUMP_N,       DST_X,    WB_N,    OPB_RT,          ALU_ADDU,        BRANCH_CMP_GREATER_EQ_Z,     JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          BGTZ    ->  List(BRANCH_Y,    JUMP_N,       DST_X,    WB_N,    OPB_RT,          ALU_ADDU,        BRANCH_CMP_GREATER_Z,        JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          BLEZ    ->  List(BRANCH_Y,    JUMP_N,       DST_X,    WB_N,    OPB_RT,          ALU_ADDU,        BRANCH_CMP_LESS_EQ_Z,        JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          BLTZ    ->  List(BRANCH_Y,    JUMP_N,       DST_X,    WB_N,    OPB_RT,          ALU_ADDU,        BRANCH_CMP_LESS_Z,           JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          BGEZAL  ->  List(BRANCH_Y,    JUMP_N,       DST_L,    WB_Y,    OPB_PCPLUSFOUR,  ALU_ADDU,        BRANCH_CMP_GREATER_EQ_Z,     JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          BLTZAL  ->  List(BRANCH_Y,    JUMP_N,       DST_L,    WB_Y,    OPB_PCPLUSFOUR,  ALU_ADDU,        BRANCH_CMP_LESS_Z,           JUMP_X,         MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          J       ->  List(BRANCH_N,    JUMP_Y,       DST_X,    WB_N,    OPB_RT,          ALU_NOP,         BRANCH_CMP_EQ,               JUMP_ADDR,      MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          JAL     ->  List(BRANCH_N,    JUMP_Y,       DST_L,    WB_Y,    OPB_PCPLUSFOUR,  ALU_PLUS_FOUR,   BRANCH_CMP_EQ,               JUMP_ADDR,      MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          JR      ->  List(BRANCH_N,    JUMP_Y,       DST_RD,   WB_N,    OPB_RT,          ALU_NOP,         BRANCH_CMP_EQ,               JUMP_REG,       MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          JALR    ->  List(BRANCH_N,    JUMP_Y,       DST_RD,   WB_Y,    OPB_PCPLUSFOUR,  ALU_PLUS_FOUR,   BRANCH_CMP_EQ,               JUMP_REG,       MEM_MASK_X,     MEM_SEXT_X,     MEM_WRITE_N,   WB_ALU),
          LW      ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_ADDU,        BRANCH_CMP_EQ,               JUMP_X,         MEM_MASK_WORD,  MEM_SEXT_Y,     MEM_WRITE_N,   WB_MEM),
          // notice: select rt as write back for bypassing
          SW      ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_N,    OPB_OFFSET,      ALU_ADDU,        BRANCH_CMP_EQ,               JUMP_X,         MEM_MASK_WORD,  MEM_SEXT_N,     MEM_WRITE_Y,   WB_ALU),
          LH      ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_ADDU,        BRANCH_CMP_EQ,               JUMP_X,         MEM_MASK_HALF,  MEM_SEXT_Y,     MEM_WRITE_N,   WB_MEM),
          LHU     ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_ADDU,        BRANCH_CMP_EQ,               JUMP_X,         MEM_MASK_HALF,  MEM_SEXT_N,     MEM_WRITE_N,   WB_MEM),
          SH      ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_N,    OPB_OFFSET,      ALU_ADDU,        BRANCH_CMP_EQ,               JUMP_X,         MEM_MASK_HALF,  MEM_SEXT_N,     MEM_WRITE_Y,   WB_ALU),
          LB      ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_ADDU,        BRANCH_CMP_EQ,               JUMP_X,         MEM_MASK_BYTE,  MEM_SEXT_Y,     MEM_WRITE_N,   WB_MEM),
          LBU     ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_Y,    OPB_OFFSET,      ALU_ADDU,        BRANCH_CMP_EQ,               JUMP_X,         MEM_MASK_BYTE,  MEM_SEXT_N,     MEM_WRITE_N,   WB_MEM),
          SB      ->  List(BRANCH_N,    JUMP_N,       DST_RT,   WB_N,    OPB_OFFSET,      ALU_ADDU,        BRANCH_CMP_EQ,               JUMP_X,         MEM_MASK_BYTE,  MEM_SEXT_N,     MEM_WRITE_Y,   WB_ALU)
        ))

    val (csPcIsBranch: Bool) :: (csPcIsJump: Bool) :: (csDstRegSelect: UInt):: (csWBEnable:Bool) :: (csOpBSelect : UInt) :: (csAluOp : UInt) :: (csBranchOp: UInt) :: (csPcJumpTarget: Bool)  :: (csMemMask : UInt)::(csMemSext:Bool)::(csMemWriteEnable:Bool):: (csWBSelect: Bool) :: Nil = controlSignals
    // branch logic

    io.output.isJump := csPcIsJump
    io.output.isBranch := csPcIsBranch
    io.output.dstRegSelect := csDstRegSelect
    io.output.wbEnable := csWBEnable
    io.output.opBSelect := csOpBSelect
    io.output.aluOp := csAluOp
    io.output.branchOp := csBranchOp
    io.output.jumpOp := csPcJumpTarget
    io.output.memMask := csMemMask
    io.output.memSext := csMemSext
    io.output.memWriteEnable := csMemWriteEnable
    io.output.wbSelect := csWBSelect

  }
