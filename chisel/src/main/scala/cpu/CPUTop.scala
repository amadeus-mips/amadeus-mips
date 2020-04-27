// See README.md for license details.

package cpu

import chisel3._
import cpu.cache.{DCacheAXIWrap, ICacheAXIWrap}
import cpu.core.Core_ls
import cpu.performance.CPUTopPerformanceIO
import shared.{AXIIO, DebugBundle}

/**
  * instantiate the top level module of the CPU
  *
  * @param performanceMonitorEnable enable the performance monitor IO of the CPU
  */
class CPUTop(performanceMonitorEnable: Boolean = false) extends Module {
  val io = IO(new Bundle {

    /** hardware interrupt */
    val intr = Input(UInt(6.W))

    val instAXI = AXIIO.master()
    val dataAXI = AXIIO.master()

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

  axiInterface.io.data <> dCache.io.axi

  iCache.io.axi := DontCare
  io.dataAXI <> axiInterface.io.bus
  io.instAXI <> iCache.io.axi

  io.debug <> core.io_ls.debug
}
