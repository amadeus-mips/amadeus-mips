// See README.md for license details.

package cpu.core.execute.components

import chisel3._
import chisel3.util._
import cpu.core.Constants._
import cpu.core.bundles.HILOBundle

/**
  * some Move instruction, include MFC0, MFHI, MFLO, MOVZ, MOVN
  */
class MoveIO extends Bundle {
  val op1       = Input(UInt(dataLen.W))
  val op2       = Input(UInt(dataLen.W))
  val operation = Input(UInt(opLen.W))
  val hilo      = Input(new HILOBundle)
  val cp0Data   = Input(UInt(dataLen.W))

  val result = Output(UInt(dataLen.W))
  val we     = Output(Bool())
}

class Move extends Module {
  val io = IO(new MoveIO)

  io.result := MuxLookup(
    io.operation,
    0.U,
    Array(
      MV_MFHI -> io.hilo.hi,
      MV_MFLO -> io.hilo.lo,
      MV_MFC0 -> io.cp0Data,
      MV_MOVZ -> io.op1,
      MV_MOVN -> io.op1
    )
  )

  io.we := !(io.operation === MV_MOVN && !io.op2.orR() || io.operation === MV_MOVZ && io.op2.orR())
//  io.result.valid := io.operation =/= MV_N
}
