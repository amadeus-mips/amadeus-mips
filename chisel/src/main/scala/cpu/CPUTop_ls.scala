package cpu

import axi.{AXIArbiter, AXIIO}
import chisel3._
import cpu.cache.{DCacheAXIWrap, ICache}
import cpu.core.Core_ls
import shared.DebugBundle


class CPUTop_ls extends Module{
  val io = IO(new Bundle() {
    /** hardware interrupt */
    val intr = Input(UInt(6.W))

    val axi = AXIIO.master()

    val debug = Output(new DebugBundle)
  })

  val axiInterface = Module(new AXIInterface)

  val iCache = Module(new ICache())
  val dCache = Module(new DCacheAXIWrap)

  val core = Module(new Core_ls)

  core.io.intr := io.intr
  core.io.rInst <> iCache.io.rInst
  core.io.rChannel <> dCache.io.rData
  core.io.wChannel <> dCache.io.wData

  dCache.io.exeAddr := core.io_ls.ex_addr

  axiInterface.io.data <> dCache.io.axi

  val axiArbiter = Module(new AXIArbiter())
  axiArbiter.io.slaves(0) <> iCache.io.axi
  axiArbiter.io.slaves(1) <> axiInterface.io.bus

  io.axi <> axiArbiter.io.master

  io.debug := core.io_ls.debug
}
