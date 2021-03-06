package memory.physicalMem

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType
import memory.axi.SimpleSramIO

/**
 * Base class for all modular backing memories. Simply declares the IO and the memory file.
 */
abstract class BaseDualPortedMemory(size: Int, memfile: String) extends Module {


val io = IO(new Bundle {
  val imem = new MemPortBusIO
  val dmem = new MemPortBusIO
})


def wireMemory (imem: BaseIMemPort, dmem: BaseDMemPort): Unit = {
  // Connect memory imem IO to dmem accessor
  this.io.imem.request <> imem.io.bus.request
  imem.io.bus.response <> this.io.imem.response
  this.io.dmem.request <> dmem.io.bus.request
  dmem.io.bus.response <> this.io.dmem.response
}

def wireMemAXIPort (imem : BaseIMemPortForAXI, dmem: BaseDMemPortForAXI): Unit = {
  this.io.imem.request <> imem.io.bus.request
  imem.io.bus.response <> this.io.imem.response
  this.io.dmem.request <> dmem.io.bus.request
  dmem.io.bus.response <> this.io.dmem.response
}

// Intentional DontCares:
// The connections between the ports and the backing memory, along with the
// ports internally assigning values to the, means that these DontCares
// should be completely 'overwritten' when the CPU is elaborated
io.imem.request <> DontCare
io.dmem.request <> DontCare
// Zero out response ports to 0, so that the pipeline does not receive any
// 'DontCare' values from the memory ports
io.imem.response <> 0.U.asTypeOf(Valid (new Response))
io.dmem.response <> 0.U.asTypeOf(Valid (new Response))

// change this to syncreadmem doesn't work with single cycle
// because the data will always be returned on the second cycle
// so essentially everything you see is a cycle late
// need some work on the interface
val physicalMem = Mem(math.ceil(size.toDouble/4).toInt, UInt(32.W))
// load hex mem
// notice: memory are encoded in Hex
// alternatively: memory can also be loaded in binary format
loadMemoryFromFile(physicalMem, memfile, MemoryLoadFileType.Hex)

}

/**
  * Base class for all instruction ports. Simply declares the IO.
  */
abstract class BaseIMemPort extends Module {
  val io = IO (new Bundle {
    val pipeline = new IMemPortIO
    val bus  = Flipped (new MemPortBusIO)
  })

  io.pipeline <> 0.U.asTypeOf (new IMemPortIO)
  // Intentional DontCare:
  // The connections between the ports and the backing memory, along with the
  // ports internally assigning values to the, means that these DontCares
  // should be completely 'overwritten' when the CPU is elaborated
  io.bus      <> DontCare
}

/**
  * Base class for all data ports. Simply declares the IO.
  */
abstract class BaseDMemPort extends Module {
  val io = IO (new Bundle {
    // pipeline taks to the actual pipeline
    val pipeline = new DMemPortIO
    // the bus talks to the backing memory
    val bus = Flipped (new MemPortBusIO)
  })

  io.pipeline <> 0.U.asTypeOf (new DMemPortIO)
  // Intentional DontCare:
  // The connections between the ports and the backing memory, along with the
  // ports internally assigning values to the, means that these DontCares
  // should be completely 'overwritten' when the CPU is elaborated
  io.bus      <> DontCare

  io.pipeline.good := io.bus.response.valid
}

abstract class BaseDMemPortForAXI extends Module {
  val io = IO(new Bundle {
    val axi = Flipped(new SimpleSramIO)
    val bus = Flipped(new MemPortBusIO)
  })

  // intential don't care
  io.axi <> 0.U.asTypeOf(new SimpleSramIO)
  io.bus <> DontCare
  // as this is a valid interface, no need for asserting ready
}

abstract class BaseIMemPortForAXI extends Module {
  val io = IO(new Bundle {
    val axi = Flipped(new SimpleSramIO)
    val bus = Flipped(new MemPortBusIO)
  })

  // intentional dontcare
  io.axi <> 0.U.asTypeOf(new SimpleSramIO)
  io.bus <> DontCare
}