package soc

import axi.{AXIInterconnect, AXIInterconnectConfig}
import chisel3._
import chisel3.util.{Cat, Decoupled, Valid}
import confreg.Confreg
import cpu.{CPUConfig, CPUTop}
import ram.AXIRam
import shared.{DebugBundle, GPIO}
import uart.MyUART

class SocUpTop(implicit cfg: SocUpTopConfig) extends Module {
  val io = IO(new Bundle {
    val gp    = new GPIO
    val debug = Output(new DebugBundle)
    val uart = new Bundle() {
      val in  = Flipped(Decoupled(UInt(8.W)))
      val out = Valid(UInt(8.W))
    }
  })

  implicit val cpuConfig = new CPUConfig(build = false, memoryFile = cfg.memFile, compareRamDirectly = false)

  val cpu = Module(new CPUTop())

  /** 1*5 */
  val axiInterconnect = Module(new AXIInterconnect(AXIInterconnectConfig.loongson_system()))

  val confreg = Module(new Confreg())
  val ddr3    = Module(new AXIRam(memFile = None, addrLen = 27))
  val flash   = Module(new AXIRam(memFile = Some(cfg.memFile), bin = true, addrLen = 20))
  val uart    = Module(new MyUART())

  axiInterconnect.io.slaves(0)  <> cpu.io.axi
  axiInterconnect.io.masters(0) <> ddr3.io.axi
  axiInterconnect.io.masters(1) <> flash.io.axi
  axiInterconnect.io.masters(2) <> uart.io.axi
  axiInterconnect.io.masters(3) <> confreg.io.axi
  axiInterconnect.io.masters(4) := DontCare // MAC, unused

  cpu.io.intr := Cat(false.B, false.B, false.B, uart.io.interrupt, false.B, false.B)

  io.gp       <> confreg.io.gp
  io.debug    := cpu.io.debug
  io.uart.in  <> uart.io.inputData
  io.uart.out := uart.io.outputData
}
