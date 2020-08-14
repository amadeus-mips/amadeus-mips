// See README.md for license details.

package cpu

import axi.{AXIIO, AXIOutstandingReadArbiter, AXIOutstandingWriteArbiter}
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import cpu.cache.{UnCachedUnit, UncachedQueue}
import cpu.core.Core_ls
import cpu.mmu.{FakeMMU, MMU}
import cpu.pipelinedCache.{DataCache, InstrCache}
import firrtl.options.TargetDirAnnotation
import shared.DebugBundle

/**
  * instantiate the top level module of the CPU
  *
  * @param performanceMonitorEnable enable the performance monitor IO of the CPU
  */
//noinspection DuplicatedCode
class CPUTop(performanceMonitorEnable: Boolean = false)(implicit conf: CPUConfig = CPUConfig.Build) extends Module {
  val io = IO(new Bundle {

    /** hardware interrupt */
    val intr  = Input(UInt(6.W))
    val axi   = AXIIO.master()
    val debug = Output(Vec(2, new DebugBundle))
  })
//  val arbiter = Module(new AXIArbiter(3))
  val readArbiter  = Module(new AXIOutstandingReadArbiter)
  val writeArbiter = Module(new AXIOutstandingWriteArbiter)

  val iCache   = Module(new InstrCache(conf.iCacheConf))
  val dCache   = Module(new DataCache(conf.iCacheConf))
//  val unCached = Module(new UnCachedUnit)
  val unCached = Module(new UncachedQueue())

  val mmu = Module(if (conf.enableTLB) new MMU else new FakeMMU)

  val core = Module(new Core_ls)

  core.io.intr := io.intr

  core.io.tlb <> mmu.io.core

  mmu.io.in.rInst  <> core.io.rInst
  mmu.io.in.memReq := core.io.memAccess.request.bits

  // assume instructions are always cached
  iCache.io.fetchIO.addr    <> mmu.io.out.rInst.addr
  iCache.io.fetchIO.data    <> mmu.io.out.rInst.data
  iCache.io.fetchIO.change  := mmu.io.out.rInst.change
  iCache.io.invalidateIndex <> core.io.iCacheInvalidate

  dCache.io.cacheInstruction <> core.io.dCacheInvalidate

  when(!mmu.io.dataUncached) {
    dCache.io.request.valid   := core.io.memAccess.request.valid && unCached.io.request.ready
    dCache.io.request.bits    := mmu.io.out.memReq
    unCached.io.request       := DontCare
    unCached.io.request.valid := false.B
  }.otherwise {
    unCached.io.request.valid := core.io.memAccess.request.valid && dCache.io.request.ready
    unCached.io.request.bits  := mmu.io.out.memReq
    dCache.io.request         := DontCare
    dCache.io.request.valid   := false.B
  }
  core.io.memAccess.request.ready := dCache.io.request.ready && unCached.io.request.ready
  core.io.memAccess.dcacheCommit  := dCache.io.commit
  core.io.memAccess.uncacheCommit := unCached.io.commit
  core.io.memAccess.cachedData    := dCache.io.readData
  core.io.memAccess.uncachedData  := unCached.io.readData
  core.io.memAccess.uncached      := mmu.io.dataUncached

//  arbiter.io.slaves(0) <> dCache.io.axi
//  arbiter.io.slaves(1) <> unCached.io.axi
//  arbiter.io.slaves(2) <> iCache.io.axi
  iCache.io.axi   := DontCare
  readArbiter.io  := DontCare
  writeArbiter.io := DontCare

//  io.axi <> arbiter.io.master
  readArbiter.io.fromMasters(0).ar  <> iCache.io.axi.ar
  readArbiter.io.fromMasters(0).r   <> iCache.io.axi.r
  readArbiter.io.fromMasters(1).ar  <> dCache.io.axi.ar
  readArbiter.io.fromMasters(1).r   <> dCache.io.axi.r
  readArbiter.io.fromMasters(2).ar  <> unCached.io.axi.ar
  readArbiter.io.fromMasters(2).r   <> unCached.io.axi.r
  writeArbiter.io.fromMasters(0).aw <> dCache.io.axi.aw
  writeArbiter.io.fromMasters(0).w  <> dCache.io.axi.w
  writeArbiter.io.fromMasters(0).b  <> dCache.io.axi.b
  writeArbiter.io.fromMasters(1).aw <> unCached.io.axi.aw
  writeArbiter.io.fromMasters(1).w  <> unCached.io.axi.w
  writeArbiter.io.fromMasters(1).b  <> unCached.io.axi.b

  io.axi.ar <> readArbiter.io.toBus.ar
  io.axi.r  <> readArbiter.io.toBus.r
  io.axi.aw <> writeArbiter.io.toBus.aw
  io.axi.w  <> writeArbiter.io.toBus.w
  io.axi.b  <> writeArbiter.io.toBus.b

  io.debug := core.io_ls.debug

}

object elaborateCPU extends App {
  implicit val cpuCfg = CPUConfig(build = true)
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new CPUTop()), TargetDirAnnotation("generation"))
  )
}
