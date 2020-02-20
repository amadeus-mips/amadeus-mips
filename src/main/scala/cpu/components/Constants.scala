package cpu.components {

  import chisel3._

  object ControlSignalConstants {

    // general yes and no
    val Y = true.B
    val N = false.B

    // whether branch
    val BRANCH_Y = true.B
    val BRANCH_N = false.B

    // whether jump
    val JUMP_Y = true.B
    val JUMP_N = false.B

    val JUMP_ADDR = true.B
    val JUMP_REG = false.B
    val JUMP_X = false.B

    // choose destination register
    val DST_X = 0.U
    val DST_RT = 0.U
    val DST_RD = 1.U
    // for reg 31
    val DST_L = 2.U

    //whether write back or not
    val WB_Y = true.B
    val WB_N = false.B

    // choose the operand B for ALU
    val OPB_OFFSET = 0.U
    val OPB_RT = 1.U
    val OPB_PCPLUSFOUR = 2.U
    val OPB_X = 0.U

    // choose the ALU Op
    val ALU_NOP = 0.U
    val ALU_ADD = 1.U
    val ALU_SUB = 2.U
    val ALU_AND = 3.U
    val ALU_SLT = 4.U
    val ALU_LUI = 5.U
    val ALU_NOR = 6.U
    val ALU_OR = 7.U
    val ALU_XOR = 8.U
    val ALU_SLV = 9.U
    val ALU_SLI = 10.U
    val ALU_SRLV = 11.U
    val ALU_SRLI = 12.U
    val ALU_SRAV = 13.U
    val ALU_SRAI = 14.U
    val ALU_PLUS_FOUR = 15.U

    // branch ops
    val BRANCH_X = 0.U
    val BRANCH_CMP_EQ = 0.U
    val BRANCH_CMP_N_EQ = 1.U
    val BRANCH_CMP_GREATER_Z = 2.U
    val BRANCH_CMP_GREATER_EQ_Z = 3.U
    val BRANCH_CMP_LESS_Z = 4.U
    val BRANCH_CMP_LESS_EQ_Z = 5.U

    // whether write to mem
    val MEM_WRITE_Y = true.B
    val MEM_WRITE_N = false.B

    // memory mask mode
    val MEM_MASK_X = 0.U
    val MEM_MASK_WORD = 2.U
    val MEM_MASK_HALF = 1.U
    val MEM_MASK_BYTE = 0.U

    // memory sign extension mode
    val MEM_SEXT_X = false.B
    val MEM_SEXT_Y = true.B
    val MEM_SEXT_N = false.B

    // chooose the writeback select
    val WB_X = false.B
    val WB_MEM = true.B
    val WB_ALU = false.B
  }

}
