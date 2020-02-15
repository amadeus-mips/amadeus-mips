// See README.md for license details.

package cpu.core.decode

import chisel3._
import chisel3.util._
import cpu.core.Constants._
import cpu.core.bundles.{CPControlBundle, IDEXBundle, IFIDBundle, ReadIO, WriteBundle, WriteControlBundle}
import common.Util._

class Decode extends Module {
  val io = IO(new Bundle {
    val signal = Input(new SignalBundle)
    val inst = Input(UInt(dataLen.W))
    val instFetchExc = Input(Bool())
    val rsData = Input(UInt(dataLen.W)) // from Forward
    val rtData = Input(UInt(dataLen.W)) // ^

    val op1 = Output(UInt(dataLen.W))
    val op2 = Output(UInt(dataLen.W))
    val write = Output(new WriteControlBundle)
    val cp0Control = Output(new CPControlBundle)
    val nextInstInDelaySlot = Output(Bool()) // ^
    val except = Output(Vec(exceptionTypeAmount, Bool()))
  })

  val rt = io.inst(20, 16)
  val rd = io.inst(15, 11)
  val sa = io.inst(10, 6)
  val imm16 = io.inst(15, 0)
  val sel = io.inst(2, 0)

  // 根据IMMType选择imm
  val imm32 = MuxLookup(io.signal.immType, 0.U,
    Array(
      IMM_LSE -> signedExtend(imm16),
      IMM_LZE -> zeroExtend(imm16),
      IMM_HZE -> Cat(imm16, Fill(16, 0.U)),
      IMM_SHT -> zeroExtend(sa)
    )
  )

  io.op1 := MuxLookup(io.signal.op1Type, 0.U,
    Array(
      OP1_IMM -> imm32,
      OP1_RS -> io.rsData,
    )
  )
  io.op2 := MuxLookup(io.signal.op2Type, 0.U,
    Array(
      OP1_IMM -> imm32,
      OP2_RS -> io.rtData,
    )
  )

  io.write.enable := io.signal.wr
  io.write.address := MuxLookup(io.signal.wraType, zeroWord,
    Array(
      WRA_T1 -> rd,
      WRA_T2 -> rt,
      WRA_T3 -> GPR31,  // 31th register
    )
  )

  io.cp0Control.enable := io.signal.operation === WO_MTC0
  io.cp0Control.address := rd
  io.cp0Control.sel := sel

  io.nextInstInDelaySlot := io.signal.instType === INST_BR

  io.except := 0.U(exceptionTypeAmount.W).asBools()
  io.except(EXCEPT_IF) := io.instFetchExc
  io.except(EXCEPT_ERET) := io.signal.operation === EXC_ER
  io.except(EXCEPT_BREAK) := io.signal.operation === EXC_BR
  io.except(EXCEPT_SYSCALL) := io.signal.operation === EXC_SC
  io.except(EXCEPT_INST_INVALID) := io.signal.instValid === N


}
