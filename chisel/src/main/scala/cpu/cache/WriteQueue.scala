package cpu.cache

import chisel3._
import chisel3.util._
import shared.AXIIO

class QueueLine(lineWidth: Int, addrWidth: Int) extends Bundle {
  val data = UInt(lineWidth.W)
  val addr = UInt(addrWidth.W)
}

/**
  * byte ( 8 bits ) address write queue
  * if there is a write to the line being sent to dram, extract the line and write back to I-cache
  * @param blockWidth size of each block in the cache line
  * @param blockSize how many blocks there are in the cache line
  * @param capacity how many entries should be in the write queue
  * @param addrWidth how wide should the address be
  */
class WriteQueue(blockWidth: Int, blockSize: Int, capacity: Int = 2, addrWidth: Int = 32) extends Module {
  val lineWidth = blockWidth * blockSize
  require(capacity >= 1 && capacity <= 8, "capacity of the write queue should not be too large or too small")
  val io = IO(new Bundle {
    // io for enqueue and dequeue
    // ready means whether write queue is full
    // valid means whether the source has the data to enqueue
    val enqueue = Flipped(Decoupled(new QueueLine(lineWidth, addrWidth)))
    val axi = AXIIO.master()

    // search in the write queue
    val addr = Input(UInt(addrWidth.W))
    val writeData = Input(UInt(blockWidth.W))
    val writeMask = Input(UInt((blockWidth/8).W))
    val operation = Input(Bool())
  })
  val inQueue = RegInit(0.U(log2Ceil(capacity).W))

  // point at the oldest pointer
  val oldestPtr = RegInit(0.U(log2Ceil(capacity).W))

  val valid = RegInit(VecInit(Seq.fill(capacity)(false.B)))
  val c = Queue
  // whether the write queue is full is whether it has reached capacity
  io.enqueue.ready := inQueue === capacity.U

}
