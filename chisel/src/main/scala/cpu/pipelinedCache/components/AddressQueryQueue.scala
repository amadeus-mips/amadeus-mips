package cpu.pipelinedCache.components

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

class QueryRecordingBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val addr       = new MSHREntry()
  val isPrefetch = Bool()

  override def cloneType = (new QueryRecordingBundle()).asInstanceOf[this.type]
}

/**
  * used to hold the address of queries that has performed an ar handshake and is waiting to
  * receive data
  */
class AddressQueryQueue(capacity: Int = 2)(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    //TODO: merge enqueue and queryAddress.
    /** enqueue into address query queue */
    val enqueue = Flipped(Decoupled(new QueryRecordingBundle))

    /** query if an address is in the address queue already. This is
      * to make sure that the same address is not queried through ar twice */
    val queryAddress = Input(new MSHREntry())

    val queryResult = Output(Bool())

    /** dequeue from the query queue */
    val dequeue = Decoupled(new QueryRecordingBundle)
  })

  val size = RegInit(0.U((log2Ceil(capacity) + 1).W))

  val headPTR = RegInit(0.U(log2Ceil(capacity).W))
  val tailPTR = RegInit(0.U(log2Ceil(capacity).W))

  val queryArray = Reg(Vec(capacity, new QueryRecordingBundle()))
  val validArray   = RegInit(VecInit(Seq.fill(capacity)(false.B)))

  assert(validArray(headPTR) === true.B || size === 0.U)
  assert(tailPTR =/= headPTR || (size === 0.U || size === capacity.U))

  val queryHitVec = Wire(Vec(capacity, Bool()))
  queryHitVec := (0 until capacity).map(i => queryArray(i).addr.tag === io.queryAddress.tag && queryArray(i).addr.index === io.queryAddress.index && validArray(i)
  )
  val isQueryHit = queryHitVec.asUInt =/= 0.U

  io.enqueue.ready := size =/= capacity.U

  io.queryResult   := isQueryHit
  io.dequeue.valid := size =/= 0.U
  io.dequeue.bits  := queryArray(headPTR)

  when(io.enqueue.fire) {
    queryArray(tailPTR) := io.enqueue.bits
    validArray(tailPTR)   := true.B
    tailPTR               := tailPTR - 1.U
  }
  when(io.dequeue.fire) {
    validArray(headPTR) := false.B
    headPTR             := headPTR - 1.U
  }
  size := size - io.dequeue.fire + io.enqueue.fire
}
