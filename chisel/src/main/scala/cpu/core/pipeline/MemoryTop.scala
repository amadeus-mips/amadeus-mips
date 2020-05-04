// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import cpu.core.Constants._
import cpu.core.bundles.CPBundle
import cpu.core.bundles.stage5.{ExeMemBundle, MemWbBundle}
import cpu.core.memory.{CP0HandleBundle, Control, Except, Forward}

class MemoryTop extends Module {
  val io = IO(new Bundle {
    val in = Input(new ExeMemBundle)

    val inCP0Handle = Input(new CP0HandleBundle)
    val wbCP0 = Input(new CPBundle)

    /** load data from memory */
    val rData = new NiseSramReadIO

    /** save data to memory */
    val wData = new NiseSramWriteIO

    val out = Output(new MemWbBundle)
    val badAddr = Output(UInt(addrLen.W))
    val EPC = Output(UInt(dataLen.W))
    val inDelaySlot = Output(Bool())
    val except = Output(Vec(exceptAmount, Bool()))
    val stallReq = Output(Bool())
  })

  val control = Module(new cpu.core.memory.Control)
  val except = Module(new cpu.core.memory.Except)

  val forward = Module(new cpu.core.memory.Forward)

  control.io.inWriteData := io.in.write.data
  control.io.inMemData   := io.in.memData
  control.io.operation   := io.in.operation
  control.io.addr        := io.in.memAddr
  control.io.except      := except.io.outExcept.asUInt() =/= 0.U
  control.io.rData       <> io.rData
  control.io.wData       <> io.wData

  except.io.pc        := io.in.pc
  except.io.addr      := io.in.memAddr
  except.io.cp0Status := forward.io.outCP0.status
  except.io.cp0Cause  := forward.io.outCP0.cause
  except.io.inExcept  <> io.in.except

  forward.io.inCP0 <> io.inCP0Handle
  forward.io.wbCP0 <> io.wbCP0

  io.out.write       <> io.in.write
  io.out.write.data  := control.io.outWriteData
  io.out.write.valid := !control.io.stallReq
  io.out.cp0         <> io.in.cp0
  io.out.hilo        <> io.in.hilo
  io.out.pc          := io.in.pc

  io.badAddr     := except.io.badAddr
  io.inDelaySlot := io.in.inDelaySlot
  io.except      <> except.io.outExcept
  io.EPC         := forward.io.outCP0.EPC
  io.stallReq    := control.io.stallReq
}
