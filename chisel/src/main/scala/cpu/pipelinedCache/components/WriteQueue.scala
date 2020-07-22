package cpu.pipelinedCache.components

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.addressBundle.{QueryAddressBundle, RecordAddressBundle}
import firrtl.options.TargetDirAnnotation

//TODO: hit in bust write queue?
//TODO: reduce wire usage
class WriteQueue(capacity: Int = 2)(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** enqueue io, for the data cache to dispatch dirty lines into the write queue
      * this should connect to [[cpu.pipelinedCache.dataCache.DataBanks]] during evict */
    val enqueue = Flipped(Decoupled(new Bundle {
      val addr = new RecordAddressBundle
      val data = Vec(cacheConfig.numOfBanks, UInt(32.W))
    }))

    /** write queue is queried every cycle to see if there is a hit */
    val query = Input(new Bundle {
      val addr      = new QueryAddressBundle
      val data      = UInt(32.W)
      val writeMask = UInt(4.W)
    })

    /** response to the query */
    val resp = Valid(UInt(32.W))

    /** dequeue address information, to dispatch to [[cpu.pipelinedCache.components.AXIPorts.AXIWritePort]] */
    val dequeueAddr = Decoupled(new RecordAddressBundle)

    /** dequeue data information, to dispatch to [[cpu.pipelinedCache.components.AXIPorts.AXIWritePort]] */
    val dequeueData = Decoupled(UInt(32.W))

    /** when dequeue is at its last stage */
    val dequeueLast = Output(Bool())
  })
  require(isPow2(capacity))

  /** size of the queue: how many entries are in this queue */
  val size = RegInit(0.U((log2Ceil(capacity) + 1).W))

  /** points to the head of the queue */
  val headPTR = RegInit(0.U(log2Ceil(capacity).W))
  dontTouch(headPTR)

  /** points to the head of the queue */
  val tailPTR = RegInit(0.U(log2Ceil(capacity).W))

  /** write ptr within a cache line */
  val lineWritePTR = RegInit(0.U(log2Ceil(cacheConfig.numOfBanks).W))

  val dIdle :: dDispatch :: Nil = Enum(2)
  val dispatchState             = RegInit(dIdle)

  // separate the associative request, data and valid
  val addrBank = Reg(Vec(capacity, new RecordAddressBundle))
  // valid bank
  val validBank = RegInit(VecInit(Seq.fill(capacity)(false.B)))
  // data banks
  val dataBanks = Reg(Vec(capacity, Vec(cacheConfig.numOfBanks, UInt(32.W))))

  val dispatchDataWire = WireInit(dataBanks(headPTR)(lineWritePTR))

  val queryHitVec = Wire(Vec(capacity, Bool()))
  queryHitVec := (addrBank.zip(validBank)).map {
    case (addr, valid) => ((addr.index === io.query.addr.index) && (addr.tag === io.query.addr.phyTag) && valid)
  }
  val queryHitPos = Wire(UInt(log2Ceil(capacity).W))
  queryHitPos := queryHitVec.indexWhere(queryHit => queryHit)
  val isQueryHit = queryHitVec.asUInt =/= 0.U

  /** enqueue io, when not full, enqueue is ready */
  io.enqueue.ready := size =/= capacity.U

  io.resp.valid := isQueryHit
  io.resp.bits  := dataBanks(queryHitPos)(io.query.addr.bankIndex)

  /** dequeue io, always dequeue when fifo is not empty */
  io.dequeueAddr.bits  := addrBank(headPTR)
  io.dequeueAddr.valid := dispatchState === dDispatch
  io.dequeueData.bits  := dispatchDataWire
  io.dequeueData.valid := dispatchState === dDispatch

  io.dequeueLast := (lineWritePTR === (cacheConfig.numOfBanks - 1).U) && dispatchState === dDispatch

  when(io.enqueue.fire) {
    addrBank(tailPTR)  := io.enqueue.bits.addr
    validBank(tailPTR) := true.B
    dataBanks(tailPTR) := io.enqueue.bits.data
    tailPTR            := tailPTR - 1.U
    size               := size + 1.U
  }

  // write query
  when(io.query.writeMask.asUInt =/= 0.U && isQueryHit) {
    dataBanks(queryHitPos)(io.query.addr.bankIndex) := Cat(
      (3 to 0 by -1).map(i =>
        Mux(io.query.writeMask(i), io.query.data(i), dataBanks(queryHitPos)(io.query.addr.bankIndex)(i))
      )
    )
  }

  switch(dispatchState) {
    is(dIdle) {
      when(size =/= 0.U) {
        dispatchState := dDispatch
        lineWritePTR  := 0.U
      }
    }
    is(dDispatch) {
      when(io.dequeueData.fire) {
        lineWritePTR := lineWritePTR + 1.U
      }
      when(lineWritePTR === (cacheConfig.numOfBanks - 1).U) {
        dispatchState := dIdle
      }
    }
  }

  assert(size === 0.U || (size =/= 0.U && validBank(headPTR)))
  assert(headPTR =/= tailPTR || (headPTR === tailPTR && (size === 0.U || size === (cacheConfig.numOfBanks - 1).U)))
}

object WriteQueueElaborate extends App {
  implicit val cacheConfig = new CacheConfig
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new WriteQueue), TargetDirAnnotation("verification"))
  )
}
