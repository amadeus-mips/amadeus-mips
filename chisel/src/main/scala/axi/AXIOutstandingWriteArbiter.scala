package axi

import chisel3._
import chisel3.util._

/**
  * out standing axi write arbiter will process aw and w seperately. w order is the same as aw order
  */
class AXIOutstandingWriteArbiter(capacity: Int = 64) extends Module {
  val io = IO(new Bundle {

    //VERI: assume master comply with axi requirements
    /** connect to axi ports in master, namely [[cpu.pipelinedCache.components.AXIPorts.AXIReadPort]],
      * [[cpu.pipelinedCache.components.AXIPorts.AXIWritePort]] and [[cpu.cache.UnCachedUnit]] */
    val fromMasters = Vec(2, AXIIO.slave())

    // VERI: assert it's output meet the same requirements
    /** connect to axi bus */
    val toBus = AXIIO.master()
  })

  io.fromMasters <> 0.U.asTypeOf(io.fromMasters)
  io.toBus       <> 0.U.asTypeOf(io.toBus)

  val awIdle :: awLocked :: Nil = Enum(2)
  val awState                   = RegInit(awIdle)

  /** keep count of the size. There should be no more than capacity number of requests in flight */
  val size = RegInit(0.U((log2Ceil(capacity) + 1).W))

  // w channels are routed in the order aw handshake is performed
  val writeQueue = Module(new Queue(UInt(1.W), capacity, pipe = true, flow = true))

  /** wait for b queue */
  val bQueue = Module(new Queue(UInt(1.W), capacity, pipe = true, flow = true))

  val isMastersAWValid = io.fromMasters(0).aw.valid || io.fromMasters(1).aw.valid

  val masterAWValidIndex = Mux(io.fromMasters(0).aw.valid, 0.U(1.W), 1.U(1.W))

  val awLockIndexReg = Reg(UInt(1.W))

  /** when size is less than capacity, write queue is always not full */
  val readyForNewAW = size =/= capacity.U
  writeQueue.io.enq.valid := io.toBus.aw.fire
  writeQueue.io.enq.bits  := awLockIndexReg

  size := MuxCase(size, Array(
    (io.toBus.aw.fire && io.toBus.b.fire) -> size,
    io.toBus.aw.fire -> (size + 1.U),
    io.toBus.b.fire -> (size - 1.U)
  ))

  writeQueue.io.deq.ready := io.toBus.w.fire && io.toBus.w.bits.last
  // VERI: will the write queue and b queue overflow and lose data?

  bQueue.io.enq.valid := writeQueue.io.deq.fire
  bQueue.io.enq.bits := writeQueue.io.deq.bits
  bQueue.io.deq.ready := io.toBus.b.fire

  when(writeQueue.io.deq.valid) {
    io.toBus.w <> io.fromMasters(writeQueue.io.deq.bits).w
  }

  when(bQueue.io.deq.valid) {
    io.toBus.b <> io.fromMasters(bQueue.io.deq.bits).b
  }

  //TODO: optimize me
  switch(awState) {
    is(awIdle) {
      when(isMastersAWValid && readyForNewAW) {
        awLockIndexReg := masterAWValidIndex
        awState        := awLocked
      }
    }
    is(awLocked) {
      assert(readyForNewAW)
      io.fromMasters(awLockIndexReg).aw <> io.toBus.aw
      when(io.toBus.aw.fire) {
        awState := awIdle
      }
    }
  }

}
