// See README.md for license details.

package cpu.core.fetch

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._
import cpu.core.bundles.stage5.IfIdBundle
import shared.ValidBundle

class Fetch extends Module {
  val io = IO(new Bundle {
    // from ctrl module
    val stall   = Input(UInt(cpuStallLen.W))
    val flush   = Input(Bool())
    val flushPC = Input(UInt(addrLen.W))

    // from id module
    val branch      = Input(new ValidBundle)
    val inDelaySlot = Input(Bool())

    // from ram
    val instValid = Input(Bool())

    // to IFID
    val out = Output(new IfIdBundle)
    // to ram
    val outPCValid = Output(Bool())
    // to ctrl
    val outStallReq = Output(Bool())
  })

  val pc              = RegInit(startPC)
  val instFetchExcept = pc(1, 0).orR

  val inDelaySlotBuffer = RegInit(false.B)
  when(io.inDelaySlot && io.outStallReq) {
    inDelaySlotBuffer := io.inDelaySlot
  }.elsewhen(!io.stall(0)) {
    inDelaySlotBuffer := false.B
  }

  def inDelaySlot = io.inDelaySlot || inDelaySlotBuffer

  val branchBuffer = RegInit(0.U.asTypeOf(new ValidBundle()))
  when(io.flush) {
    branchBuffer.valid := false.B
  }.elsewhen(io.outStallReq && inDelaySlot && io.branch.valid) {
      branchBuffer <> io.branch
    }
    .elsewhen(branchBuffer.valid && !io.stall(0)) {
      branchBuffer.valid := false.B
    }

  io.out.instFetchExcept := instFetchExcept
  io.out.pc              := pc
  io.out.instValid       := io.instValid
  io.out.inDelaySlot     := inDelaySlot

  io.outPCValid := !instFetchExcept
//  io.outPCValid := !instFetchExcept && (!(io.instValid && io.stall(0)))
  io.outStallReq := !io.instValid & !instFetchExcept

  pc := MuxCase(
    pc + 4.U,
    Array(
      io.flush                          -> io.flushPC,
      (!inDelaySlot && io.branch.valid) -> io.branch.bits,
      io.stall(0)                       -> pc,
      io.branch.valid                   -> io.branch.bits,
      branchBuffer.valid                -> branchBuffer.bits
    )
  )
}
