// See README.md for license details.
package memory

import chisel3._
import memory.axi.AXI1x2SramInterface
import memory.physicalMem.{DMemCombinationalPortForAXI, DualPortedCombinMemory, IMemCombinationalPortForAXI}
import _root_.axi.AXIIO

class memoryAXIWrap(memFile: String) extends Module {
  val io = IO(new Bundle {
    val axi = AXIIO.slave()
  })
  val mem = Module(new DualPortedCombinMemory(1 << 20, memFile))
  val imem = Module(new IMemCombinationalPortForAXI)
  val dmem = Module(new DMemCombinationalPortForAXI)
  mem.wireMemAXIPort(imem, dmem)
  val axiInterface = Module(new AXI1x2SramInterface)

  axiInterface.io.dram <> dmem.io.axi
  axiInterface.io.iram <> imem.io.axi
  axiInterface.io.bus <> io.axi
}
