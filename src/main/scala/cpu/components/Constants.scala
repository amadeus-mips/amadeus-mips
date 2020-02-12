package cpu.components
{

  import chisel3._
  import chisel3.util.Enum

  // Not used
  object MIPSConstants {
    // opcode in a instruction is up to 31 bit and down to 26 bit

    // R, I type rs register
    val RS_MSB = 25
    val RS_LSB = 21

    // R, I type rt register
    val RT_MSB = 20
    val RT_LSB = 16

    // R type rd register
    val RD_MSB = 15
    val RD_LSB = 11

    // R type shamt
    val SHAMT_MSB = 10
    val SHAMT_LSB = 6

    // R type funct
    val FUNCT_MSB = 5
    val FUNCT_LSB = 0

    // address in J-type and immediate in I-type are note specified
  }

  // this is originally a trait. Don't know why it doesn't work
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
    val ALU_CMP_EQ = 4.U
    val ALU_CMP_N_EQ = 5.U
    val ALU_CMP_GREATER_Z = 6.U
    val ALU_CMP_GREATER_EQ_Z = 7.U
    val ALU_CMP_LESS_Z = 8.U
    val ALU_CMP_LESS_EQ_Z = 9.U
    val ALU_PASSTHROUGH = 10.U


    // whether write to mem
    val MEM_WRITE_Y = true.B
    val MEM_WRITE_N = false.B

    // chooose the writeback select
    val WB_MEM = true.B
    val WB_ALU = false.B
  }

}
