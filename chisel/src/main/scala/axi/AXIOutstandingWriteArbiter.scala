package axi

import chisel3._
import chisel3.util._

/**
  * out standing axi write arbiter will process aw and w seperately. w order is the same as aw order
  */
class AXIOutstandingWriteArbiter extends Module {
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

  io.toBus.b.ready := true.B

  val awIdle :: awLocked :: Nil = Enum(2)
  val awState                   = RegInit(awIdle)

  // w channels are routed in the order aw handshake is performed
  val writeQueue = Module(new Queue(UInt(1.W), 16, pipe = true, flow = true))

  val isMastersAWValid = io.fromMasters(0).aw.valid || io.fromMasters(1).aw.valid

  val masterAWValidIndex = Mux(io.fromMasters(0).aw.valid, 0.U(1.W), 1.U(1.W))

  val awLockIndexReg = Reg(UInt(1.W))

  val readyForNewAW = writeQueue.io.enq.ready
  writeQueue.io.enq.valid := io.toBus.aw.fire
  writeQueue.io.enq.bits  := awLockIndexReg

  writeQueue.io.deq.ready := io.toBus.w.fire && io.toBus.w.bits.last

  when(writeQueue.io.deq.valid) {
    io.toBus.w <> io.fromMasters(writeQueue.io.deq.bits).w
  }

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
