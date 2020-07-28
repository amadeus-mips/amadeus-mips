package cpu.core.pipeline

import chisel3._
import chisel3.util.{Cat, MuxCase}
import cpu.core.Constants._
import cpu.core.bundles.stages.{Mem0Mem1Bundle, Mem1Mem2Bundle}
import cpu.core.components.ExceptionHandleBundle

class Memory1Top extends Module {
  val io = IO(new Bundle() {
    val in     = Input(new Mem0Mem1Bundle)
    val commit = Input(Bool())

    val exceptionCP0 = Input(new ExceptionHandleBundle)

    val out            = Output(new Mem1Mem2Bundle)
    val stallReq       = Output(Bool())
    val exceptJumpAddr = Output(UInt(addrLen.W))
  })

  val isInterruptExcept = io.in.except(EXCEPT_INTR)
  val isRefillExcept = io.in.except(EXCEPT_INST_TLB_REFILL) || io.in.except(EXCEPT_DATA_TLB_W_REFILL) || io.in.except(
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

  io.out.addrL2   := io.in.addrL2
  io.out.op       := io.in.op
  io.out.write    := io.in.write
  io.out.pc       := io.in.pc
  io.out.uncached := io.in.uncached
  io.out.valid    := !io.in.except.asUInt().orR()

  when(!io.out.valid){
    io.out.pc := 0.U
  }

  io.stallReq := io.out.valid && (opIsLoad(io.in.op) || opIsStore(io.in.op)) && !io.commit
}
