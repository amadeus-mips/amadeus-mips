// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._

class Hazard extends Module {
  val io = IO(new Bundle {
    val except = Input(Vec(exceptAmount, Bool()))
    val EPC    = Input(UInt(dataLen.W))

    val predictFail = Input(Bool())
    val waitingDS   = Input(Bool())

    val stallReqFromIf0 = Input(Bool())
    val stallReqFromIf1 = Input(Bool())

    val stallReqFromId  = Input(Bool())
    val stallReqFromExe = Input(Bool())
    val stallReqFromMem = Input(Bool())

    val stallIf0     = Output(Bool())
    val stallIf1     = Output(Bool())
    val fifoDeqReady = Output(Bool())
    val stallExe     = Output(Bool())
    val stallMem     = Output(Bool())

    val flushAll = Output(Bool())
    val flushPC  = Output(UInt(dataLen.W))

    val flushIf1  = Output(Bool())
    val flushFIFO = Output(Bool())
    val flushExe  = Output(Bool())
    val flushMem  = Output(Bool())
    val flushWb   = Output(Bool())

    val branchValid = Output(Bool())
  })

  val hasExcept = io.except.asUInt().orR()

  io.flushAll := hasExcept
  io.flushPC := MuxCase(
    generalExceptPC,
    Seq(
      io.except(EXCEPT_ERET) -> io.EPC,
      (io.except(EXCEPT_INST_TLB_REFILL) || io.except(EXCEPT_DATA_TLB_R_REFILL) ||
        io.except(EXCEPT_DATA_TLB_W_REFILL)) -> tlbRefillExceptPC
    )
  )

  val branchValid = !io.stallExe && io.predictFail

  // stalled because inst cache has not accept pc yet, pc won't change
  io.stallIf0 := io.stallReqFromIf0
  // if pc is stalled, and data in fetch1 has fired; Or prediction failed
  io.flushIf1 := io.stallReqFromIf0 && !io.stallReqFromIf1 || branchValid
  // if data in fetch1 hasn't fired
  io.stallIf1 := io.stallReqFromIf1

  // prediction failed
  io.flushFIFO := branchValid

  io.fifoDeqReady := !io.stallReqFromId && !io.stallReqFromExe && !io.stallReqFromMem

  io.flushExe := io.stallReqFromId && !io.stallReqFromExe && !io.stallReqFromMem && !io.waitingDS
  io.stallExe := io.stallReqFromExe || io.stallReqFromMem || io.waitingDS

  io.flushMem := (io.stallReqFromExe || io.waitingDS) && !io.stallReqFromMem
  io.stallMem := io.stallReqFromMem

  io.flushWb := io.stallReqFromMem

  io.branchValid := branchValid
}
