// See README.md for license details.

package cpu

import chisel3._
import _root_.common.{AXIIO, DebugBundle}
import cpu.cache.{DCacheAXIWrap, ICacheAXIWrap}
import cpu.core.Core_ls
import cpu.performance.CPUTopPerformanceIO

/**
  * instantiate the top level module of the CPU
  *
  * @param performanceMonitorEnable enable the performance monitor IO of the CPU
  */
class CPUTop(performanceMonitorEnable: Boolean = false) extends Module {
  val io = IO(new Bundle {

    /** hardware interrupt */
    val intr = Input(UInt(6.W))

    val axi = AXIIO.master()

    val debug = Output(new DebugBundle)

    val performance = if (performanceMonitorEnable) Some(new CPUTopPerformanceIO) else None
  })

  val axiInterface = Module(new AXIInterface)

  val iCache = Module(new ICacheAXIWrap(performanceMonitorEnable = performanceMonitorEnable))
  val dCache = Module(new DCacheAXIWrap)

  val core = Module(new Core_ls)

  // hook up the performance monitor wires
  if (performanceMonitorEnable) {
    io.performance.get.cache := iCache.io.performanceMonitorIO.get
  }

  core.io.intr := io.intr
  core.io.rInst <> iCache.io.rInst
  core.io.rData <> dCache.io.rData
  core.io.wData <> dCache.io.wData

  dCache.io.exeAddr := core.io_ls.ex_addr

  axiInterface.io.inst <> iCache.io.axi
  axiInterface.io.data <> dCache.io.axi

  io.axi <> axiInterface.io.bus
  io.debug <> core.io_ls.debug
}
