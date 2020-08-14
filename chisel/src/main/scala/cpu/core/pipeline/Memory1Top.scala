package cpu.core.pipeline

import chisel3._
import chisel3.util.{Cat, MuxCase}
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.stages.{Mem0Mem1Bundle, Mem1Mem2Bundle}
import cpu.core.bundles.{CPBundle, HILOValidBundle, TLBReadBundle}
import cpu.core.components.ExceptionHandleBundle

class Memory1Top(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle() {
    val ins    = Input(Vec(conf.decodeWidth, new Mem0Mem1Bundle))
    val dcacheCommit = Input(Bool())
    val uncacheCommit = Input(Bool())

    val exceptionCP0 = Input(new ExceptionHandleBundle)

    val out      = Output(Vec(conf.decodeWidth, new Mem1Mem2Bundle))
    val stallReq = Output(Bool())

    // hilo write
    val hiloWrite   = Output(Vec(conf.decodeWidth, new HILOValidBundle))

    // cp0 write
    val cp0Write = Output(new CPBundle)
    val op       = Output(UInt(opLen.W))
    val tlbWrite = Output(new TLBReadBundle)

    // exception handler
    val except      = Output(Vec(exceptAmount, Bool()))
    val inDelaySlot = Output(Bool())
    val pc          = Output(UInt(addrLen.W))
    val badAddr     = Output(UInt(addrLen.W))

    val exceptJumpAddr = Output(UInt(addrLen.W))
  })
  val exceptSlot = Mux(io.ins(0).except.reduce(_ || _), 0.U, 1.U)
  val c0Slot     = Mux(opIsC0Write(io.ins(0).op), 0.U, 1.U)

  val except    = Mux(exceptSlot === 0.U, io.ins(0).except, io.ins(1).except)
  val hasExcept = except.reduce(_ || _)

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

  io.hiloWrite := io.ins.map(_.hiloWrite)

  when(!io.out(0).valid) {
    io.out(0).pc := 0.U
    io.hiloWrite(0).hi.valid := false.B
    io.hiloWrite(0).lo.valid := false.B
  }
  when(!io.out(1).valid) {
    io.out(1).pc := 0.U
    io.hiloWrite(1).hi.valid := false.B
    io.hiloWrite(1).lo.valid := false.B
  }

  io.cp0Write := io.ins(c0Slot).cp0Write
  io.op       := io.ins(c0Slot).op
  io.tlbWrite := io.ins(c0Slot).tlbWrite
  when(hasExcept) {
    io.cp0Write.enable := false.B
    io.op              := 0.U
  }

  io.except      := except
  io.inDelaySlot := io.ins(exceptSlot).inDelaySlot
  io.pc          := io.ins(exceptSlot).pc
  io.badAddr     := io.ins(exceptSlot).badAddr

  io.stallReq := !io.commit &&
    io.out.map(_.valid).zip(io.ins.map(_.op)).map { case (valid, op) => valid && opIsLoad(op) }.reduce(_ || _)
  /** 0 means uncached, 1 means cached */
  val commitBuffer = RegInit(0.U)
  val commitBufferValid = RegInit(false.B)

  when(io.out.valid && opIsLoad(io.in.op)) {
    when(io.in.uncached){
      when(io.dcacheCommit){
        commitBuffer := 1.U
        commitBufferValid := true.B
      }
      when(commitBufferValid && commitBuffer === 0.U){
        commitBufferValid := false.B
      }
      assert(!(io.dcacheCommit && (commitBufferValid && commitBuffer === 0.U)))
    }.otherwise{
      when(io.uncacheCommit) {
        commitBuffer := 0.U
        commitBufferValid := true.B
      }
      when(commitBufferValid && commitBuffer === 1.U) {
        commitBufferValid := false.B
      }
      assert(!(io.uncacheCommit && (commitBufferValid && commitBuffer === 1.U)))
    }
  }

  val committed = Mux(
    io.in.uncached,
    commitBufferValid && commitBuffer === 0.U || io.uncacheCommit,
    commitBufferValid && commitBuffer === 1.U || io.dcacheCommit
  )

  io.stallReq := io.out.valid && opIsLoad(io.in.op) && !committed
}
