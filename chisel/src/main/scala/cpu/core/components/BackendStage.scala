package cpu.core.components

import chisel3._
import chisel3.util._

class BackendStage[+T <: Data](gen:T) extends MultiIOModule {
  val io = IO(new Bundle {
    // from stage bellow
    val stallReq = Input(Bool())
    // from stage before
    val stallFLush = Input(Bool())
    // from hazard
    val flush = Input(Bool())
    // from last stage reg
    val inIsBubble = Input(Bool())
    val in = Input(gen)

    val out = Output(gen)
    // to next stage reg
    val outIsBubble = Output(Bool())
    // to last stage reg
    val stallForward = Output(Bool())
    val stalled = Output(Bool())
  })

  val pipeReg = RegInit(0.U(gen.getWidth.W))
  val bubbleReg = RegInit(false.B)
  val stalledReg = RegInit(false.B)

  when(io.flush) {
    pipeReg := 0.U
    bubbleReg := false.B
    stalledReg := false.B
  }.elsewhen(io.stallReq) {
    // stalled, do nothing
    stalledReg := true.B
  }.elsewhen(io.stallFLush) {
    pipeReg := 0.U
    bubbleReg := true.B
    stalledReg := false.B
  }.otherwise{
    pipeReg := io.in.asUInt()
    bubbleReg := io.inIsBubble
    stalledReg := false.B
  }

  io.out := pipeReg.asTypeOf(gen)
  io.outIsBubble := bubbleReg
  io.stallForward := io.stallReq && !io.inIsBubble
  io.stalled := stalledReg
}
