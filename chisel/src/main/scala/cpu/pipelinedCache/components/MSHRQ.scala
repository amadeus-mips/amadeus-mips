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
    val extractMiss = Output(new MSHREntry())

    /** address information for write back into banks
      * this is the first miss address to handle */
    val writeBackInfo = Decoupled(new MSHREntry())

    /** ar complete denotes that the head of miss Queue has completed ar transfer and about to enter
      * r transfer */
    val arComplete = Input(Bool())

    /** there are pending un-handled miss */
    val pendingMiss = Output(Bool())
  })
  // There is no need to search MSHR queue. A miss is never served before an entry is mshr is flushed.

  /** waiting for handshake queue */
  val missQueue = Module(new Queue(new MSHREntry(), 1, false, true))

  /** ready for write back queue */
  val readyQueue = Module(new Queue(new MSHREntry(), 2, true, true))

  io.recordMiss            <> missQueue.io.enq
  missQueue.io.deq.ready   := readyQueue.io.enq.ready && io.arComplete
  io.extractMiss.tag       := missQueue.io.deq.bits.tag
  io.extractMiss.index     := missQueue.io.deq.bits.index
  io.extractMiss.bankIndex := missQueue.io.deq.bits.bankIndex
  readyQueue.io.enq.valid  := missQueue.io.deq.valid && io.arComplete
  readyQueue.io.enq.bits   := missQueue.io.deq.bits
  io.writeBackInfo         <> readyQueue.io.deq

  io.pendingMiss := missQueue.io.count === 0.U && readyQueue.io.count === 0.U
}
