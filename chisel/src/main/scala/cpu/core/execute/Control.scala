// See README.md for license details.

package cpu.core.execute

import chisel3._
import chisel3.util.MuxLookup
import cpu.core.Constants._
import cpu.core.bundles.WriteBundle

class Control extends Module {
  val io = IO(new Bundle {
    val instType = Input(UInt(instTypeLen.W))
    val inWrite  = Input(new WriteBundle)
    val pc       = Input(UInt(addrLen.W))
    val inExcept = Input(Vec(exceptAmount, Bool()))

    val aluResult   = Input(UInt(dataLen.W))
    val aluOverflow = Input(Bool())

    val trap = Input(Bool())

    val exceptLoad = Input(Bool())
    val exceptSave = Input(Bool())

    val moveResult = Input(UInt(dataLen.W))
    val moveWe     = Input(Bool())

    val memWriteData = Input(UInt(dataLen.W))

    val outWrite  = Output(new WriteBundle())
    val outExcept = Output(Vec(exceptAmount, Bool()))
  })

  io.outExcept := io.inExcept

  io.outExcept(EXCEPT_OVERFLOW) := io.aluOverflow
  io.outExcept(EXCEPT_LOAD)     := io.exceptLoad
  io.outExcept(EXCEPT_STORE)    := io.exceptSave
  io.outExcept(EXCEPT_TRAP)     := io.trap && io.instType === INST_TRAP

  io.outWrite := io.inWrite
  when(io.aluOverflow || io.exceptSave || io.exceptLoad || !io.moveWe) { io.outWrite.enable := false.B }
  io.outWrite.data := MuxLookup(
    io.instType,
    0.U,
    Array(
      INST_ALU -> io.aluResult,
      INST_MV  -> io.moveResult,
      INST_BR  -> (io.pc + 8.U), // 链接跳转的写入地址为pc + 8
      INST_MEM -> io.memWriteData
    )
  )
  io.outWrite.valid := io.inWrite.valid ||
    VecInit(Seq(INST_ALU, INST_MV, INST_BR)).contains(io.instType)

}
