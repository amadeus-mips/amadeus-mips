// See README.md for license details.

package cpu.core

import chisel3._
import cpu.common.{DefaultConfig, DefaultWireLength, Instructions}

trait opConstants {
  // @formatter:off
  // 指令是否有效
  val Y       = true.B
  val N       = true.B

  // 是否为链接跳转 BRanch Link
  val BRL_Y   = true.B        // 仅当为链接跳转指令时
  val BRL_N   = false.B
  val BRL_X   = false.B       // not care
  private val BL = 4          // BR type length
  // 跳转指令类型 BRanch
  val BR_N      = 0.U(BL.W)   // not branch
  val BR_JR     = 1.U(BL.W)   // Jump Register
  val BR_J      = 2.U(BL.W)   // Jump
  val BR_EQ     = 3.U(BL.W)   // Branch on Equal
  val BR_NE     = 4.U(BL.W)   // Branch on Not Equal
  val BR_GTZ    = 5.U(BL.W)   // Branch on Greater Than Zero
  val BR_GEZ    = 6.U(BL.W)   // Branch on Greater/Equal Than Zero
  val BR_LTZ    = 7.U(BL.W)   // Branch on Less Than Zero
  val BR_LEZ    = 8.U(BL.W)   // Branch on Less/Equal Than Zero

  // 指令例外类型 Except  (不包括未定义指令例外）
  val EXC_N     = 0.U(2.W)    // 无指令例外
  val EXC_SC    = 1.U(2.W)    // syscall 系统调用
  val EXC_ER    = 2.U(2.W)    // eret 返回
  val EXC_BR    = 3.U(2.W)    // break 中断

  // 是否为无符号运算（ALU与MEM） UnSigned
  val US_U      = true.B      // Unsigned
  val US_S      = false.B     // Signed
  val US_X      = true.B      // not care

  // OP1
  val OP1_RS    = 0.U(2.W)    // regfile
  val OP1_IMM   = 1.U(2.W)    // 立即数
  val OP1_N     = 2.U(2.W)    // 不需要

  // OP2
  val OP2_RS    = 0.U(2.W)    // regfile
  val OP2_IMM   = 1.U(2.W)    // 立即数
  val OP2_N     = 2.U(2.W)    // 不需要

  // ALU 类型
  private val AL = DefaultConfig.aluOpLen
  val ALU_N     = 0.U(AL.W)   // 无ALU
  val ALU_OR    = 1.U(AL.W)   // OR
  val ALU_AND   = 2.U(AL.W)
  val ALU_XOR   = 3.U(AL.W)   // 位异或
  val ALU_NOR   = 4.U(AL.W)   // 位或非
  val ALU_SLL   = 5.U(AL.W)   // 逻辑左移
  val ALU_SRL   = 6.U(AL.W)   // 逻辑右移
  val ALU_SRA   = 7.U(AL.W)   // 运算右移
  val ALU_MFHI  = 8.U(AL.W)   // 从HI寄存器读取
  val ALU_MFLO  = 9.U(AL.W)   // ^
  val ALU_MTHI  = 10.U(AL.W)  // ^
  val ALU_MTLO  = 11.U(AL.W)  // ^
  val ALU_SLT   = 12.U(AL.W)  // 小于则置为1
  val ALU_ADD   = 13.U(AL.W)
  val ALU_SUB   = 14.U(AL.W)
  val ALU_MULT  = 15.U(AL.W)  // 乘
  val ALU_DIV   = 16.U(AL.W)  // 除
  val ALU_MFC0  = 17.U(AL.W)
  val ALU_MTC0  = 18.U(AL.W)
  val ALU_MEM   = 19.U(AL.W)

  // 访存类型 Memory
  private val ML = DefaultConfig.memOpLen
  val MEM_N     = 0.U(ML.W)   // 无访存
  val MEM_LB    = 1.U(ML.W)
  val MEM_LH    = 2.U(ML.W)
  val MEM_LW    = 3.U(ML.W)
  val MEM_SB    = 4.U(ML.W)
  val MEM_SH    = 5.U(ML.W)
  val MEM_SW    = 6.U(ML.W)

  // 是否写regfile, Write Register
  val WR_Y      = true.B
  val WR_N      = false.B
  // 写寄存器目标 Write Register Target
  val WRT_T1    = 0.U(2.W)    // 取inst(15,11)
  val WRT_T2    = 1.U(2.W)    // 取inst(20,16)
  val WRT_T3    = 2.U(2.W)    // 取"b11111", 即31号寄存器
  val WRT_X     = 0.U(2.W)    // not care

  // 立即数类型
  private val IL = 3
  val IMM_N     = 0.U(IL.W)
  val IMM_LSE   = 1.U(IL.W)   // 立即数取inst(15,0)作为低16位，符号扩展，适用于ADDI，ADDIU，SLTI，和SLTIU
  val IMM_LZE   = 2.U(IL.W)   // 立即数取inst(15,0)作为低16位，零扩展，适用于位操作指令
  val IMM_HZE   = 3.U(IL.W)   // 立即数取inst(15,0)作为高16位，零扩展，适用于LUI （是否有必要？）
  val IMM_SHT   = 4.U(IL.W)   // 立即数取inst(10,6)作为低5位，不关心扩展，适用于SLL，SRL，SRA
  // @formatter:on
}

trait valueConstants {
  val zeroWord = 0.U(Constants.dataLen.W)
}

trait exceptConstants {
  val EXCEPT_IF = 0
  val EXCEPT_ERET = 1
  val EXCEPT_INST_INVALID = 2
  val EXCEPT_BREAK = 3
  val EXCEPT_SYSCALL = 4
}

object Constants extends
  opConstants with
  exceptConstants with
  Instructions with
  DefaultWireLength with
  valueConstants {

}
