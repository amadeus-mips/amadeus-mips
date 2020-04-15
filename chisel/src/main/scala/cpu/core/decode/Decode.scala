// See README.md for license details.

package cpu.core.decode

import chisel3._
import chisel3.util._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, WriteBundle}
import shared.Util

class Decode extends Module {
  val io = IO(new Bundle {
    val signal = Input(new SignalBundle)
    val inst = Input(UInt(dataLen.W))
    val instFetchExc = Input(Bool())
    val rsData = Input(UInt(dataLen.W)) // from Forward
    val rtData = Input(UInt(dataLen.W)) // ^

    val op1 = Output(UInt(dataLen.W))
    val op2 = Output(UInt(dataLen.W))
    val write = Output(new WriteBundle)
    val cp0 = Output(new CPBundle)
    val nextInstInDelaySlot = Output(Bool()) // ^
    val except = Output(Vec(exceptAmount, Bool()))
  })

  val rt = io.inst(20, 16)
  val rd = io.inst(15, 11)
  val sa = io.inst(10, 6)
  val imm16 = io.inst(15, 0)
  val sel = io.inst(2, 0)

  // 根据IMMType选择imm
  val imm32 = MuxLookup(io.signal.immType, 0.U,
    Array(
      IMM_LSE -> Util.signedExtend(imm16),
      IMM_LZE -> Util.zeroExtend(imm16),
      IMM_HZE -> Cat(imm16, Fill(16, 0.U)),
      IMM_SHT -> Util.zeroExtend(sa)
    )
  )

  io.op1 := MuxLookup(io.signal.op1Type, 0.U,
    Array(
      OPn_IMM -> imm32,
      OPn_RF -> io.rsData,
    )
  )
  io.op2 := MuxLookup(io.signal.op2Type, 0.U,
    Array(
      OPn_IMM -> imm32,
      OPn_RF -> io.rtData,
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
  io.write.data := DontCare
  io.write.valid := false.B

  io.cp0.enable := io.signal.operation === WO_MTC0
  io.cp0.address := rd
  io.cp0.sel := sel
  io.cp0.data := DontCare
  io.cp0.valid := false.B

  io.nextInstInDelaySlot := io.signal.instType === INST_BR

  io.except := 0.U(exceptAmount.W).asBools()
  io.except(EXCEPT_FETCH) := io.instFetchExc
  io.except(EXCEPT_ERET) := io.signal.operation === EXC_ER
  io.except(EXCEPT_BREAK) := io.signal.operation === EXC_BR
  io.except(EXCEPT_SYSCALL) := io.signal.operation === EXC_SC
  io.except(EXCEPT_INST_INVALID) := io.signal.instValid === N


}
