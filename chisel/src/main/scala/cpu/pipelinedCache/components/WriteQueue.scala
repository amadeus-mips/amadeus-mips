package cpu.pipelinedCache.components

import axi.AXIIO
import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.addressBundle.{QueryAddressBundle, RecordAddressBundle}

class WriteQueue(capacity: Int = 8)(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    val enqueue = Flipped(Decoupled(new Bundle {
      val addr = new RecordAddressBundle
      val data = Vec(cacheConfig.numOfBanks, UInt(32.W))
    }))

    val query = Input(new Bundle {
      val addr      = new QueryAddressBundle
      val data      = Vec(4, UInt(8.W))
      val writeMask = Vec(4, Bool())
    })
    val respData = Valid(UInt(32.W))
    val axi      = AXIIO.master()
  })
  require(isPow2(capacity))

  // size of the write queue
  val size    = RegInit(0.U(log2Ceil(capacity).W))
  val headPTR = RegInit(0.U(log2Ceil(capacity).W))
  val tailPTR = RegInit(0.U(log2Ceil(capacity).W))

  // separate the associative request, data and valid
  val addrBank = Reg(Vec(capacity, new RecordAddressBundle))
  // valid bank
  val validBank = RegInit(VecInit(Seq.fill(capacity)(false.B)))
  // data banks
  for (i <- 0 until capacity) {
    val dataBank = Mem(cacheConfig.numOfBanks, Vec(4, UInt(8.W)))
    dataBank.suggestName(s"data_bank_No.$i")
  }

  // AXI registers, to comply with axi rules

  io.enqueue.ready := size === capacity.U
  io.axi           := DontCare

}
