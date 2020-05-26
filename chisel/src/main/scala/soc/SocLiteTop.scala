//*************************************************************************
//   > File Name   : soc_top.v
//   > Description : SoC, included cpu, 2 x 3 bridge,
//                   inst ram, confreg, data ram
//
//           -------------------------
//           |           cpu         |
//           -------------------------
//                       | axi
//                       |
//             ---------------------
//             |    1 x 2 bridge   |
//             ---------------------
//                  |            |
//                  |            |
//             -----------   -----------
//             | axi ram |   | confreg |
//             -----------   -----------
//
//   > Author      : LOONGSON
//   > Date        : 2017-08-04
//*************************************************************************
package soc

/** Modified by Discreater */
import axi.{AXIArbiter, AXIInterconnect, AXIInterconnectConfig}
import chisel3._
import chisel3.util.ValidIO
import confreg.Confreg
import cpu.CPUTop
import cpu.performance.SocPerformanceIO
import ram.AXIRamRandomWrap
import shared.{DebugBundle, GPIO}

/**
  *
  */
class SocLiteTop(
  implicit socCfg: SocConfig
) extends Module {
  val io = IO(new Bundle {
    val gp    = new GPIO
    val uart  = new ValidIO(UInt(8.W))
    val debug = Output(new DebugBundle)

    val performance = if (socCfg.performanceMonitor) Some(new SocPerformanceIO) else None
  })

  val cpu = Module(new CPUTop(socCfg.performanceMonitor))

  /** 2x1 arbiter */
  val axiArbiter = Module(new AXIArbiter(2))

  /** 1x2 interconnect */
  val axiInterconnect = Module(new AXIInterconnect(AXIInterconnectConfig.loongson_func()))

  val confreg = Module(new Confreg(socCfg.simulation))
  val ram     = Module(new AXIRamRandomWrap())

  axiArbiter.io.slaves(0) <> cpu.io.dataAXI
  axiArbiter.io.slaves(1) <> cpu.io.instAXI

  axiInterconnect.io.slaves(0) <> axiArbiter.io.master

  axiInterconnect.io.masters(0) <> ram.io.axi
  axiInterconnect.io.masters(1) <> confreg.io.axi

  cpu.io.intr := 0.U // high active

  io.gp    <> confreg.io.gp
  io.uart  <> confreg.io.uart
  io.debug <> cpu.io.debug

  ram.io.ramRandomMask := confreg.io.ram_random_mask
  // the optional performance IO
  if (socCfg.performanceMonitor) {
    io.performance.get.cpu := cpu.io.performance.get
  }
}
