// See README.md for license details.

package cpu.core

import chisel3._
import cpu.common.{CP0Constants, DefaultWireLength, Instructions}
import shared.Util

trait opConstants {
  // @formatter:off
  // 指令是否有效
  val Y       = true.B
  val N       = false.B

  // Operand
  val OPn_RF    = 0.U(1.W)    // regfile
  val OPn_IMM   = 1.U(1.W)    // 立即数
  val OPn_X     = 1.U(1.W)    // Dont care

  // 指令类型（判断执行阶段写入寄存器结果的来源 ALU, CP0...）
  val instTypeLen = 4
  val INST_N      = 0.U(instTypeLen.W)
  val INST_ALU    = 1.U(instTypeLen.W)    // 普通ALU,
  val INST_MV     = 2.U(instTypeLen.W)    // Move（包括hilo, cp0)
  val INST_WO     = 3.U(instTypeLen.W)    // 写入其它寄存器（hilo, cp0)
  val INST_MEM    = 4.U(instTypeLen.W)    // 访存
  val INST_BR     = 5.U(instTypeLen.W)    // 跳转指令
  val INST_EXC    = 6.U(instTypeLen.W)    // 例外指令
  val INST_TLB    = 7.U(instTypeLen.W)    // TLB instruction
  val INST_TRAP   = 8.U(instTypeLen.W)

  val opLen = 7
  val OP_N     = 0.U(opLen.W)   // 无操作
  // ALU 类型
  val ALU_OR    = 1.U(opLen.W)    // OR
  val ALU_AND   = 2.U(opLen.W)
  val ALU_XOR   = 3.U(opLen.W)    // 位异或
  val ALU_NOR   = 4.U(opLen.W)    // 位或非
  val ALU_SLL   = 5.U(opLen.W)    // 逻辑左移
  val ALU_SRL   = 6.U(opLen.W)    // 逻辑右移
  val ALU_SRA   = 7.U(opLen.W)    // 运算右移
  val ALU_SLT   = 8.U(opLen.W)    // 小于则置为1
  val ALU_SLTU  = 9.U(opLen.W)    // 小于则置为1（无符号）
  val ALU_ADD   = 10.U(opLen.W)
  val ALU_ADDU  = 11.U(opLen.W)
  val ALU_SUB   = 12.U(opLen.W)
  val ALU_SUBU  = 13.U(opLen.W)
  // Move
  val MV_MFHI   = 14.U(opLen.W)
  val MV_MFLO   = 15.U(opLen.W)
  val MV_MFC0   = 16.U(opLen.W)
  // write to other 类型
  val WO_MTC0   = 17.U(opLen.W)    // only used in decode
  val WO_MTLO   = 18.U(opLen.W)
  val WO_MTHI   = 19.U(opLen.W)
  val WO_MULT   = 20.U(opLen.W)
  val WO_MULTU  = 21.U(opLen.W)
  val WO_DIV    = 22.U(opLen.W)
  val WO_DIVU   = 23.U(opLen.W)
  // 访存类型 Memory
  val MEM_LB    = 24.U(opLen.W)
  val MEM_LBU   = 25.U(opLen.W)
  val MEM_LH    = 26.U(opLen.W)
  val MEM_LHU   = 27.U(opLen.W)
  val MEM_LW    = 28.U(opLen.W)
  val MEM_SB    = 29.U(opLen.W)
  val MEM_SH    = 30.U(opLen.W)
  val MEM_SW    = 31.U(opLen.W)
  // 跳转指令类型 BRanch
  val BR_JR     = 32.U(opLen.W)   // Jump Register
  val BR_JALR   = 33.U(opLen.W)   // Jump Register and Link
  val BR_J      = 34.U(opLen.W)   // Jump
  val BR_JAL    = 35.U(opLen.W)   // Jump and Link
  val BR_EQ     = 36.U(opLen.W)   // Branch on Equal
  val BR_NE     = 37.U(opLen.W)   // Branch on Not Equal
  val BR_GTZ    = 38.U(opLen.W)   // Branch on Greater Than Zero
  val BR_GEZ    = 39.U(opLen.W)   // Branch on Greater/Equal Than Zero
  val BR_GEZAL  = 40.U(opLen.W)   // Branch on Greater/Equal Than Zero and Link
  val BR_LTZ    = 41.U(opLen.W)   // Branch on Less Than Zero
  val BR_LTZAL  = 42.U(opLen.W)   // Branch on Less Than Zero and Link
  val BR_LEZ    = 43.U(opLen.W)   // Branch on Less/Equal Than Zero
  // 指令例外类型 Except  (不包括未定义指令例外）
  val EXC_SC    = 44.U(opLen.W)   // syscall 系统调用
  val EXC_ER    = 45.U(opLen.W)   // eret 返回
  val EXC_BR    = 46.U(opLen.W)   // break 中断
  // TLB type
  val TLB_WI    = 47.U(opLen.W)
  val TLB_WR    = 48.U(opLen.W)
  val TLB_P     = 49.U(opLen.W)
  val TLB_R     = 50.U(opLen.W)
  val ALU_MUL   = 51.U(opLen.W)
  val MV_MOVN   = 52.U(opLen.W)
  val MV_MOVZ   = 53.U(opLen.W)
  val MEM_CAC   = 54.U(opLen.W)
  val ALU_CLO   = 55.U(opLen.W)
  val ALU_CLZ   = 56.U(opLen.W)
  val MEM_LWL   = 57.U(opLen.W)
  val MEM_LWR   = 58.U(opLen.W)
  val MEM_SWL   = 59.U(opLen.W)
  val MEM_SWR   = 60.U(opLen.W)
  val EXC_WAIT  = 61.U(opLen.W)
  val WO_MADD   = 62.U(opLen.W)
  val WO_MADDU  = 63.U(opLen.W)
  val WO_MSUB   = 64.U(opLen.W)
  val WO_MSUBU  = 65.U(opLen.W)
  val MEM_LL    = 66.U(opLen.W)
  val MEM_SC    = 67.U(opLen.W)

