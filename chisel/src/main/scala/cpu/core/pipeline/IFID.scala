// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.IFIDBundle

class IFID extends Module {
  val io = IO(new Bundle {
    val stall = Input(UInt(cpuStallLen.W))
    val flush = Input(Bool())
    val in = Input(new IFIDBundle)
    val out = Output(new IFIDBundle)
  })

  val pipeReg = RegInit(0.U.asTypeOf(new IFIDBundle))

  /**
   *  是否为流水线stall的终止点，
   *  即此前的流水阶段被stall，此后的流水阶段没有被stall
   */
  val stallEnd = io.stall(1) && !io.stall(2)

  when(io.flush || stallEnd) {
    pipeReg := 0.U.asTypeOf(new IFIDBundle)
  }.elsewhen(!io.stall(1)) {
    pipeReg <> io.in
    pipeReg.inst := Mux(io.in.instFetchExcept, 0.U, io.in.inst)
  }.otherwise {
    // stalled, do nothing
  }

  io.out <> pipeReg
}
