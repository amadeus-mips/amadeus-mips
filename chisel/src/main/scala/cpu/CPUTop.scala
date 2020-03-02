// See README.md for license details.

package cpu

import chisel3._
import _root_.common.{AXIMasterIO, DebugBundle}
import cpu.cache.{DCacheAXIWrap, ICacheAXIWrap}
import cpu.core.Core_ls

class CPUTop extends Module {
  val io = IO(new Bundle {

    /** hardware interrupt */
    val intr = Input(UInt(6.W))

    val bus_axi = new AXIMasterIO()

    val debug = Output(new DebugBundle)
  })

  val axiInterface = Module(new AXIInterface)

  val iCache = Module(new ICacheAXIWrap)
  val dCache = Module(new DCacheAXIWrap)

  val core = Module(new Core_ls)

  core.io.intr := io.intr
  core.io.rInst <> iCache.io.rInst
  core.io.rData <> dCache.io.rData
  core.io.wData <> dCache.io.wData

  iCache.io.flush := core.io_ls.flush

  dCache.io.exeAddr := core.io_ls.ex_addr

  axiInterface.io.inst <> iCache.io.axi
  axiInterface.io.data <> dCache.io.axi
  axiInterface.io.flush := core.io_ls.flush

  io.bus_axi <> axiInterface.io.bus
  io.debug <> core.io_ls.debug
}
