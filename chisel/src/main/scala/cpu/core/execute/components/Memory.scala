// See README.md for license details.

package cpu.core.execute.components

import chisel3._
import cpu.core.Constants._
import shared.Util

class Memory extends Module {
  val io = IO(new Bundle {
    val op1 = Input(UInt(dataLen.W))
    val op2 = Input(UInt(dataLen.W))
    val imm16 = Input(UInt(16.W))
    val operation = Input(UInt(opLen.W))

    val memAddr = Output(UInt(addrLen.W))
    val exceptLoad = Output(Bool())
    val exceptSave = Output(Bool())
  })

  val offset = Util.signedExtend(io.imm16)  // extend to 32bits
  val memAddr = io.op1 + offset
  io.memAddr := memAddr

  /** Exception caused by request not alignment */
  io.exceptLoad := ((io.operation === MEM_LH || io.operation === MEM_LHU) && memAddr(0) =/= 0.U) ||
    (io.operation === MEM_LW && memAddr(1,0) =/= 0.U)
  io.exceptSave := (io.operation === MEM_SH && memAddr(0) =/= 0.U) ||
    (io.operation === MEM_SW && memAddr(1,0) =/= 0.U)
}