  val TRAP_EQ   = 1.U(opLen.W)
  val TRAP_GE   = 2.U(opLen.W)
  val TRAP_GEU  = 3.U(opLen.W)
  val TRAP_LT   = 4.U(opLen.W)
  val TRAP_LTU  = 5.U(opLen.W)
  val TRAP_NE   = 6.U(opLen.W)

  /** judge whether op is to load data from memory */
  def opIsLoad(op: UInt): Bool = {
    require(op.getWidth == opLen)
    VecInit(MEM_LB, MEM_LBU, MEM_LH, MEM_LHU, MEM_LW, MEM_LWL, MEM_LWR, MEM_LL).contains(op)
  }
  /** judge whether op is to save data to memory */
  def opIsStore(op: UInt): Bool = {
    require(op.getWidth == opLen)
    VecInit(MEM_SB, MEM_SH, MEM_SW, MEM_SWL, MEM_SWR, MEM_SC).contains(op)
  }
  /** judge whether op is branch. */
  def opIsBBranch(op: UInt): Bool = {
    require(op.getWidth == opLen)
    Util.listHasElement(List(BR_EQ, BR_NE, BR_GTZ, BR_GEZ, BR_GEZAL, BR_LTZ, BR_LTZAL, BR_LEZ), op)
  }

  def opIsHILOWrite(op: UInt): Bool = {
    require(op.getWidth == opLen)
    VecInit(WO_MULT, WO_MULTU, WO_DIV, WO_DIVU, ALU_MUL, WO_MTHI, WO_MTLO, WO_MADD, WO_MADDU, WO_MSUB, WO_MSUBU).contains(op)
  }

  def opIsC0Write(op: UInt): Bool = {
    require(op.getWidth == opLen)
    VecInit(TLB_P, TLB_R, WO_MTC0, MEM_CAC).contains(op)
  }

