package axi

import chisel3._
import chisel3.util._

/**
  * axi arbiter that arbitrates between ar and r channel
  * the priority should be connected manually
  * @note: please change the ordering of the ports
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

  // VERI: cover queue is full, is empty, neither full or empty
  /** as 2 masters ( uncached unit and dcache unit ) share ID, this queue must tell the difference between them */
  val readQueue = Module(new Queue(UInt(log2Ceil(masterCount).W), 2, pipe = true, flow = true))

  /** ar arbiting state. As ar dictates that once valid goes high, the address could not change,
    * every cycle, a combinatorial check will be performed on ar valid in every channel
    * if there is no ar valid, everything defaults to 0
    * if there are multiple or one ar valid, the one with the highest priority will be asserted ready
    * this will make the arbiter enter locked state, i.e. it won't change again unless the ar handshake has taken place */
  val arIdle :: arLocked :: Nil = Enum(2)
  val arState                   = RegInit(arIdle)

  /** a vec of bits, depicting if the master under index is valid
    * this only tracks the ar valid array in this cycle */
  val arValidVec = VecInit(io.fromMasters.map { _.ar.valid })

  /** see which master assert ar valid, and perform with it an ar handshake */
  val selectARNum = arValidVec.indexWhere((masterValid: Bool) => masterValid)

  /** is any ar valid? */
  val hasARValid = arValidVec.foldLeft(false.B)(_ || _)

  /** VERI: after ar handshake with master and before ar handshake with bus, this must not change */
  val lockedARNumReg = Reg(UInt(log2Ceil(masterCount).W))

  /** arbiter can only continue to server ar request when queue is not full */
  val arbiterARReady = readQueue.io.enq.ready

  io.fromMasters <> 0.U.asTypeOf(io.fromMasters)
  io.toBus       <> 0.U.asTypeOf(io.toBus)

  val arPassThroughSelect = Mux(arState === arIdle, selectARNum, lockedARNumReg)
  // VERI: only 1 ar could be high at the same time
  // VERI: to bus ar cannot fire when from master ar is not valid
  //VERI: cannot fire when queue is full
  when(arbiterARReady) {
    io.fromMasters(arPassThroughSelect).ar <> io.toBus.ar
  }

  readQueue.io.enq.valid := io.toBus.ar.fire && io.toBus.ar.bits.id === shared.Constants.DATA_ID
  // VERI: every element is queue is between 1 and 2
  readQueue.io.enq.bits := arPassThroughSelect

  switch(arState) {
    is(arIdle) {
      when(hasARValid && !io.fromMasters(selectARNum).ar.fire && arbiterARReady) {
        lockedARNumReg := selectARNum
        arState        := arLocked
      }
    }
    is(arLocked) {
      // VERI: assert from master fire is to bus fire
      when(io.fromMasters(lockedARNumReg).ar.fire) {
        arState := arIdle
      }
    }
  }

  val rIdle :: rLocked :: Nil = Enum(2)
  val rState                  = RegInit(rIdle)

  val lockedRNumReg = Reg(UInt(log2Ceil(masterCount).W))

  when(rState === rLocked) {
    io.toBus.r <> io.fromMasters(lockedRNumReg).r
  }
  readQueue.io.deq.ready := false.B
  switch(rState) {
    is(rIdle) {
      when(io.toBus.r.valid) {
        when(io.toBus.r.bits.id === shared.Constants.INST_ID) {
          lockedRNumReg := 0.U
        }.otherwise {
          // VERI: queue not empty
          readQueue.io.deq.ready := true.B
          lockedRNumReg          := readQueue.io.deq.bits
        }
        rState := rLocked
      }
    }
    is(rLocked) {
      when(io.toBus.r.fire && io.toBus.r.bits.last) {
        rState := rIdle
      }
    }
  }
}
