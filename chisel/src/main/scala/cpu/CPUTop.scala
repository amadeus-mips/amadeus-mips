// See README.md for license details.

package cpu

import axi.{AXIArbiter, AXIIO}
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util.Cat
import cpu.cache.{ICache, UnCachedUnit, newDCache}
import cpu.core.Core_ls
import cpu.mmu.FullTLB
import cpu.performance.CPUTopPerformanceIO
import firrtl.options.TargetDirAnnotation
import shared.DebugBundle

/**
  * instantiate the top level module of the CPU
  *
  * @param performanceMonitorEnable enable the performance monitor IO of the CPU
  */
class CPUTop(performanceMonitorEnable: Boolean = false)(implicit conf: CPUConfig = CPUConfig.Build) extends Module {
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

  val tlb = Module(new FullTLB(numOfReadPorts = 2, TLBSize = conf.tlbSize))

  val core = Module(new Core_ls)

  // hook up the performance monitor wires
  if (performanceMonitorEnable) {
    io.performance.get.cache := iCache.io.performanceMonitorIO.get
  }

  core.io.tlb.readResp := tlb.io.readResp
  core.io.tlb.probeResp := tlb.io.probeResp

  tlb.io.asid := core.io.tlb.asid
  tlb.io.kseg0Uncached := core.io.tlb.kseg0Uncached
  tlb.io.instrReq := core.io.tlb.instrReq
  tlb.io.probeReq := core.io.tlb.probeReq

  tlb.io.query(0).vAddr := core.io.rInst.addr(31, 12)
  tlb.io.query(1).vAddr := core.io.rChannel.addr(31,12)
//  assert(!core.io.rInst.enable || !tlb.io.result(0).mapped || tlb.io.result(0).hit, s"${core.io.rInst.addr.litValue()}")
  assert(!(core.io.rChannel.enable&&core.io.wChannel.enable) || !tlb.io.result(1).mapped || tlb.io.result(1).hit, s"${core.io.rChannel.addr}")

  core.io.intr := io.intr
  // assume instructions are always cached
  core.io.rInst <> iCache.io.rInst
  iCache.io.rInst.addr := Cat(tlb.io.result(0).pageInfo.pfn, core.io.rInst.addr(11, 0))

  // buffer the read data
  // write doesn't have this problem because write valid is asserted
  // in the same cycle
  val useDCache = RegInit(true.B)

  when(!tlb.io.result(1).uncached) {
    when(dCache.io.rChannel.valid) {
      useDCache := true.B
    }
  }.otherwise {
    when(unCached.io.rChannel.valid) {
      useDCache := false.B
    }
  }
  when(!tlb.io.result(1).uncached) {
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
  val dataPhyAddr = Cat(tlb.io.result(1).pageInfo.pfn, core.io.rChannel.addr(11, 0))
  dCache.io.rChannel.addr := dataPhyAddr
  unCached.io.rChannel.addr := dataPhyAddr
  dCache.io.wChannel.addr := dataPhyAddr
  unCached.io.wChannel.addr := dataPhyAddr

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

object elaborateCPU extends App {
  implicit val cpuCfg = new CPUConfig(build = true)
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new CPUTop()),
      TargetDirAnnotation("generation"))
  )
}