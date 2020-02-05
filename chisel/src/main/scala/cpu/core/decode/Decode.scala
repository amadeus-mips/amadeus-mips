// See README.md for license details.

package cpu.core.decode

import chisel3._
import chisel3.util._
import cpu.core.Constants._
import cpu.core.bundles.{IDEXBundle, IFIDBundle, ReadRegisterMasterBundle, WriteRegisterBundle}

class Decode extends Module {
  val io = IO(new Bundle {
    val ifIn = Input(new IFIDBundle)
    val exMemOp = Input(UInt(memOpLen.W)) // from ex, 解决load相关
    val exWR = Input(new WriteRegisterBundle) // from ex, 数据前推
    val memWR = Input(new WriteRegisterBundle) // from mem, 数据前推
    val reg1 = new ReadRegisterMasterBundle // with regfile
    val reg2 = new ReadRegisterMasterBundle // ^
    val inDelaySlot = Input(Bool()) // 若上一指令为跳转指令，则当前指令为延迟槽指令

    val out = Output(new IDEXBundle) // to IDEX
    val nextInstInDelaySlot = Output(Bool()) // ^
    val branchFlag = Output(Bool())
    val branchTarget = Output(UInt(addressLen.W))
    val branchLinkAddress = Output(UInt(addressLen.W))
    val outputInDelaySlot = Output(Bool())
    val stallReq = Output(Bool())
    val except = Output(Vec(exceptionTypeNumber, Bool()))
    val pc = Output(UInt(addressLen.W))
  })

