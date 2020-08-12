package cpu.core.pipeline

import chisel3._
import chisel3.util.{Cat, MuxCase}
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.stages.{Mem0Mem1Bundle, Mem1Mem2Bundle}
import cpu.core.components.ExceptionHandleBundle

class Memory1Top(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle() {
    val ins    = Input(Vec(conf.decodeWidth, new Mem0Mem1Bundle))
    val commit = Input(Bool())

    val exceptionCP0 = Input(new ExceptionHandleBundle)

    val out            = Output(Vec(conf.decodeWidth, new Mem1Mem2Bundle))
    val stallReq       = Output(Bool())
    val exceptJumpAddr = Output(UInt(addrLen.W))
  })

  val except = Mux(io.ins(0).except.reduce(_ || _), io.ins(0).except, io.ins(1).except)

  val isInterruptExcept = except(EXCEPT_INTR)
  val isRefillExcept = except(EXCEPT_INST_TLB_REFILL) || except(EXCEPT_DATA_TLB_W_REFILL) || except(
    EXCEPT_DATA_TLB_R_REFILL
  )

  io.exceptJumpAddr := Mux(
    io.exceptionCP0.status.bev,
    MuxCase(
      "hbfc00380".U,
      Seq(
        io.exceptionCP0.status.exl                      -> "hbfc00380".U,
        (isInterruptExcept && io.exceptionCP0.cause.iv) -> "hbfc00400".U,
        isRefillExcept                                  -> "hbfc00200".U
      )
    ),
    Cat(
      io.exceptionCP0.ebase.asUInt()(31, 12),
      MuxCase(
        "h180".U(12.W),
        Seq(
          io.exceptionCP0.status.exl                      -> "h180".U(12.W),
          (isInterruptExcept && io.exceptionCP0.cause.iv) -> "h200".U(12.W),
          isRefillExcept                                  -> 0.U(12.W)
        )
      )
    )
  )

  io.out.zip(io.ins).foreach {
    case (out, in) =>
      out.addrL2   := in.addrL2
      out.op       := in.op
      out.write    := in.write
      out.pc       := in.pc
      out.uncached := in.uncached
  }
  io.out(0).valid := !io.ins(0).except.reduce(_ || _)
  io.out(1).valid := !io.ins.map(_.except.reduce(_ || _)).reduce(_ || _)

  when(!io.out(0).valid) {
    io.out(0).pc := 0.U
  }
  when(!io.out(1).valid) {
    io.out(1).pc := 0.U
  }

  io.stallReq := io.out.map(_.valid).reduce(_ || _) && (opIsLoad(io.ins(0).op) || opIsLoad(io.ins(1).op)) && !io.commit
}
