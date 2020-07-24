package cpu.core.pipeline

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.stages.{Mem0Mem1Bundle, Mem1Mem2Bundle}

class Memory1Top extends Module {
  val io = IO(new Bundle() {
    val in       = Input(new Mem0Mem1Bundle)
    val commit   = Input(Bool())
    val out      = Output(new Mem1Mem2Bundle)
    val stallReq = Output(Bool())
  })

  io.out.addrL2   := io.in.addrL2
  io.out.op       := io.in.op
  io.out.write    := io.in.write
  io.out.pc       := io.in.pc
  io.out.valid    := io.in.valid
  io.out.uncached := io.in.uncached
  when(!io.in.valid) {
    io.out := 0.U.asTypeOf(io.out)
  }

  io.stallReq := io.in.valid && (opIsLoad(io.in.op) || opIsStore(io.in.op)) && !io.commit
}