  // @formatter:off
  val csignals: List[UInt]= ListLookup(io.ifIn.inst,
                     List(N , BRL_X, BR_N  , EXC_N  , US_X     , OP1_N  , OP2_N  , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),
      Array(         /* val  | BR   | BR    | Except | Unsigned | Op1    | Op2    | ALU     | MEM   | Write | WReg   | Imm */
                     /* inst | link | type  | type   |          | sel    | sel    | type    | type  | reg   | Target | type */
        // 位操作
        OR        -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_OR  , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        AND       -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_AND , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        XOR       -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_XOR , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        NOR       -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_NOR , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
          // 立即数
        ORI       -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_IMM, ALU_OR  , MEM_N , WR_Y  , WRT_T2 , IMM_LZE),
        ANDI      -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_IMM, ALU_AND , MEM_N , WR_Y  , WRT_T2 , IMM_LZE),
        XORI      -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_IMM, ALU_XOR , MEM_N , WR_Y  , WRT_T2 , IMM_LZE),
        LUI       -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_IMM, ALU_OR  , MEM_N , WR_Y  , WRT_T2 , IMM_HZE),

        // 移位
        SLLV      -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_SLL , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        SRLV      -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_SRL , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        SRAV      -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_SRL , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        SLL       -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_IMM, OP2_RS , ALU_SLL , MEM_N , WR_Y  , WRT_T1 , IMM_SHT),
        SRL       -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_IMM, OP2_RS , ALU_SRL , MEM_N , WR_Y  , WRT_T1 , IMM_SHT),
        SRA       -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_IMM, OP2_RS , ALU_SRA , MEM_N , WR_Y  , WRT_T1 , IMM_SHT),

        // HI，LO的Move指令
        MFHI      -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_N  , OP2_N  , ALU_MFHI, MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        MFLO      -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_N  , OP2_N  , ALU_MFLO, MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        MTHI      -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_N  , ALU_MTHI, MEM_N , WR_N  , WRT_X  , IMM_N  ),
        MTLO      -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_N  , ALU_MTLO, MEM_N , WR_N  , WRT_X  , IMM_N  ),
        // C0的Move指令
        MFC0      -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_N  , OP2_N  , ALU_MFC0, MEM_N , WR_Y  , WRT_T2 , IMM_N  ),
        MTC0      -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_N  , OP2_RS , ALU_MTC0, MEM_N , WR_N  , WRT_X  , IMM_N  ),

        // 比较指令
        SLT       -> List(Y , BRL_X, BR_N  , EXC_N  , US_S     , OP1_RS , OP2_RS , ALU_SLT , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        SLTU      -> List(Y , BRL_X, BR_N  , EXC_N  , US_U     , OP1_RS , OP2_RS , ALU_SLT , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
          // 立即数
        SLTI      -> List(Y , BRL_X, BR_N  , EXC_N  , US_S     , OP1_RS , OP2_IMM, ALU_SLT , MEM_N , WR_Y  , WRT_T2 , IMM_LSE),
        SLTIU     -> List(Y , BRL_X, BR_N  , EXC_N  , US_U     , OP1_RS , OP2_IMM, ALU_SLT , MEM_N , WR_Y  , WRT_T2 , IMM_LSE),

        // 算术指令
        ADD       -> List(Y , BRL_X, BR_N  , EXC_N  , US_S     , OP1_RS , OP2_RS , ALU_ADD , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        ADDU      -> List(Y , BRL_X, BR_N  , EXC_N  , US_U     , OP1_RS , OP2_RS , ALU_ADD , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        SUB       -> List(Y , BRL_X, BR_N  , EXC_N  , US_S     , OP1_RS , OP2_RS , ALU_SUB , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        SUBU      -> List(Y , BRL_X, BR_N  , EXC_N  , US_U     , OP1_RS , OP2_RS , ALU_SUB , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        MULT      -> List(Y , BRL_X, BR_N  , EXC_N  , US_S     , OP1_RS , OP2_RS , ALU_MULT, MEM_N , WR_N  , WRT_X  , IMM_N  ),
        MULTU     -> List(Y , BRL_X, BR_N  , EXC_N  , US_U     , OP1_RS , OP2_RS , ALU_MULT, MEM_N , WR_N  , WRT_X  , IMM_N  ),
        DIV       -> List(Y , BRL_X, BR_N  , EXC_N  , US_S     , OP1_RS , OP2_RS , ALU_DIV , MEM_N , WR_N  , WRT_X  , IMM_N  ),
        DIVU      -> List(Y , BRL_X, BR_N  , EXC_N  , US_U     , OP1_RS , OP2_RS , ALU_DIV , MEM_N , WR_N  , WRT_X  , IMM_N  ),
          // 立即数
        ADDI      -> List(Y , BRL_X, BR_N  , EXC_N  , US_S     , OP1_RS , OP2_IMM, ALU_ADD , MEM_N , WR_Y  , WRT_T2 , IMM_LSE),
        ADDIU     -> List(Y , BRL_X, BR_N  , EXC_N  , US_U     , OP1_RS , OP2_IMM, ALU_ADD , MEM_N , WR_Y  , WRT_T2 , IMM_LSE),

        // 跳转指令
        J         -> List(Y , BRL_N, BR_J  , EXC_N  , US_X     , OP1_N  , OP2_N  , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),
        JAL       -> List(Y , BRL_Y, BR_J  , EXC_N  , US_X     , OP1_N  , OP2_N  , ALU_N   , MEM_N , WR_Y  , WRT_T3 , IMM_N  ),
        JR        -> List(Y , BRL_N, BR_JR , EXC_N  , US_X     , OP1_RS , OP2_N  , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),
        JALR      -> List(Y , BRL_Y, BR_JR , EXC_N  , US_X     , OP1_RS , OP2_N  , ALU_N   , MEM_N , WR_Y  , WRT_T1 , IMM_N  ),
        BEQ       -> List(Y , BRL_N, BR_EQ , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),
        BNE       -> List(Y , BRL_N, BR_NE , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),
        BGTZ      -> List(Y , BRL_N, BR_GTZ, EXC_N  , US_X     , OP1_RS , OP2_N  , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),
        BLEZ      -> List(Y , BRL_N, BR_LEZ, EXC_N  , US_X     , OP1_RS , OP2_N  , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),
        BGEZ      -> List(Y , BRL_N, BR_GEZ, EXC_N  , US_X     , OP1_RS , OP2_N  , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),
        BGEZAL    -> List(Y , BRL_Y, BR_GEZ, EXC_N  , US_X     , OP1_RS , OP2_N  , ALU_N   , MEM_N , WR_Y  , WRT_T3 , IMM_N  ),
        BLTZ      -> List(Y , BRL_N, BR_LTZ, EXC_N  , US_X     , OP1_RS , OP2_N  , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),
        BLTZAL    -> List(Y , BRL_Y, BR_LTZ, EXC_N  , US_X     , OP1_RS , OP2_N  , ALU_N   , MEM_N , WR_Y  , WRT_T3 , IMM_N  ),

        // 例外指令
        SYSCALL   -> List(Y , BRL_X, BR_N  , EXC_SC , US_X     , OP1_N  , OP2_N  , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),
        BREAK     -> List(Y , BRL_X, BR_N  , EXC_BR , US_X     , OP1_N  , OP2_N  , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),
        ERET      -> List(Y , BRL_X, BR_N  , EXC_ER , US_X     , OP1_N  , OP2_N  , ALU_N   , MEM_N , WR_N  , WRT_X  , IMM_N  ),

        // 访存指令
        LB        -> List(Y , BRL_X, BR_N  , EXC_N  , US_S     , OP1_RS , OP2_N  , ALU_N   , MEM_LB, WR_Y  , WRT_T2 , IMM_N  ),
        LBU       -> List(Y , BRL_X, BR_N  , EXC_N  , US_U     , OP1_RS , OP2_N  , ALU_N   , MEM_LB, WR_Y  , WRT_T2 , IMM_N  ),
        LH        -> List(Y , BRL_X, BR_N  , EXC_N  , US_S     , OP1_RS , OP2_N  , ALU_N   , MEM_LH, WR_Y  , WRT_T2 , IMM_N  ),
        LHU       -> List(Y , BRL_X, BR_N  , EXC_N  , US_U     , OP1_RS , OP2_N  , ALU_N   , MEM_LH, WR_Y  , WRT_T2 , IMM_N  ),
        LW        -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_N  , ALU_N   , MEM_LW, WR_Y  , WRT_T2 , IMM_N  ),
        SB        -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_N   , MEM_SB, WR_N  , WRT_X  , IMM_N  ),
        SH        -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_N   , MEM_SH, WR_N  , WRT_X  , IMM_N  ),
        SW        -> List(Y , BRL_X, BR_N  , EXC_N  , US_X     , OP1_RS , OP2_RS , ALU_N   , MEM_SW, WR_N  , WRT_X  , IMM_N  ),
      )
    )
  // @formatter:on

  val (csInstValid: Bool) :: (csBRL: Bool) :: csBRType :: csEXCType :: csUSType :: csOP1Type :: csOP2Type :: cs0 = csignals
  val csALUType :: csMEMType :: (csWR: Bool) :: csWRType :: csIMMType :: Nil = cs0

  // 根据IMMType选择imm
  val imm = MuxLookup(csIMMType, zeroWord,
    Array(
      IMM_LSE -> Cat(Fill(16, io.ifIn.inst(15)), io.ifIn.inst(15, 0)),
      IMM_LZE -> Cat(Fill(16, 0.U), io.ifIn.inst(15, 0)),
      IMM_HZE -> Cat(io.ifIn.inst(15, 0), Fill(16, 0.U)),
      IMM_SHT -> Cat(Fill(27, 0.U), io.ifIn.inst(10, 6))
    )
  )
  val reg1Address = io.ifIn.inst(25,21)
  val reg2Address = io.ifIn.inst(20,16)

  io.out.aluOp := csALUType
  io.out.aluSigned := csUSType

  // 解决数据冒险
  val reg1Data = MuxCase(zeroWord,
    Array(
      (csOP1Type === OP1_IMM) ->
        imm,
      (csOP1Type === OP1_RS && io.exWR.writeEnable && io.exWR.writeTarget === reg1Address) ->
        io.exWR.writeData,
      (csOP1Type === OP1_RS && io.memWR.writeEnable && io.memWR.writeTarget === reg1Address) ->
        io.memWR.writeData,
      (csOP1Type === OP1_RS) ->
        io.reg1.readData,
    )
  )
  val reg2Data = MuxCase(zeroWord,
    Array(
      (csOP2Type === OP1_IMM) ->
        imm,
      (csOP2Type === OP2_RS && io.exWR.writeEnable && io.exWR.writeTarget === reg2Address) ->
        io.exWR.writeData,
      (csOP2Type === OP2_RS && io.memWR.writeEnable && io.memWR.writeTarget === reg2Address) ->
        io.memWR.writeData,
      (csOP2Type === OP2_RS) ->
        io.reg2.readData,
    )
  )
  io.out.reg1 := reg1Data
  io.out.reg2 := reg2Data

  io.out.writeRegister.writeEnable := csWR
  io.out.writeRegister.writeTarget := MuxLookup(csWRType, zeroWord,
    Array(
      WRT_T1 -> io.ifIn.inst(15,11),
      WRT_T2 -> io.ifIn.inst(20,16),
      WRT_T3 -> "b11111".U(5.W),
    )
  )
  io.out.writeRegister.writeData := DontCare

  io.out.inst := io.ifIn.inst

  io.reg1.readEnable := DontCare
  io.reg2.readEnable := DontCare
  io.reg1.readTarget := io.ifIn.inst(25,21)
  io.reg2.readTarget := io.ifIn.inst(20,16)

  io.nextInstInDelaySlot := csBRType =/= BR_N

  val pcPlus4 = io.ifIn.pc + 4.U
  val BTarget = pcPlus4 + Cat(Fill(14, io.ifIn.inst(15)), io.ifIn.inst(15,0), 0.U(2.W))
  io.branchFlag := false.B
  io.branchTarget := zeroWord
  switch(csBRType) {
    is(BR_N) {
      io.branchFlag := false.B
      io.branchTarget := zeroWord
    }
    is(BR_JR) {
      io.branchFlag := true.B
      io.branchTarget := reg1Data
    }
    is(BR_J) {
      io.branchFlag := true.B
      io.branchTarget := Cat(pcPlus4(31,28), io.ifIn.inst(25,0), 0.U(2.W))
    }
    is(BR_EQ) {
      io.branchFlag := reg1Data === reg2Data
      io.branchTarget := BTarget
    }
    is(BR_NE) {
      io.branchFlag := reg1Data =/= reg2Data
      io.branchTarget := BTarget
    }
    is(BR_GTZ) {
      io.branchFlag := !reg1Data(31) && (reg1Data =/= 0.U)
      io.branchTarget := BTarget
    }
    is(BR_GEZ) {
      io.branchFlag := !reg1Data(31)
      io.branchTarget := BTarget
    }
    is(BR_LTZ) {
      io.branchFlag := reg1Data(31)
      io.branchTarget := BTarget
    }
    is(BR_LEZ) {
      io.branchFlag := !(!reg1Data(31) && (reg1Data =/= 0.U))
      io.branchTarget := BTarget
    }
  }

  io.branchLinkAddress := io.ifIn.pc + 8.U

  io.outputInDelaySlot := io.inDelaySlot

  val preInstIsLoad = io.exMemOp === MEM_LB || io.exMemOp === MEM_LH || io.exMemOp === MEM_LW
  val stallReqByReg1LoadUse = preInstIsLoad && csOP1Type === OP1_RS && io.exWR.writeTarget === reg1Address
  val stallReqByReg2LoadUse = preInstIsLoad && csOP2Type === OP2_RS && io.exWR.writeTarget === reg2Address
  io.stallReq := stallReqByReg1LoadUse || stallReqByReg2LoadUse

  io.except := 0.U(exceptionTypeNumber.W).asBools()
  io.except(EXCEPT_IF) := io.ifIn.instFetchExcept
  io.except(EXCEPT_ERET) := csEXCType === EXC_ER
  io.except(EXCEPT_BREAK) := csEXCType === EXC_BR
  io.except(EXCEPT_SYSCALL) := csEXCType === EXC_SC
  io.except(EXCEPT_INST_INVALID) := csInstValid === N

  io.pc := io.ifIn.pc

}
