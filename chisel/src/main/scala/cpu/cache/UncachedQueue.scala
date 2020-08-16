package cpu.cache

import axi.AXIIO
import chisel3._
import chisel3.util._
import cpu.common.MemReqBundle
import shared.Constants

class UncachedQueue(capacity: Int = 64) extends Module {
  val io = IO(new Bundle {

    /** see documentation of [[cpu.pipelinedCache.DataCache.io.request]] */
    val request = Flipped(Decoupled(new MemReqBundle))

    /** see documentation of [[cpu.pipelinedCache.DataCache.io.commit]] */
    val commit = Output(Bool())

    /** see documentation of [[cpu.pipelinedCache.DataCache.io.readData]] */
    val readData = Output(UInt(32.W))

    val axi = AXIIO.master()
  })

  /** for write meta data */
  class WriteMeta extends Bundle {
    val data = UInt(32.W)
    val strb = UInt(4.W)
  }

  val reqValid = io.request.valid
  val reqRead  = io.request.bits.writeMask === 0.U
  val reqWrite = io.request.bits.writeMask =/= 0.U

  val rIdle :: rRead :: rWrite :: Nil = Enum(3)
  val rState                          = RegInit(rIdle)

  /** count how many outstanding read are there */
  val rCounter = RegInit(0.U((log2Ceil(capacity) + 1).W))

  val bCounter = RegInit(0.U((log2Ceil(capacity) + 1).W))

  /** only stores read address */
  val readQueue = Module(new Queue(UInt(32.W), capacity, true, false))

  /** stores the address for the write */
  val writeAddressQueue = Module(new Queue(UInt(32.W), capacity, true, false))

  /** stores the data for the write */
  val writeDataQueue = Module(new Queue(new WriteMeta, capacity, true, false))

  /** when idle, can always handle input; when read or write, can handle input as long as queue is not full */
  io.request.ready := MuxCase(
    true.B,
    Array(
      (rState === rRead) -> (((readQueue.io.count + rCounter) =/= capacity.U)  && reqRead),
      (rState === rWrite) -> ((writeAddressQueue.io.count + writeDataQueue.io.count + bCounter) =/= capacity.U && reqWrite)
    )
  )

  /** when it is a write, can commit as soon as dispatched. When it is a read, can only commit
    * when read data has returned */
  io.commit := io.axi.r.fire

  io.readData := RegNext(io.axi.r.bits.data)

  io.axi.ar.bits.id    := Constants.DATA_ID
  io.axi.ar.bits.addr  := readQueue.io.deq.bits
  io.axi.ar.bits.len   := 0.U(4.W)
  io.axi.ar.bits.size  := MuxCase("b010".U(3.W), Array(
    (readQueue.io.deq.bits(0) === 1.U) -> "b000".U(3.W),
    (readQueue.io.deq.bits(1) === 1.U) -> "b001".U(3.W)
  ))
  io.axi.ar.bits.burst := "b01".U(2.W) // Incrementing-request burst
  io.axi.ar.bits.lock  := 0.U
  io.axi.ar.bits.cache := 0.U
  io.axi.ar.bits.prot  := 0.U
  io.axi.ar.valid      := readQueue.io.deq.valid

  io.axi.r.ready := rState === rRead

  io.axi.aw.bits.id    := Constants.DATA_ID
  io.axi.aw.bits.addr  := Cat(writeAddressQueue.io.deq.bits(31,2), 0.U(2.W))
  io.axi.aw.bits.len   := 0.U(4.W)
  io.axi.aw.bits.size  := "b010".U(3.W)
  io.axi.aw.bits.burst := "b01".U(2.W)
  io.axi.aw.bits.lock  := 0.U
  io.axi.aw.bits.cache := 0.U
  io.axi.aw.bits.prot  := 0.U
  io.axi.aw.valid      := writeAddressQueue.io.deq.valid

  io.axi.w.bits.id   := Constants.DATA_ID
  io.axi.w.bits.data := writeDataQueue.io.deq.bits.data
  io.axi.w.bits.strb := writeDataQueue.io.deq.bits.strb
  io.axi.w.bits.last := true.B
  io.axi.w.valid     := writeDataQueue.io.deq.valid

  /** when b complete, r could begin */
  io.axi.b.ready := rState =/= rRead

  readQueue.io.enq.bits  := Cat(io.request.bits.tag, io.request.bits.physicalIndex)
  readQueue.io.enq.valid := reqValid && reqRead && rState =/= rWrite
  readQueue.io.deq.ready := io.axi.ar.fire

  writeAddressQueue.io.enq.bits  := Cat(io.request.bits.tag, io.request.bits.physicalIndex)
  writeAddressQueue.io.enq.valid := io.request.fire && reqWrite && rState =/= rRead
  writeAddressQueue.io.deq.ready := io.axi.aw.fire

  writeDataQueue.io.enq.bits.data := io.request.bits.writeData
  writeDataQueue.io.enq.bits.strb := io.request.bits.writeMask
  writeDataQueue.io.enq.valid     := io.request.fire && reqWrite && rState =/= rRead
  writeDataQueue.io.deq.ready     := io.axi.w.fire

  rCounter := MuxCase(
    rCounter,
    Array(
      (io.axi.r.fire && io.axi.ar.fire) -> rCounter,
      io.axi.r.fire                     -> (rCounter - 1.U),
      io.axi.ar.fire                    -> (rCounter + 1.U)
    )
  )

  bCounter := MuxCase(
    bCounter,
    Array(
      (io.axi.b.fire && io.axi.aw.fire) -> bCounter,
      io.axi.b.fire                     -> (bCounter - 1.U),
      io.axi.aw.fire                    -> (bCounter + 1.U)
    )
  )

  switch(rState) {
    is(rIdle) {
      when(io.request.fire) {
        when(reqRead) {
          rState := rRead
        }.elsewhen(reqWrite) {
          rState := rWrite
        }
      }
    }
    is(rRead) {
      when(rCounter === 0.U && readQueue.io.count === 0.U && !io.request.fire) {
        rState := rIdle
      }
    }
    is(rWrite) {
      when(writeAddressQueue.io.count === 0.U && writeDataQueue.io.count === 0.U && bCounter === 0.U && !io.request.fire) {
        rState := rIdle
      }
    }
  }
}
