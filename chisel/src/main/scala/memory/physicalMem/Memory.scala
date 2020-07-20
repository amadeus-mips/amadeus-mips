package memory.physicalMem

import chisel3._

/**
 * This is the actual memory. You should never directly use this in the CPU.
 * This module should only be instantiated in the Top file.
 *
 * The I/O for this module is defined in [[MemPortBusIO]].
 */
import memory.physicalMem.MemOperations._

class DualPortedCombinMemory(size: Int, memfile: String) extends BaseDualPortedMemory(size, memfile) {
  def wireMemPipe(portio: MemPortBusIO): Unit = {
    portio.response.valid := false.B
    // Combinational memory is inherently always ready for port requests
    portio.request.ready := true.B
  }

  // Instruction port

  wireMemPipe(io.imem)

  when (io.imem.request.valid) {
    // Put the Request into the instruction pipe and signal that instruction memory is busy
    val request = io.imem.request.bits

    // We should only be expecting a read from instruction memory
    assert(request.operation === Read)
    // Check that request is pointing to a valid location in memory

    // TODO: Revert this back to the assert form "assert (request.request < size.U)"
    // TODO: once CSR is integrated into CPU
    io.imem.response.valid := true.B
    io.imem.response.bits.data := physicalMem.read((request.address(19, 0) >> 2).asUInt())
  } .otherwise {
    io.imem.response.valid := false.B
  }

  // Data port

  wireMemPipe(io.dmem)

  val memAddress =  io.dmem.request.bits.address(19, 0)
  val memWriteData = io.dmem.request.bits.writedata

  when (io.dmem.request.valid) {
    val request = io.dmem.request.bits

    // Check that non-combin write isn't being used
    assert (request.operation =/= Write)
    // Check that request is pointing to a valid location in memory
    // assert (request.request < size.U)

    // Read path
    //TODO: read request is byte aligned, acutal reference request for mem is word aligned
    io.dmem.response.bits.data := physicalMem.read((memAddress >> 2).asUInt())
    io.dmem.response.valid := true.B

    // Write path
    when (request.operation === ReadWrite) {
      physicalMem.write((memAddress >> 2).asUInt(), memWriteData)
    }
  } .otherwise {
    io.dmem.response.valid := false.B
  }
}