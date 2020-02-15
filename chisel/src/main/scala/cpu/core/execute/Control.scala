// See README.md for license details.

package cpu.core.execute

import chisel3._
import chisel3.util.MuxLookup
import cpu.core.Constants._
import cpu.core.bundles.{IDEXBundle, WriteBundle, WriteControlBundle}

class Control extends Module {
  val io = IO(new Bundle {
    val instType = Input(UInt(instTypeLen.W))
    val inWrite = Input(new WriteControlBundle)
    val pcPlus4 = Input(UInt(addrLen.W))
    val inExcept = Input(Vec(exceptionTypeAmount, Bool()))

    val aluResult = Input(UInt(dataLen.W))
    val aluOverflow = Input(Bool())

    val exceptLoad = Input(Bool())
    val exceptSave = Input(Bool())

    val moveResult = Input(UInt(dataLen.W))

    val outWrite = Output(new WriteBundle())
    val outExcept = Output(Vec(exceptionTypeAmount, Bool()))
  })

  io.outExcept <> io.inExcept
  io.outExcept(EXCEPT_OVERFLOW) := io.aluOverflow
  io.outExcept(EXCEPT_LOAD) := io.exceptLoad
  io.outExcept(EXCEPT_SAVE) := io.exceptSave

  io.outWrite.control <> io.inWrite
  when(io.aluOverflow || io.exceptSave || io.exceptLoad){ io.outWrite.control.enable := false.B}
  io.outWrite.data := MuxLookup(io.instType, 0.U,
    Array(
      INST_ALU -> io.aluResult,
      INST_MV -> io.moveResult,
      INST_BR -> (io.pcPlus4 + 4.U),  // 链接跳转的写入地址为pc + 8
    )
  )

}
