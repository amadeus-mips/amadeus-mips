package cpu.components {

  import chisel3._

  // TODO: this is originally a trait. Don't know why it doesn't work
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

    // choose destination register
    val DSTRD = true.B
    val DSTRT = false.B

    //whether write back or not
    val WB_Y = true.B
    val WB_N = false.B

    // choose the operand B for ALU
    val OPBRT = false.B
    val OPBOFFSET = true.B

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
    //TODO: put branching comparison to ID, and
    // compare with 2's complement
    val ALU_CMP_EQ = 15.U
    val ALU_CMP_N_EQ = 16.U
    val ALU_CMP_GREATER_Z = 17.U
    val ALU_CMP_GREATER_EQ_Z = 18.U
    val ALU_CMP_LESS_Z = 19.U
    val ALU_CMP_LESS_EQ_Z = 20.U

    // whether write to mem
    val MEM_WRITE_Y = true.B
    val MEM_WRITE_N = false.B

    // memory mask mode
    val MEM_MASK_WORD = 2.U
    val MEM_MASK_HALF = 1.U
    val MEM_MASK_BYTE = 0.U

    // memory sign extension mode
    val MEM_SEXT_Y = true.B
    val MEM_SEXT_N = false.B

    // chooose the writeback select
    val WB_MEM = true.B
    val WB_ALU = false.B
  }

}
