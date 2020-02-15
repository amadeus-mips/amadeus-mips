// See README.md for license details.

package cpu.core.execute.control

import chisel3._
import chisel3.util.{MuxLookup, ValidIO}
import cpu.core.Constants._
import cpu.core.bundles.{IDEXBundle, WriteBundle}

class Control extends Module {
  val io = IO(new Bundle {
    val in = Input(new IDEXBundle)
    val aluResult = Input(UInt(dataLen.W))
    val aluOverflow = Input(Bool())
    val moveResult = Input(UInt(dataLen.W))
    val write = Output(new WriteBundle())
  })

  val overExcept = io.aluOverflow

  io.write <> io.in.write
  when(overExcept){ io.write.control.enable := false.B}
  io.write.data := MuxLookup(io.in.instType, 0.U,
    Array(
      INST_ALU -> io.aluResult,
      INST_MV -> io.moveResult
    )
  )

}
