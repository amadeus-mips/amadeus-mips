// See README.md for license details.

package cpu.core.decode

import chisel3._
import chisel3.util.ListLookup
import cpu.core.Constants._

class SignalBundle extends Bundle {
  val instValid = Bool()
  val op1Type = UInt(2.W) // may change
  val op2Type = UInt(2.W) // ^
  val instType = UInt(instTypeLen.W)
  val operation = UInt(opLen.W)    // operation type
  val wr = Bool()     // write to register-file
  val wraType = UInt(2.W) // write register address type
  val immType = UInt(3.W)
}

class Control extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(addrLen.W))
    val out = Output(new SignalBundle)
  })

  // @formatter:off
  val csignals: List[UInt]= ListLookup(io.inst,
                   List(N , OP1_N  , OP2_N  , INST_N  , OP_N    , WR_N  , WRA_X  , IMM_N  ),
    Array(         /* val  | Op1    | Op2    | inst    |operation| Write | WReg   | Imm */
                   /* inst | sel    | sel    | type    | type    | reg   | Target | type */
      // 位操作
      OR        -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_OR  , WR_Y  , WRA_T1 , IMM_N  ),
      AND       -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_AND , WR_Y  , WRA_T1 , IMM_N  ),
      XOR       -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_XOR , WR_Y  , WRA_T1 , IMM_N  ),
      NOR       -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_NOR , WR_Y  , WRA_T1 , IMM_N  ),
      // 立即数
      ORI       -> List(Y , OP1_RS , OP2_IMM, INST_ALU, ALU_OR  , WR_Y  , WRA_T2 , IMM_LZE),
      ANDI      -> List(Y , OP1_RS , OP2_IMM, INST_ALU, ALU_AND , WR_Y  , WRA_T2 , IMM_LZE),
      XORI      -> List(Y , OP1_RS , OP2_IMM, INST_ALU, ALU_XOR , WR_Y  , WRA_T2 , IMM_LZE),
      LUI       -> List(Y , OP1_RS , OP2_IMM, INST_ALU, ALU_OR  , WR_Y  , WRA_T2 , IMM_HZE),

      // 移位
      SLLV      -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_SLL , WR_Y  , WRA_T1 , IMM_N  ),
      SRLV      -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_SRL , WR_Y  , WRA_T1 , IMM_N  ),
      SRAV      -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_SRL , WR_Y  , WRA_T1 , IMM_N  ),
      SLL       -> List(Y , OP1_IMM, OP2_RS , INST_ALU, ALU_SLL , WR_Y  , WRA_T1 , IMM_SHT),
      SRL       -> List(Y , OP1_IMM, OP2_RS , INST_ALU, ALU_SRL , WR_Y  , WRA_T1 , IMM_SHT),
      SRA       -> List(Y , OP1_IMM, OP2_RS , INST_ALU, ALU_SRA , WR_Y  , WRA_T1 , IMM_SHT),

      // HI，LO的Move指令
      MFHI      -> List(Y , OP1_N  , OP2_N  , INST_MV , MV_MFHI , WR_Y  , WRA_T1 , IMM_N  ),
      MFLO      -> List(Y , OP1_N  , OP2_N  , INST_MV , MV_MFLO , WR_Y  , WRA_T1 , IMM_N  ),
      MTHI      -> List(Y , OP1_RS , OP2_N  , INST_WO , WO_MTHI , WR_N  , WRA_X  , IMM_N  ),
      MTLO      -> List(Y , OP1_RS , OP2_N  , INST_WO , WO_MTLO , WR_N  , WRA_X  , IMM_N  ),
      // C0的Move指令
      MFC0      -> List(Y , OP1_N  , OP2_N  , INST_MV , MV_MFC0 , WR_Y  , WRA_T2 , IMM_N  ),
      MTC0      -> List(Y , OP1_N  , OP2_RS , INST_WO , WO_MTC0 , WR_N  , WRA_X  , IMM_N  ),

      // 比较指令
      SLT       -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_SLT , WR_Y  , WRA_T1 , IMM_N  ),
      SLTU      -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_SLTU, WR_Y  , WRA_T1 , IMM_N  ),
      // 立即数
      SLTI      -> List(Y , OP1_RS , OP2_IMM, INST_ALU, ALU_SLT , WR_Y  , WRA_T2 , IMM_LSE),
      SLTIU     -> List(Y , OP1_RS , OP2_IMM, INST_ALU, ALU_SLTU, WR_Y  , WRA_T2 , IMM_LSE),

      // 算术指令
      ADD       -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_ADD , WR_Y  , WRA_T1 , IMM_N  ),
      ADDU      -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_ADDU, WR_Y  , WRA_T1 , IMM_N  ),
      SUB       -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_SUB , WR_Y  , WRA_T1 , IMM_N  ),
      SUBU      -> List(Y , OP1_RS , OP2_RS , INST_ALU, ALU_SUBU, WR_Y  , WRA_T1 , IMM_N  ),
      MULT      -> List(Y , OP1_RS , OP2_RS , INST_WO , WO_MULT , WR_N  , WRA_X  , IMM_N  ),
      MULTU     -> List(Y , OP1_RS , OP2_RS , INST_WO , WO_MULTU, WR_N  , WRA_X  , IMM_N  ),
      DIV       -> List(Y , OP1_RS , OP2_RS , INST_WO , WO_DIV  , WR_N  , WRA_X  , IMM_N  ),
      DIVU      -> List(Y , OP1_RS , OP2_RS , INST_WO , WO_DIVU , WR_N  , WRA_X  , IMM_N  ),
      // 立即数
      ADDI      -> List(Y , OP1_RS , OP2_IMM, INST_ALU, ALU_ADD , WR_Y  , WRA_T2 , IMM_LSE),
      ADDIU     -> List(Y , OP1_RS , OP2_IMM, INST_ALU, ALU_ADDU, WR_Y  , WRA_T2 , IMM_LSE),

      // 跳转指令
      J         -> List(Y , OP1_N  , OP2_N  , INST_BR , BR_J    , WR_N  , WRA_X  , IMM_N  ),
      JAL       -> List(Y , OP1_N  , OP2_N  , INST_BR , BR_JAL  , WR_Y  , WRA_T3 , IMM_N  ),
      JR        -> List(Y , OP1_RS , OP2_N  , INST_BR , BR_JR   , WR_N  , WRA_X  , IMM_N  ),
      JALR      -> List(Y , OP1_RS , OP2_N  , INST_BR , BR_JALR , WR_Y  , WRA_T1 , IMM_N  ),
      BEQ       -> List(Y , OP1_RS , OP2_RS , INST_BR , BR_EQ   , WR_N  , WRA_X  , IMM_N  ),
      BNE       -> List(Y , OP1_RS , OP2_RS , INST_BR , BR_NE   , WR_N  , WRA_X  , IMM_N  ),
      BGTZ      -> List(Y , OP1_RS , OP2_N  , INST_BR , BR_GTZ  , WR_N  , WRA_X  , IMM_N  ),
      BLEZ      -> List(Y , OP1_RS , OP2_N  , INST_BR , BR_LEZ  , WR_N  , WRA_X  , IMM_N  ),
      BGEZ      -> List(Y , OP1_RS , OP2_N  , INST_BR , BR_GEZ  , WR_N  , WRA_X  , IMM_N  ),
      BGEZAL    -> List(Y , OP1_RS , OP2_N  , INST_BR , BR_GEZAL, WR_Y  , WRA_T3 , IMM_N  ),
      BLTZ      -> List(Y , OP1_RS , OP2_N  , INST_BR , BR_LTZ  , WR_N  , WRA_X  , IMM_N  ),
      BLTZAL    -> List(Y , OP1_RS , OP2_N  , INST_BR , BR_LTZAL, WR_Y  , WRA_T3 , IMM_N  ),

      // 例外指令
      SYSCALL   -> List(Y , OP1_N  , OP2_N  , INST_EXC, EXC_SC  , WR_N  , WRA_X  , IMM_N  ),
      BREAK     -> List(Y , OP1_N  , OP2_N  , INST_EXC, EXC_BR  , WR_N  , WRA_X  , IMM_N  ),
      ERET      -> List(Y , OP1_N  , OP2_N  , INST_EXC, EXC_ER  , WR_N  , WRA_X  , IMM_N  ),

      // 访存指令
      LB        -> List(Y , OP1_RS , OP2_N  , INST_MEM, MEM_LB  , WR_Y  , WRA_T2 , IMM_LSE),
      LBU       -> List(Y , OP1_RS , OP2_N  , INST_MEM, MEM_LBU , WR_Y  , WRA_T2 , IMM_LSE),
      LH        -> List(Y , OP1_RS , OP2_N  , INST_MEM, MEM_LH  , WR_Y  , WRA_T2 , IMM_LSE),
      LHU       -> List(Y , OP1_RS , OP2_N  , INST_MEM, MEM_LHU , WR_Y  , WRA_T2 , IMM_LSE),
      LW        -> List(Y , OP1_RS , OP2_N  , INST_MEM, MEM_LW  , WR_Y  , WRA_T2 , IMM_LSE),
      SB        -> List(Y , OP1_RS , OP2_RS , INST_MEM, MEM_SB  , WR_N  , WRA_X  , IMM_LSE),
      SH        -> List(Y , OP1_RS , OP2_RS , INST_MEM, MEM_SH  , WR_N  , WRA_X  , IMM_LSE),
      SW        -> List(Y , OP1_RS , OP2_RS , INST_MEM, MEM_SW  , WR_N  , WRA_X  , IMM_LSE),
    )
  )
  // @formatter:on

  /** for easy modification */
  val (csInstValid: Bool) :: csOP1Type :: csOP2Type :: csInstType :: cs0 = csignals
  val csOpType :: (csWR: Bool) :: csWRType :: csIMMType :: Nil = cs0

  io.out.instValid := csInstValid
  io.out.op1Type := csOP1Type
  io.out.op2Type := csOP2Type
  io.out.instType := csInstType
  io.out.operation := csOpType
  io.out.wr := csWR
  io.out.wraType := csWRType
  io.out.immType := csIMMType
}
