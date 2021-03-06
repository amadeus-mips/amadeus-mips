package cpu.pipelinedCache.components

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.addressBundle.{QueryAddressBundle, RecordAddressBundle}
import firrtl.options.TargetDirAnnotation

//TODO: hit in bust write queue?
//TODO: reduce wire usage
class WriteQueue(capacity: Int = 8)(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
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

    /** write handshake represents a aw handshake or a w handshake
      * once this handshake has been made, subsequent read to the same location in write queue is treated as miss
      * according to axi ordering rules, read miss now will read newly written value
      * this will impose ordering requests on [[axi.AXIArbiter]]*/
    val writeHandshake = Input(Bool())

    /** when dequeue is at its last stage */
    val dequeueLast = Output(Bool())

    /** when there is a write query hit in the write queue */
    val holdOffNewMiss = Output(Bool())

    val size = Output(UInt((log2Ceil(capacity)+1).W))
  })
  require(isPow2(capacity))
  require(capacity >= cacheConfig.numOfWays)

  /** size of the queue: how many entries are in this queue */
  val size = RegInit((if (CPUConfig.verification) 0 else 0).U((log2Ceil(capacity) + 1).W))

  /** points to the head of the queue */
  val headPTR = RegInit(0.U(log2Ceil(capacity).W))
  dontTouch(headPTR)

  /** points to the head of the queue */
  val tailPTR = RegInit(0.U(log2Ceil(capacity).W))

  /** write ptr within a cache line */
  val lineWritePTR = RegInit(0.U(log2Ceil(cacheConfig.numOfBanks).W))


  val dIdle :: dDispatch :: Nil = Enum(2)
  val dispatchState             = RegInit(dIdle)

  require(!CPUConfig.verification)
  // separate the associative request, data and valid
  val addrBank = Mem(capacity, new RecordAddressBundle)
//  val addrBank = RegInit(
//    VecInit(Seq.tabulate(capacity)(i => (new RecordAddressBundle).Lit(_.tag -> 7.U, _.index -> i.U)))
//  )
  // valid bank
  val validBank = RegInit(VecInit(Seq.fill(capacity)((false).B)))
  // data banks
  val dataBanks = Mem(capacity, Vec(cacheConfig.numOfBanks, UInt(32.W)))
//  val dataBanks = RegInit(
//    VecInit(
//      // this reset value is for verification, but there is no convenient way to swap this to mem
//      Seq.tabulate(capacity)(i => VecInit(Seq.tabulate(cacheConfig.numOfBanks)(k => (7 * 4 + i * 2 + k).U(32.W))))
//    )
//  )

  val dispatchDataWire = WireInit(dataBanks(headPTR)(lineWritePTR))

  val queryHitVec = Wire(Vec(capacity, Bool()))
  queryHitVec := (0 until capacity).map{ case (index) => addrBank(index).index === io.query.addr.index && addrBank(index).tag === io.query.addr.phyTag && validBank(index)}
//  queryHitVec := (addrBank.zip(validBank)).map {
//    case (addr, valid) => ((addr.index === io.query.addr.index) && (addr.tag === io.query.addr.phyTag) && valid)
//  }
  val queryHitPos = Wire(UInt(log2Ceil(capacity).W))
  queryHitPos := queryHitVec.indexWhere(queryHit => queryHit)
  val isQueryHit =
    /**
      * there is a hit in the hit vec
      * if the line is not being dispatched, everything is good
      * or the query is a read, also good
      * but if the query is performing a write, then it is not counted as a query hit.
      * [[cpu.pipelinedCache.dataCache.query.QueryTop]]
      * will issue a new miss. As this handshake has taken place yet, the read will observe this write
      * in transfer
      */
    queryHitVec.asUInt =/= 0.U && Mux(io.query.writeMask === 0.U, true.B, queryHitPos =/= headPTR)

  io.holdOffNewMiss := queryHitVec.asUInt =/= 0.U && io.query.writeMask =/= 0.U && queryHitPos === headPTR

  val hasAWHandshake = RegInit(false.B)

  /** enqueue io, when not full, enqueue is ready */
  io.enqueue.ready := size =/= capacity.U

  io.resp.valid := isQueryHit
  io.resp.bits  := dataBanks(queryHitPos)(io.query.addr.bankIndex)

  /** dequeue io, always dequeue when fifo is not empty */
  io.dequeueAddr.bits  := addrBank(headPTR)
  io.dequeueAddr.valid := dispatchState === dDispatch && !hasAWHandshake
  io.dequeueData.bits  := dispatchDataWire
  io.dequeueData.valid := dispatchState === dDispatch

  io.dequeueLast := (lineWritePTR === (cacheConfig.numOfBanks - 1).U) && dispatchState === dDispatch

  io.size := size

  when(io.enqueue.fire) {
    addrBank(tailPTR)  := io.enqueue.bits.addr
    validBank(tailPTR) := true.B
    dataBanks(tailPTR) := io.enqueue.bits.data
    tailPTR            := tailPTR - 1.U
  }

  size := size - ((dispatchState === dDispatch) && (io.dequeueData.fire) && (lineWritePTR === (cacheConfig.numOfBanks - 1).U)).asUInt + io.enqueue.fire.asUInt

  // write query
  when(io.query.writeMask.asUInt =/= 0.U && isQueryHit) {
    dataBanks(queryHitPos)(io.query.addr.bankIndex) := Cat(
      (3 to 0 by -1).map(i =>
        Mux(
          io.query.writeMask(i),
          io.query.data(7 + 8 * i, 8 * i),
          dataBanks(queryHitPos)(io.query.addr.bankIndex)(7 + 8 * i, 8 * i)
        )
      )
    )
  }

  switch(dispatchState) {
    is(dIdle) {
      when(size =/= 0.U) {
        dispatchState     := dDispatch
        lineWritePTR      := 0.U
        hasAWHandshake := false.B
      }
    }
    is(dDispatch) {
      when(io.dequeueAddr.fire) {
        hasAWHandshake := true.B
      }
      when(io.dequeueData.fire) {
        lineWritePTR := lineWritePTR + 1.U
        when(lineWritePTR === (cacheConfig.numOfBanks - 1).U) {
          dispatchState      := dIdle
          headPTR            := headPTR - 1.U
          validBank(headPTR) := false.B
        }
      }
    }
  }

  assert(size === 0.U || (size =/= 0.U && validBank(headPTR)))
  assert(headPTR =/= tailPTR || (headPTR === tailPTR && (size === 0.U || size === capacity.U)))
}

object WriteQueueElaborate extends App {
  implicit val cacheConfig = new CacheConfig
  implicit val CPUConfig   = new CPUConfig(build = false, verification = true)
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new WriteQueue), TargetDirAnnotation("verification"))
  )
}
