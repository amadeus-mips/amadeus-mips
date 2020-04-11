// See README.md for license details.

package memory.axi

import chisel3._
import chisel3.util._
import common.AXIIO

class QueueBundle extends Bundle {
  val addr = UInt(32.W)
  /** the len of the transition, need to inc 1 to get the actual length */
  val len = UInt(4.W)
}

/**
  * only for 'INCR' burst
  *
  * @param id    AXI id
  * @param qSize address buffer queue size
  */
class AXIToSram(id: UInt, qSize: Int = 20) extends Module {
  val io = IO(new Bundle {
    val bus = AXIIO.slave()
    val ram = new SimpleSramIO
  })
  assert(!(io.bus.ar.burst =/= 1.U && io.bus.ar.valid), "Unsupported burst type! Only support INCR burst")

  //---- read channel --------------------------------------------------
  val sRIdle :: sRWaitRam :: sRWaitBus :: Nil = Enum(3)
  val rState = RegInit(sRIdle)

  /** read queue */
  val rq = Module(new Queue(new QueueBundle, qSize))

  val rData = RegInit(0.U(32.W))
  val rValid = RegInit(false.B)
  val ramRAddr = RegInit(0.U(32.W))
  val rLen = RegInit(0.U(4.W))
  val rLast = RegInit(false.B)
  switch(rState) {
    is(sRIdle) {
      rValid := false.B
      when(rq.io.deq.valid) {
        rLen := rq.io.deq.bits.len
        ramRAddr := rq.io.deq.bits.addr
        rState := sRWaitRam
      }
    }
    is(sRWaitRam) {
      when(io.ram.read.valid) {
        rData := io.ram.read.data
        rValid := true.B
        rLast := rLen === 0.U
        when(io.bus.r.ready) {
          when(rLen === 0.U) { // finish
            rState := sRIdle
          }.otherwise { // hold
            ramRAddr := ramRAddr + 4.U
            rLen := rLen - 1.U
          }
        }.otherwise { // wait bus
          rState := sRWaitBus
        }
      }.otherwise {
        rValid := false.B
      }
    }
    is(sRWaitBus) {
      when(io.bus.r.ready) {
        rValid := false.B // in this cycle, data has been sent
        when(rLen === 0.U) { // finish
          rState := sRIdle
        }.otherwise { // wait ram
          ramRAddr := ramRAddr + 4.U
          rLen := rLen - 1.U
          rState := sRWaitRam
        }
      }
    }
  }

  io.bus.ar.ready := rq.io.enq.ready // make sure the queue is not full
  io.bus.r.id := id
  io.bus.r.data := rData
  io.bus.r.resp := 0.U // fixed OKAY
  io.bus.r.last := rLast
  io.bus.r.valid := rValid

  io.ram.read.addr := ramRAddr
  io.ram.read.enable := rState === sRWaitRam

  rq.io.deq.ready := rq.io.deq.valid && rState === sRIdle
  rq.io.enq.bits.addr := io.bus.ar.addr
  rq.io.enq.bits.len := io.bus.ar.len
  rq.io.enq.valid := io.bus.ar.ready && io.bus.ar.valid && io.bus.ar.id === id
  //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


  //---- write channel ----------------------------------------------------
  val sWIdle :: sWWaitBus :: sBWaitBus :: Nil = Enum(3)
  val wState = RegInit(sWIdle)

  /** write queue */
  val wq = Module(new Queue(new QueueBundle, qSize))

  val wReady = RegInit(false.B)
  val ramWAddr = RegInit(0.U(32.W))
  val wLen = RegInit(0.U(4.W))
  switch(wState) {
    is(sWIdle) {
      when(wq.io.deq.valid) {
        wLen := wq.io.deq.bits.len
        ramWAddr := wq.io.deq.bits.addr
        wState := sWWaitBus
      }
    }
    is(sWWaitBus) {
      when(io.bus.w.valid && io.ram.write.valid) {
        assert(!(wLen === 0.U ^ io.bus.w.last), "wLen conflict with last")
        when(wLen === 0.U && io.bus.w.last) {
          wState := sBWaitBus
        }.otherwise {
          ramWAddr := ramWAddr + 4.U
          wLen := wLen - 1.U
        }
      }
    }
    is(sBWaitBus) {
      when(io.bus.b.ready) {
        wState := sWIdle
      }
    }
  }

  io.bus.aw.ready := wq.io.enq.ready // make sure the queue is not full
  io.bus.w.ready := wState === sWWaitBus && io.bus.w.valid && io.ram.write.valid
  io.bus.b.id := id
  io.bus.b.resp := 0.U // fixed OKAY
  io.bus.b.valid := wState === sBWaitBus

  io.ram.write.addr := ramWAddr
  io.ram.write.enable := wState === sWWaitBus && io.bus.w.valid
  io.ram.write.sel := io.bus.w.strb
  io.ram.write.data := io.bus.w.data

  wq.io.deq.ready := wState === sWIdle && wq.io.deq.valid
  wq.io.enq.bits.addr := io.bus.aw.addr
  wq.io.enq.bits.len := io.bus.aw.len
  wq.io.enq.valid := io.bus.aw.ready && io.bus.aw.valid && io.bus.aw.id === id
  //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

}
