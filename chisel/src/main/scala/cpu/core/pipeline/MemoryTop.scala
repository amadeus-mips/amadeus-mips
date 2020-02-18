// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.Cat
import cpu.common.{DataReadIO, DataWriteIO}
import cpu.core.Constants._
import cpu.core.bundles.CPBundle
import cpu.core.bundles.stage.{EXEMEMBundle, MEMWBBundle}
import cpu.core.memory.{CP0HandleBundle, Control, Except, Forward}

class MemoryTop extends Module {
  val io = IO(new Bundle {
    val in = Input(new EXEMEMBundle)

    val inCP0Handle = Input(new CP0HandleBundle)
    val wbCP0 = Input(new CPBundle)

    /** load data from memory */
    val load = new DataReadIO
    /** save data to memory */
    val store = new DataWriteIO
    /** address to memory */
    val addr = Output(UInt(dataLen.W))

    val out = Output(new MEMWBBundle)
    val badAddr = Output(UInt(addrLen.W))
    val EPC = Output(UInt(dataLen.W))
    val inDelaySlot = Output(Bool())
    val except = Output(Vec(exceptAmount, Bool()))
    val stallReq = Output(Bool())
  })

  val control = Module(new Control)
  val except = Module(new Except)

  val forward = Module(new Forward)

  control.io.inWriteData := io.in.write.data
  control.io.inMemData := io.in.memData
  control.io.operation := io.in.operation
  control.io.addrL2 := io.in.memAddr(1, 0)
  control.io.except := except.io.outExcept.asUInt() =/= 0.U
  control.io.load <> io.load
  control.io.store <> io.store

  except.io.pc := io.in.pc
  except.io.addr := io.in.memAddr
  except.io.cp0Status := forward.io.outCP0.status
  except.io.cp0Cause := forward.io.outCP0.cause
  except.io.inExcept <> io.in.except

  forward.io.inCP0 <> io.inCP0Handle
  forward.io.wbCP0 <> io.wbCP0

  io.addr := Cat(io.in.memAddr(dataLen-1, 2), 0.U(2.W))

  io.out.write.control <> io.in.write.control
  io.out.write.data := control.io.outWriteData
  io.out.cp0 <> io.in.cp0
  io.out.hilo <> io.in.hilo
  io.out.pc := io.in.pc

  io.badAddr := except.io.badAddr
  io.inDelaySlot := io.in.inDelaySlot
  io.except <> except.io.outExcept
  io.EPC := forward.io.outCP0.EPC
  io.stallReq := control.io.stallReq
}
