package cpu

import chisel3._
import memory.axi.AXI1x2SramInterface
import memory.physicalMem.{DMemCombinationalPortForAXI, DualPortedCombinMemory, IMemCombinationalPortForAXI}

class TestTop(memFile: String) extends Module {
  val io = IO(new Bundle() {
    val success = Output(Bool())
  })
  io.success := DontCare

  // directory structure for sbt run is at the root directory of the project
  // in this case: ./ = chisel/
  //  val memFile = s"./src/main/scala/zero.txt"

  val cpu = Module(new CPUTop)
  val mem = Module(new DualPortedCombinMemory(1 << 18, memFile))
  val imem = Module(new IMemCombinationalPortForAXI)
  val dmem = Module(new DMemCombinationalPortForAXI)
  mem.wireMemAXIPort(imem, dmem)
  val memAXISlave = Module(new AXI1x2SramInterface)

  memAXISlave.io.dram <> dmem.io.axi
  memAXISlave.io.iram <> imem.io.axi
  memAXISlave.io.bus <> cpu.io.axi

  cpu.io.intr := 0.U(6.W)
  cpu.io.debug := DontCare
}
