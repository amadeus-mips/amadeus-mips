// See README.md for license details.

package cpu.core.decode

import chisel3._
import chisel3.util.ListLookup
import cpu.core.Constants._

class SignalBundle extends Bundle {
  val instValid = Bool()
  val op1Type   = UInt(1.W) // may change
  val op2Type   = UInt(1.W) // ^
  val instType  = UInt(instTypeLen.W)
  val operation = UInt(opLen.W) // operation type
  val wr        = Bool() // write to register-file
  val wraType   = UInt(2.W) // write register request type
  val immType   = UInt(3.W)
}

class Decode extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(addrLen.W))
    val out  = Output(new SignalBundle)
  })

  // @formatter:off
  val csignals: List[UInt]= ListLookup(io.inst,
                   List(N , OPn_X  , OPn_X  , INST_N  , OP_N    , WR_N  , WRA_X  , IMM_N  ),
    Array(         /* val  | Op1    | Op2    | inst    |operation| Write | WReg   | Imm */
                   /* inst | sel    | sel    | type    | type    | reg   | Target | type */
      // 位操作
      OR        -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_OR  , WR_Y  , WRA_T1 , IMM_N  ),
      AND       -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_AND , WR_Y  , WRA_T1 , IMM_N  ),
      XOR       -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_XOR , WR_Y  , WRA_T1 , IMM_N  ),
      NOR       -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_NOR , WR_Y  , WRA_T1 , IMM_N  ),
      // 立即数
      ORI       -> List(Y , OPn_RF , OPn_IMM, INST_ALU, ALU_OR  , WR_Y  , WRA_T2 , IMM_LZE),
      ANDI      -> List(Y , OPn_RF , OPn_IMM, INST_ALU, ALU_AND , WR_Y  , WRA_T2 , IMM_LZE),
      XORI      -> List(Y , OPn_RF , OPn_IMM, INST_ALU, ALU_XOR , WR_Y  , WRA_T2 , IMM_LZE),
      LUI       -> List(Y , OPn_RF , OPn_IMM, INST_ALU, ALU_OR  , WR_Y  , WRA_T2 , IMM_HZE),

      // 移位
      SLLV      -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_SLL , WR_Y  , WRA_T1 , IMM_N  ),
      SRLV      -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_SRL , WR_Y  , WRA_T1 , IMM_N  ),
      SRAV      -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_SRA , WR_Y  , WRA_T1 , IMM_N  ),
      SLL       -> List(Y , OPn_IMM, OPn_RF , INST_ALU, ALU_SLL , WR_Y  , WRA_T1 , IMM_SHT),
      SRL       -> List(Y , OPn_IMM, OPn_RF , INST_ALU, ALU_SRL , WR_Y  , WRA_T1 , IMM_SHT),
      SRA       -> List(Y , OPn_IMM, OPn_RF , INST_ALU, ALU_SRA , WR_Y  , WRA_T1 , IMM_SHT),

      // Move
      MOVN      -> List(Y , OPn_RF , OPn_RF , INST_MV , MV_MOVN , WR_Y  , WRA_T1 , IMM_N  ),
      MOVZ      -> List(Y , OPn_RF , OPn_RF , INST_MV , MV_MOVZ , WR_Y  , WRA_T1 , IMM_N  ),

      // HI，LO的Move指令
      MFHI      -> List(Y , OPn_X  , OPn_X  , INST_MV , MV_MFHI , WR_Y  , WRA_T1 , IMM_N  ),
      MFLO      -> List(Y , OPn_X  , OPn_X  , INST_MV , MV_MFLO , WR_Y  , WRA_T1 , IMM_N  ),
      MTHI      -> List(Y , OPn_RF , OPn_X  , INST_WO , WO_MTHI , WR_N  , WRA_X  , IMM_N  ),
      MTLO      -> List(Y , OPn_RF , OPn_X  , INST_WO , WO_MTLO , WR_N  , WRA_X  , IMM_N  ),
      // C0的Move指令
      MFC0      -> List(Y , OPn_X  , OPn_X  , INST_MV , MV_MFC0 , WR_Y  , WRA_T2 , IMM_N  ),
      MTC0      -> List(Y , OPn_X  , OPn_RF , INST_WO , WO_MTC0 , WR_N  , WRA_X  , IMM_N  ),

      // 比较指令
      SLT       -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_SLT , WR_Y  , WRA_T1 , IMM_N  ),
      SLTU      -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_SLTU, WR_Y  , WRA_T1 , IMM_N  ),
      // 立即数
      SLTI      -> List(Y , OPn_RF , OPn_IMM, INST_ALU, ALU_SLT , WR_Y  , WRA_T2 , IMM_LSE),
      SLTIU     -> List(Y , OPn_RF , OPn_IMM, INST_ALU, ALU_SLTU, WR_Y  , WRA_T2 , IMM_LSE),

      // Trap
      TEQ       -> List(Y , OPn_RF , OPn_RF , INST_TRAP, TRAP_EQ, WR_N  , WRA_X  , IMM_N  ),
      TEQI      -> List(Y , OPn_RF , OPn_IMM, INST_TRAP, TRAP_EQ, WR_N  , WRA_X  , IMM_LSE),
      TGE       -> List(Y , OPn_RF , OPn_RF , INST_TRAP, TRAP_GE, WR_N  , WRA_X  , IMM_N  ),
      TGEI      -> List(Y , OPn_RF , OPn_IMM, INST_TRAP, TRAP_GE, WR_N  , WRA_X  , IMM_LSE),
      TGEIU     -> List(Y , OPn_RF , OPn_IMM, INST_TRAP, TRAP_GEU, WR_N , WRA_X  , IMM_LSE),
      TGEU      -> List(Y , OPn_RF , OPn_RF , INST_TRAP, TRAP_GEU, WR_N , WRA_X  , IMM_N  ),
      TLT       -> List(Y , OPn_RF , OPn_RF , INST_TRAP, TRAP_LT, WR_N  , WRA_X  , IMM_N  ),
      TLTI      -> List(Y , OPn_RF , OPn_IMM, INST_TRAP, TRAP_LT, WR_N  , WRA_X  , IMM_LSE),
      TLTIU     -> List(Y , OPn_RF , OPn_IMM, INST_TRAP, TRAP_LTU, WR_N , WRA_X  , IMM_LSE),
      TLTU      -> List(Y , OPn_RF , OPn_RF , INST_TRAP, TRAP_LTU, WR_N , WRA_X  , IMM_N  ),
      TNE       -> List(Y , OPn_RF , OPn_RF , INST_TRAP, TRAP_NE, WR_N  , WRA_X  , IMM_N  ),
      TNEI      -> List(Y , OPn_RF , OPn_IMM, INST_TRAP, TRAP_NE, WR_N  , WRA_X  , IMM_LSE),

      // 算术指令
      ADD       -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_ADD , WR_Y  , WRA_T1 , IMM_N  ),
      ADDU      -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_ADDU, WR_Y  , WRA_T1 , IMM_N  ),
      SUB       -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_SUB , WR_Y  , WRA_T1 , IMM_N  ),
      SUBU      -> List(Y , OPn_RF , OPn_RF , INST_ALU, ALU_SUBU, WR_Y  , WRA_T1 , IMM_N  ),
      MUL       -> List(Y , OPn_RF , OPn_RF,  INST_ALU, ALU_MUL , WR_Y  , WRA_T1 , IMM_N  ),
      MULT      -> List(Y , OPn_RF , OPn_RF , INST_WO , WO_MULT , WR_N  , WRA_X  , IMM_N  ),
      MULTU     -> List(Y , OPn_RF , OPn_RF , INST_WO , WO_MULTU, WR_N  , WRA_X  , IMM_N  ),
      MADD      -> List(Y , OPn_RF , OPn_RF , INST_WO , WO_MADD , WR_N  , WRA_X  , IMM_N  ),
      MADDU     -> List(Y , OPn_RF , OPn_RF , INST_WO , WO_MADDU, WR_N  , WRA_X  , IMM_N  ),
      MSUB      -> List(Y , OPn_RF , OPn_RF , INST_WO , WO_MSUB , WR_N  , WRA_X  , IMM_N  ),
      MSUBU     -> List(Y , OPn_RF , OPn_RF , INST_WO , WO_MSUBU, WR_N  , WRA_X  , IMM_N  ),
      DIV       -> List(Y , OPn_RF , OPn_RF , INST_WO , WO_DIV  , WR_N  , WRA_X  , IMM_N  ),
      DIVU      -> List(Y , OPn_RF , OPn_RF , INST_WO , WO_DIVU , WR_N  , WRA_X  , IMM_N  ),
      CLO       -> List(Y , OPn_RF , OPn_X  , INST_ALU, ALU_CLO , WR_Y  , WRA_T1 , IMM_N  ),
      CLZ       -> List(Y , OPn_RF , OPn_X  , INST_ALU, ALU_CLZ , WR_Y  , WRA_T1 , IMM_N  ),
      // 立即数
      ADDI      -> List(Y , OPn_RF , OPn_IMM, INST_ALU, ALU_ADD , WR_Y  , WRA_T2 , IMM_LSE),
      ADDIU     -> List(Y , OPn_RF , OPn_IMM, INST_ALU, ALU_ADDU, WR_Y  , WRA_T2 , IMM_LSE),


      // 跳转指令
      J         -> List(Y , OPn_X  , OPn_X  , INST_BR , BR_J    , WR_N  , WRA_X  , IMM_N  ),
      JAL       -> List(Y , OPn_X  , OPn_X  , INST_BR , BR_JAL  , WR_Y  , WRA_T3 , IMM_N  ),
      JR        -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_JR   , WR_N  , WRA_X  , IMM_N  ),
      JALR      -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_JALR , WR_Y  , WRA_T1 , IMM_N  ),
      BEQ       -> List(Y , OPn_RF , OPn_RF , INST_BR , BR_EQ   , WR_N  , WRA_X  , IMM_N  ),
      BNE       -> List(Y , OPn_RF , OPn_RF , INST_BR , BR_NE   , WR_N  , WRA_X  , IMM_N  ),
      BGTZ      -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_GTZ  , WR_N  , WRA_X  , IMM_N  ),
      BLEZ      -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_LEZ  , WR_N  , WRA_X  , IMM_N  ),
      BGEZ      -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_GEZ  , WR_N  , WRA_X  , IMM_N  ),
      BGEZAL    -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_GEZAL, WR_Y  , WRA_T3 , IMM_N  ),
      BLTZ      -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_LTZ  , WR_N  , WRA_X  , IMM_N  ),
      BLTZAL    -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_LTZAL, WR_Y  , WRA_T3 , IMM_N  ),
      BEQL      -> List(Y , OPn_RF , OPn_RF , INST_BR , BR_EQ   , WR_N  , WRA_X  , IMM_N  ),
      BNEL      -> List(Y , OPn_RF , OPn_RF , INST_BR , BR_NE   , WR_N  , WRA_X  , IMM_N  ),
      BGTZL     -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_GTZ  , WR_N  , WRA_X  , IMM_N  ),
      BLEZL     -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_LEZ  , WR_N  , WRA_X  , IMM_N  ),
      BGEZL     -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_GEZ  , WR_N  , WRA_X  , IMM_N  ),
      BGEZALL   -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_GEZAL, WR_Y  , WRA_T3 , IMM_N  ),
      BLTZL     -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_LTZ  , WR_N  , WRA_X  , IMM_N  ),
      BLTZALL   -> List(Y , OPn_RF , OPn_X  , INST_BR , BR_LTZAL, WR_Y  , WRA_T3 , IMM_N  ),

      // TLB
      TLBP      -> List(Y , OPn_X  , OPn_X  , INST_TLB, TLB_P   , WR_N  , WRA_X  , IMM_N  ),
      TLBR      -> List(Y , OPn_X  , OPn_X  , INST_TLB, TLB_R   , WR_N  , WRA_X  , IMM_N  ),
      TLBWI     -> List(Y , OPn_X  , OPn_X  , INST_TLB, TLB_WI  , WR_N  , WRA_X  , IMM_N  ),
      TLBWR     -> List(Y , OPn_X  , OPn_X  , INST_TLB, TLB_WR  , WR_N  , WRA_X  , IMM_N  ),

      // 例外指令
      SYSCALL   -> List(Y , OPn_X  , OPn_X  , INST_EXC, EXC_SC  , WR_N  , WRA_X  , IMM_N  ),
      BREAK     -> List(Y , OPn_X  , OPn_X  , INST_EXC, EXC_BR  , WR_N  , WRA_X  , IMM_N  ),
      ERET      -> List(Y , OPn_X  , OPn_X  , INST_EXC, EXC_ER  , WR_N  , WRA_X  , IMM_N  ),
      WAIT      -> List(Y , OPn_X  , OPn_X  , INST_EXC, EXC_WAIT, WR_N  , WRA_X  , IMM_N  ),

      // 访存指令
      LB        -> List(Y , OPn_RF , OPn_X  , INST_MEM, MEM_LB  , WR_Y  , WRA_T2 , IMM_N  ),
      LBU       -> List(Y , OPn_RF , OPn_X  , INST_MEM, MEM_LBU , WR_Y  , WRA_T2 , IMM_N  ),
      LH        -> List(Y , OPn_RF , OPn_X  , INST_MEM, MEM_LH  , WR_Y  , WRA_T2 , IMM_N  ),
      LHU       -> List(Y , OPn_RF , OPn_X  , INST_MEM, MEM_LHU , WR_Y  , WRA_T2 , IMM_N  ),
      LW        -> List(Y , OPn_RF , OPn_X  , INST_MEM, MEM_LW  , WR_Y  , WRA_T2 , IMM_N  ),
      SB        -> List(Y , OPn_RF , OPn_RF , INST_MEM, MEM_SB  , WR_N  , WRA_X  , IMM_N  ),
      SH        -> List(Y , OPn_RF , OPn_RF , INST_MEM, MEM_SH  , WR_N  , WRA_X  , IMM_N  ),
      SW        -> List(Y , OPn_RF , OPn_RF , INST_MEM, MEM_SW  , WR_N  , WRA_X  , IMM_N  ),
      LWL       -> List(Y , OPn_RF , OPn_RF , INST_MEM, MEM_LWL , WR_Y  , WRA_T2 , IMM_N  ),
      LWR       -> List(Y , OPn_RF , OPn_RF , INST_MEM, MEM_LWR , WR_Y  , WRA_T2 , IMM_N  ),
      SWL       -> List(Y , OPn_RF , OPn_RF , INST_MEM, MEM_SWL , WR_N  , WRA_X  , IMM_N  ),
      SWR       -> List(Y , OPn_RF , OPn_RF , INST_MEM, MEM_SWR , WR_N  , WRA_X  , IMM_N  ),
      LL        -> List(Y , OPn_RF , OPn_X  , INST_MEM, MEM_LL  , WR_Y  , WRA_T2 , IMM_N  ),
      SC        -> List(Y , OPn_RF , OPn_RF , INST_MEM, MEM_SC  , WR_Y  , WRA_T2 , IMM_N  ),


      SYNC      -> List(Y , OPn_X  , OPn_X  , INST_N  , OP_N    , WR_N  , WRA_X  , IMM_N  ),
      PREF      -> List(Y , OPn_X  , OPn_X  , INST_N  , OP_N    , WR_N  , WRA_X  , IMM_N  ),
      PREFX     -> List(Y , OPn_X  , OPn_X  , INST_N  , OP_N    , WR_N  , WRA_X  , IMM_N  ),

      // Cache
      CACHE     -> List(Y , OPn_RF , OPn_X  , INST_MEM, MEM_CAC , WR_N  , WRA_X  , IMM_N  ),

    )
  )
  // @formatter:on

  /** for easy modification */
  val (csInstValid: Bool) :: csOP1Type :: csOP2Type :: csInstType :: cs0 = csignals
  val csOpType :: (csWR: Bool) :: csWRType :: csIMMType :: Nil = cs0

  io.out.instValid := csInstValid
  io.out.op1Type   := csOP1Type
  io.out.op2Type   := csOP2Type
  io.out.instType  := csInstType
  io.out.operation := csOpType
  io.out.wr        := csWR
  io.out.wraType   := csWRType
  io.out.immType   := csIMMType
}
