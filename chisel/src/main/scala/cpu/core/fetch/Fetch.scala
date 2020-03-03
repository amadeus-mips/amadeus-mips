// See README.md for license details.

package cpu.core.fetch

import chisel3._
import common.ValidBundle
import cpu.core.Constants._
import cpu.core.bundles.stage5.IfIdBundle

class Fetch extends Module {
  val io = IO(new Bundle {
    // from ctrl module
    val stall = Input(UInt(cpuStallLen.W))
    val flush = Input(Bool())
    val flushPC = Input(UInt(addrLen.W))

    // from id module
    val branch = Input(new ValidBundle)

    // from ram
    val instValid = Input(Bool())

    // to IFID
    val out = Output(new IfIdBundle)
    // to ram
    val outPCValid = Output(Bool())
    // to ctrl
    val outStallReq = Output(Bool())
  })

  val pc = RegInit(startPC)
  val instFetchExcept = pc(1, 0) =/= 0.U

  io.out.instFetchExcept := instFetchExcept
  io.out.pc := pc
  io.out.instValid := io.instValid
  io.outPCValid := !instFetchExcept
//  io.outPCValid := !instFetchExcept && (!(io.instValid && io.stall(0)))
  io.outStallReq := !io.instValid

  when(io.flush) {
    // flush pipeLine
    pc := io.flushPC
  }.elsewhen(!io.stall(0)) {
    // not stalled, select branch or pc plus 4
    pc := Mux(io.branch.valid, io.branch.bits, pc + 4.U)
  }.otherwise {
    // stalled
  }
}
