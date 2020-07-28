package cpu.pipelinedCache.components

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

//TODO: add prefetch depth into cache config
//TODO: flush the whole address generator
class PrefetchAddressGenerator(prefetchDepth: Int = 4)(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** when there is new hit in refill buffer or a new miss, consult this address
      * if the address is the tail of the queue, predict to prefetch the next line
      * otherwise, prefetch the prefetch depth number of lines */
    val newPrefetchRequest = Flipped(Valid(new MSHREntry()))

    val continuePrefetch = Flipped(Decoupled(new MSHREntry()))

    /** valid denotes if this prefetch request is true
      * bank index is always 0 in this address */
    val nextFetchAddress = Decoupled(new MSHREntry())
  })

  //TODO: make sure it does not prefetch the same location multiple times
  /** when prefetch length > 1, use a counter to continue to prefetch */
  val prefetchCounter = RegInit(0.U((log2Ceil(prefetchDepth) + 1).W))

  val prefetchBaseAddress = Reg(new MSHREntry)

  /** prefetch address queue before AR handshake */
  val toPrefetchAddressQueue = Module(new Queue(new MSHREntry, 2, false, false))

  toPrefetchAddressQueue.io.enq.valid := false.B
  toPrefetchAddressQueue.io.enq.bits  := DontCare
  io.nextFetchAddress                 <> toPrefetchAddressQueue.io.deq
  io.continuePrefetch.ready           := toPrefetchAddressQueue.io.enq.ready

  when(io.continuePrefetch.valid) {
//    io.continuePrefetch.ready := toPrefetchAddressQueue.io.enq.ready
    toPrefetchAddressQueue.io.enq.valid      := true.B
    toPrefetchAddressQueue.io.enq.bits.tag   := io.continuePrefetch.bits.tag
    toPrefetchAddressQueue.io.enq.bits.index := io.continuePrefetch.bits.index + prefetchDepth.U
  }.elsewhen(io.newPrefetchRequest.valid) {
      prefetchCounter     := prefetchDepth.U
      prefetchBaseAddress := io.newPrefetchRequest.bits
    }
    .otherwise {
      toPrefetchAddressQueue.io.enq.valid      := prefetchCounter =/= 0.U
      toPrefetchAddressQueue.io.enq.bits.tag   := prefetchBaseAddress.tag
      toPrefetchAddressQueue.io.enq.bits.index := prefetchBaseAddress.index + ((prefetchDepth + 1).U - prefetchCounter)
      when(toPrefetchAddressQueue.io.enq.fire && prefetchCounter =/= 0.U) {
        prefetchCounter := prefetchCounter - 1.U
      }
    }
}