  def opIsC0Read(op: UInt): Bool = {
    require(op.getWidth == opLen)
    VecInit(TLB_WI, TLB_WR, MV_MFC0, MEM_CAC).contains(op)
  }



  // 是否写regfile, Write Register
  val WR_Y      = true.B
  val WR_N      = false.B
  // 写寄存器目标 Write Register Address type
  val WRA_T1    = 0.U(2.W)    // 取inst(15,11)
  val WRA_T2    = 1.U(2.W)    // 取inst(20,16)
  val WRA_T3    = 2.U(2.W)    // 取"b11111", 即31号寄存器
  val WRA_X     = 0.U(2.W)    // not care

  // 立即数类型
  private val IL = 3
  val IMM_N     = 0.U(IL.W)
  val IMM_LSE   = 1.U(IL.W)   // 立即数取inst(15,0)作为低16位，符号扩展，适用于ADDI，ADDIU，SLTI，和SLTIU
  val IMM_LZE   = 2.U(IL.W)   // 立即数取inst(15,0)作为低16位，零扩展，适用于位操作指令
  val IMM_HZE   = 3.U(IL.W)   // 立即数取inst(15,0)作为高16位，零扩展，适用于LUI （是否有必要？）
  val IMM_SHT   = 4.U(IL.W)   // 立即数取inst(10,6)作为低5位，不关心扩展，适用于SLL，SRL，SRA
  // @formatter:on
}

trait cacheOpConstants {

  /** Instruction cache */
  val TARGET_I = 0.U

  /** Data cache */
  val TARGET_D = 1.U

  /** Tertiary */
  val TARGET_T = 2.U

  /** Secondary */
  val TARGET_S = 3.U
}

trait valueConstants {
  val startPC           = "hbfc00000".U(32.W)
  val tlbRefillExceptPC = "hbfc00200".U(32.W)
  val generalExceptPC   = "hbfc00380".U(32.W)
  val zeroWord          = 0.U(32.W)
  val GPR31             = "b11111".U(5.W)
}

trait exceptConstants {
  val exceptAmount = 17

  val EXCEPT_FETCH        = 0
  val EXCEPT_ERET         = 1
  val EXCEPT_INST_INVALID = 2
  val EXCEPT_BREAK        = 3
  val EXCEPT_SYSCALL      = 4
  val EXCEPT_OVERFLOW     = 5
  val EXCEPT_LOAD         = 6
  val EXCEPT_STORE        = 7

  /** External interrupt */
  val EXCEPT_INTR = 8

  // TLB
  val EXCEPT_INST_TLB_REFILL     = 9
  val EXCEPT_INST_TLB_INVALID    = 10
  val EXCEPT_DATA_TLB_R_REFILL   = 11
  val EXCEPT_DATA_TLB_R_INVALID  = 12
  val EXCEPT_DATA_TLB_W_REFILL   = 13
  val EXCEPT_DATA_TLB_W_INVALID  = 14
  val EXCEPT_DATA_TLB_W_MODIFIED = 15

  val EXCEPT_TRAP = 16

  def isTLBExcept(exc: Vec[Bool]): Bool = {
    require(exc.length == exceptAmount)
    exc(EXCEPT_INST_TLB_REFILL) || exc(EXCEPT_INST_TLB_INVALID) || exc(EXCEPT_DATA_TLB_R_REFILL) ||
    exc(EXCEPT_DATA_TLB_R_INVALID) || exc(EXCEPT_DATA_TLB_W_REFILL) || exc(EXCEPT_DATA_TLB_W_INVALID) ||
    exc(EXCEPT_DATA_TLB_W_MODIFIED)
  }
}

object Constants
    extends opConstants
    with exceptConstants
    with Instructions
    with DefaultWireLength
    with cacheOpConstants
    with valueConstants
    with CP0Constants {}
