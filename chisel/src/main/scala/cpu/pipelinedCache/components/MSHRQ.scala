package cpu.pipelinedCache.components

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

//TODO: flush to avoid writing back not necessary item
class MSHRQ(capacity: Int = 4)(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** record when there is a new miss, and when it could handle it */
    val recordMiss = Flipped(Decoupled(new MSHREntry()))

    /** extract miss information for ar port
      * this is the address to put into [[AddressQueryQueue]] */
    val extractMiss = Decoupled(new MSHREntry())

    /** there are pending un-handled miss */
    val pendingMiss = Output(Bool())
  })
  // There is no need to search MSHR queue. A miss is never served before an entry is mshr is flushed.

  /** waiting for handshake queue */
  val missQueue = Module(new Queue(new MSHREntry(), 1, false, true))

  io.recordMiss  <> missQueue.io.enq
  io.extractMiss <> missQueue.io.deq

  io.pendingMiss := missQueue.io.count === 0.U
}
