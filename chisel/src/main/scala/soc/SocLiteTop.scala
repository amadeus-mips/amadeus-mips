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
import axi.{AXIInterconnect, AXIInterconnectConfig}
import chisel3._
import chisel3.util.ValidIO
import confreg.Confreg
import cpu.{CPUConfig, CPUTop}
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

  implicit val cpuCfg = new CPUConfig(build = false)

  val cpu = Module(new CPUTop(socCfg.performanceMonitor))

  /** 2x2 interconnect */
  val axiInterconnect = Module(new AXIInterconnect(AXIInterconnectConfig.loongson_func()))
  val confreg         = Module(new Confreg(socCfg.simulation))
  val ram             = Module(new AXIRamRandomWrap())

  // the optional performance IO
  axiInterconnect.io.slaves(0) <> cpu.io.axi
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
