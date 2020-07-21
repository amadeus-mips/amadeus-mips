package cpu.pipelinedCache.dataCache.query

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

class DirtyBanks(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** query whether data at index is dirty */
    val queryIndex = Input(UInt(cacheConfig.indexLen.W))

    /** write to index, way to make it dirty or not dirty
      * write not dirty only applies to evict */
    val write = Flipped(Valid(new Bundle {
      val indexSelect = UInt(cacheConfig.indexLen.W)
      val waySelect   = UInt(log2Ceil(cacheConfig.numOfWays).W)

      /** when is dirty == true, write as dirty */
      val isDirty = Bool()
    }))

    /** query dirty result */
    val indexDirty = Output(Vec(cacheConfig.numOfWays, Bool()))
  })
  for (i <- 0 until cacheConfig.numOfWays) {
    val bank = RegInit(VecInit(Seq.fill(cacheConfig.numOfSets)(false.B)))
    io.indexDirty(i) := bank(io.queryIndex)
    when(io.write.valid && i.U === io.write.bits.waySelect) {
      bank(io.write.bits.indexSelect) := io.write.bits.isDirty
    }
  }
}
