// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.{Fill, MuxCase}
import cpu.core.Constants._

class Hazard extends Module {
  val io = IO(new Bundle {
    val except   = Input(Vec(exceptAmount, Bool()))
    val EPC      = Input(UInt(dataLen.W))
    val stallReq = Input(Vec(5, Bool()))

    val flush   = Output(Bool())
    val flushPC = Output(UInt(dataLen.W))
    val stall   = Output(UInt(cpuStallLen.W))

    val delaySlots = Input(Vec(3, Bool())) // fetch, fetch1, decode

    val lastDS = Output(UInt(2.W))
    val predictFailFlush = Output(Vec(2, Bool())) // if_if1, if1_id
  })

  val hasExcept = io.except.asUInt().orR()

  io.flush   := hasExcept
  io.flushPC := Mux(io.except(EXCEPT_ERET), io.EPC, exceptPC)

  io.stall := MuxCase(
    0.U,
    Array(
      hasExcept -> 0.U,
      io.stallReq(4) -> "b111111".U,
      io.stallReq(3) -> "b011111".U,
      io.stallReq(2) -> "b001111".U,
      io.stallReq(1) -> "b000111".U,
      io.stallReq(0) -> "b000011".U
    )
  )

  val lastDS = WireInit(0.U(2.W))
  for(i <- 0 until 3){
    when(io.delaySlots(i)){ lastDS := i.U }
  }

  io.predictFailFlush(0) := lastDS === 2.U || lastDS === 1.U && !io.stall(1)
  io.predictFailFlush(1) := lastDS === 2.U && !io.stall(2)
  io.lastDS := lastDS
}
