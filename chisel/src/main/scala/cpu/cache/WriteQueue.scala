package cpu.cache

import chisel3._
import chisel3.util._
import shared.{AXIIO, Constants}

class BufferLineAddr(val tagWidth: Int, val indexWidth: Int) extends Bundle {
  val tag = UInt(tagWidth.W)
  val index = UInt(indexWidth.W)
}

class BufferLineBundle(val lineWidth: Int, val tagWidth: Int, val indexWidth: Int) extends Bundle {
  val data = UInt(lineWidth.W)
  val addr = new BufferLineAddr(tagWidth, indexWidth)
}

/**
  * byte ( 8 bits ) address write queue
  * if there is a write to the line being sent to dram, extract the line and write back to I-cache
  * @param bankWidth size of each bank in the cache line
  * @param bankAmount how many banks there are in the cache line
  * @param capacity how many entries should be in the write queue
  * @param tagWidth how wide should the address tag should be
  * @param indexWidth how wide should the address index should be
  */
class WriteBuffer(bankWidth: Int, bankAmount: Int, capacity: Int = 2, tagWidth: Int, indexWidth: Int, addrWidth: Int = 32) extends Module {
  val lineWidth = bankWidth * bankAmount
  require(capacity >= 1 && capacity <= 8, "capacity of the write queue should not be too large or too small")
  require(isPow2(capacity), "capacity should be a power of 2 to ensure that ")
  val io = IO(new Bundle {
    // io for enqueue and dequeue
    // ready means whether write queue is full
    // valid means whether the source has the data to enqueue
    val enqueueAddr = Flipped(Decoupled(new BufferLineAddr(tagWidth, indexWidth)))
    val axi = AXIIO.master()

    // search in the write queue
    val addr = Input(UInt(addrWidth.W))
    val writeData = Input(UInt(bankWidth.W))
    val writeMask = Input(UInt((bankWidth/8).W))
    val operation = Input(Bool())
  })
  // how many entries are valid in the queue
  val inQueueCounter = Counter(capacity)

  // which is the next entry to enqueue the data in
  val enqPtr = RegInit(0.U(log2Ceil(capacity).W))

  // whether each entry is valid
  val valid = RegInit(VecInit(Seq.fill(capacity)(false.B)))

  val wAddrBuffer = Reg(Vec(capacity, new BufferLineAddr( tagWidth, indexWidth)))

  val wDataBuffer = for (i <- 0 until capacity) yield {

  }

  val sIdle :: sTransfer :: sWriteFinish :: Nil = Enum(3)
  val state = RegInit(sIdle)

  // keep track of whether there has been an aw handshake yet
  // true means still waiting, false means that the handshake has finished
  // only assert before waiting for handshake
  val waitForAWHandshake = RegInit(false.B)
  //-----------------------------------------------------------------------------
  //------------------define a counple of functions to trim the addr-------------
  //-----------------------------------------------------------------------------

  //-----------------------------------------------------------------------------
  //------------------default IO for the write buffer----------------------------
  //-----------------------------------------------------------------------------
  // whether the write queue is full is whether it has reached capacity
//  io.enqueue.ready := inQueueCounter.value === capacity.U

  io.axi := DontCare
  //TOD:
//  io.axi.aw.bits.addr :=
  /**
    * default IO for aw
    */
  io.axi.aw.bits.id    := Constants.DATA_ID
  io.axi.aw.bits.len   := (bankAmount - 1).U(4.W)
  io.axi.aw.bits.size  := "b010".U(3.W)
  io.axi.aw.bits.burst := "b10".U(2.W)
  io.axi.aw.bits.cache := 0.U
  io.axi.aw.bits.prot  := 0.U
  io.axi.aw.bits.lock  := 0.U
  io.axi.aw.valid := waitForAWHandshake

  io.axi.w.bits.id   := Constants.DATA_ID
  io.axi.w.bits.strb := "b1111".U(4.W)
  io.axi.w.bits.last := false.B
  io.axi.w.valid     := state === sTransfer

  io.axi.b.ready := state === sWriteFinish


}
