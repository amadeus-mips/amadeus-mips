package cpu.components
{

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
    val OPBRT = true.B
    val OPBOFFSET = false.B

    // choose the ALU Op
    val ALU_NOP = 0.U
    val ALU_ADD = 1.U
    val ALU_SUB = 2.U
    val ALU_AND = 3.U

    // the branch ops
    val BRANCH_CMP_X = 0.U
    val BRANCH_CMP_EQ = 1.U
    val BRANCH_CMP_N_EQ = 2.U
    val BRANCH_CMP_GREATER_Z = 3.U
    val BRANCH_CMP_GREATER_EQ_Z = 4.U
    val BRANCH_CMP_LESS_Z = 5.U
    val BRANCH_CMP_LESS_EQ_Z = 6.U

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
