package cpu.pipelinedCache.components

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.addressBundle.{QueryAddressBundle, RecordAddressBundle}

class WriteQueue(capacity: Int = 8)(implicit cacheConfig: CacheConfig) extends Module {
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
      val data      = Vec(4, UInt(8.W))
      val writeMask = Vec(4, Bool())
    })

    /** response to the query */
    val respData = Valid(UInt(32.W))

    /** dequeue address information, to dispatch to [[cpu.pipelinedCache.components.AXIPorts.AXIWritePort]] */
    val dequeueAddr = Decoupled(new RecordAddressBundle)

    /** dequeue data information, to dispatch to [[cpu.pipelinedCache.components.AXIPorts.AXIWritePort]] */
    val dequeueData = Decoupled(UInt(32.W))

    /** when dequeue is at its last stage */
    val dequeueLast = Output(Bool())
  })
  require(isPow2(capacity))

  // size of the write queue
  val size    = RegInit(0.U(log2Ceil(capacity).W))
  val headPTR = RegInit(0.U(log2Ceil(capacity).W))
  val tailPTR = RegInit(0.U(log2Ceil(capacity).W))

  /** write ptr within a cache line */
  val lineWritePTR = RegInit(0.U(log2Ceil(cacheConfig.numOfBanks).W))

  val dIdle :: dDispatch :: Nil = Enum(2)
  val dispatchState             = RegInit(dIdle)

  val dispatchDataWire = Wire(UInt(32.W))
  // separate the associative request, data and valid
  val addrBank = Reg(Vec(capacity, new RecordAddressBundle))
  // valid bank
  val validBank = RegInit(VecInit(Seq.fill(capacity)(false.B)))
  // data banks
  for (i <- 0 until capacity) {
    val dataBank = Mem(cacheConfig.numOfBanks, Vec(4, UInt(8.W)))
    dataBank.suggestName(s"data_bank_No.$i")
    when(i.U === headPTR) {
      dispatchDataWire := dataBank(lineWritePTR)
    }
  }

  // AXI registers, to comply with axi rules

  io.enqueue.ready := size =/= capacity.U

  io.dequeueAddr.bits  := addrBank(headPTR)
  io.dequeueAddr.valid := Mux(dispatchState === dDispatch, true.B, size =/= 0.U)
  io.dequeueData.bits  := dispatchDataWire
  io.dequeueData.valid := Mux(dispatchState === dDispatch, true.B, size =/= 0.U)

  switch(dispatchState) {
    is(dIdle) {
      when(size =/= 0.U) {
        dispatchState := dDispatch
        lineWritePTR  := 0.U
      }
    }
    is(dDispatch) {
      when()
    }
  }

  assert(size === 0.U || (size =/= 0.U && validBank(headPTR)))

}
