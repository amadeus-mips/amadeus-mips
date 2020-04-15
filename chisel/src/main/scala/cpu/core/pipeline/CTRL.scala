// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._
import shared.ValidBundle

class CTRL extends Module {
  val io = IO(new Bundle {
    val except = Input(Vec(exceptAmount, Bool()))
    val EPC = Input(UInt(dataLen.W))
    val stallReqFromFetch = Input(Bool())
    val stallReqFromDecode = Input(Bool())
    val stallReqFromExecute = Input(Bool())
    val stallReqFromMemory = Input(Bool())

    val flush = Output(Bool())
    val flushPC = Output(UInt(dataLen.W))
    val stall = Output(UInt(cpuStallLen.W))
  })

  /** Only external signal require stall will buffer the flush signal */
  val stalledByExternal = io.stallReqFromFetch || io.stallReqFromMemory

  val flushPC_tmp = Mux(io.except(EXCEPT_ERET), io.EPC, exceptPC)
  val hasExcept = io.except.asUInt() =/= 0.U

  /**
    * The buffer will be used when external stall finished.
    * If no external stall, we should handle the except immediately.
    * Otherwise, follow the below rules.
    * 1. We should use the first exception during the external stall, and ignore the later.
    * 2. We should always handle the hardware interrupt. (not fully implemented)
    * When rules conflict, observe the later rule.
    */
  val flushBuffer = RegInit(0.U.asTypeOf(new ValidBundle()))
  when(!stalledByExternal){
    // If no external stall, the except will be handled directly. Don't need to use buffer
    flushBuffer.valid := false.B
  }.otherwise{
    // If has external stall
    when(!hasExcept){
      // If no except, hold
    }.elsewhen(io.except(EXCEPT_INTR)){
      // If is hardware interrupt
      flushBuffer.valid := true.B
      flushBuffer.bits := flushPC_tmp
    }.elsewhen(!flushBuffer.valid){
      // Only handle the first exception signal. So if the buffer valid is high, ignore the exception signal
      flushBuffer.valid := true.B
      flushBuffer.bits := flushPC_tmp
    }.otherwise{
      // There is already an except signal. Ignore the later exception
    }
  }

  /**
    * If stalled by external, shouldn't flush. When the stalled finished, the buffer will keep a cycle.
    * So can use it.
    */
  io.flush := !stalledByExternal && (flushBuffer.valid || hasExcept)
  io.flushPC := Mux(flushBuffer.valid, flushBuffer.bits, flushPC_tmp)

  io.stall := MuxCase(0.U,
    Array(
      io.flush -> 0.U,
      (stalledByExternal && (flushBuffer.valid || hasExcept)) -> "b011111".U, // TODO
      io.stallReqFromMemory   -> "b011111".U,
      io.stallReqFromExecute  -> "b001111".U,
      io.stallReqFromDecode   -> "b000111".U,
      io.stallReqFromFetch    -> "b000111".U
    )
  )


}
