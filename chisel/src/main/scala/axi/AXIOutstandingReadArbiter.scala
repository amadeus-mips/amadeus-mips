package axi

import chisel3._
import chisel3.util._

/**
  * axi arbiter that arbitrates between ar and r channel
  * the priority should be connected manually
  */
class AXIOutstandingReadArbiter extends Module {
  val masterCount: Int = 3
  val io = IO(new Bundle {

    //VERI: assume master comply with axi requirements
    /** connect to axi ports in master, namely [[cpu.pipelinedCache.components.AXIPorts.AXIReadPort]],
      * [[cpu.pipelinedCache.components.AXIPorts.AXIWritePort]] and [[cpu.cache.UnCachedUnit]] */
    val fromMasters = Vec(masterCount, AXIIO.slave())

    // VERI: assert it's output meet the same requirements
    /** connect to axi bus */
    val toBus = AXIIO.master()
  })

  /** assumes every master it connects to has both a read port and a write port */
  val numOfChannels = masterCount

  /** as 2 masters ( uncached unit and dcache unit ) share ID, this queue must tell the difference between them */
  val readQueue = new Queue(UInt(log2Ceil(numOfChannels).W), 4, pipe = true, flow = true)

  val selectARNum = io.fromMasters
    .map { _.ar.valid }
    .indexOf(true.B)

  io.fromMasters := 0.U.asTypeOf(io.fromMasters)

  io.fromMasters(selectARNum).ar <> io.toBus.ar

}
