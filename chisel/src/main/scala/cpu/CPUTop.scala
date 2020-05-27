// See README.md for license details.

package cpu

import axi.{AXIArbiter, AXIIO}
import chisel3._
import cpu.cache.{ICache, UnCachedUnit, newDCache}
import cpu.core.Core_ls
import cpu.performance.CPUTopPerformanceIO
import shared.DebugBundle

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

  val axiArbiter = Module(new AXIArbiter())

  val iCache   = Module(new ICache(performanceMonitorEnable = performanceMonitorEnable))
  val dCache   = Module(new newDCache)
  val unCached = Module(new UnCachedUnit)

  val core = Module(new Core_ls)

  // hook up the performance monitor wires
  if (performanceMonitorEnable) {
    io.performance.get.cache := iCache.io.performanceMonitorIO.get
  }
  core.io.intr := io.intr
  // assume instructions are always cached
  core.io.rInst <> iCache.io.rInst

  // buffer the read data
  // write doesn't have this problem because write valid is asserted
  // in the same cycle
  val useDCache = RegInit(true.B)

  when(!isUnCached(core.io.rChannel.addr)) {
    when(dCache.io.rChannel.valid) {
      useDCache := true.B
    }
  }.otherwise {
    when(unCached.io.rChannel.valid) {
      useDCache := false.B
    }
  }
  when(!isUnCached(core.io.rChannel.addr)) {
    core.io.rChannel            <> dCache.io.rChannel
    core.io.wChannel            <> dCache.io.wChannel
    unCached.io                 <> DontCare
    unCached.io.rChannel.enable := false.B
    unCached.io.wChannel.enable := false.B
  }.otherwise {
    core.io.rChannel          <> unCached.io.rChannel
    core.io.wChannel          <> unCached.io.wChannel
    dCache.io                 <> DontCare
    dCache.io.rChannel.enable := false.B
    dCache.io.wChannel.enable := false.B
  }
  core.io.rChannel.data := Mux(useDCache, dCache.io.rChannel.data, unCached.io.rChannel.data)

  iCache.io.axi  := DontCare
  axiArbiter.io.slaves(0) <> dCache.io.axi
  axiArbiter.io.slaves(1) <> unCached.io.axi
  axiArbiter.io.slaves(2) <> iCache.io.axi

  io.axi <> axiArbiter.io.master

  io.debug <> core.io_ls.debug

  def isUnCached(addr: UInt): Bool = {
    require(addr.getWidth == 32)
    addr(31, 29) === "b101".U(3.W)
  }
}
