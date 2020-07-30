package cpu.pipelinedCache.instCache.fetch

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.metaBanks.{TagValidBanks, TagValidBundle}

class WriteTagValidBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val waySelection   = UInt(log2Ceil(cacheConfig.numOfWays).W)
  val indexSelection = UInt(cacheConfig.indexLen.W)
  val tagValid       = new TagValidBundle

  override def cloneType = (new WriteTagValidBundle).asInstanceOf[this.type]
}

/**
  * fetch the tag and valid bits for all ways
  *
  * @param cacheConfig implicit configuration that governs whole Cache
  */
@chiselName
class TagValid(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {

    /** for query request */
    val index = Input(UInt(cacheConfig.indexLen.W))

    val write = Flipped(Valid(new Bundle {
      val addr = new Bundle {
        val index  = UInt(cacheConfig.indexLen.W)
        val waySel = UInt(log2Ceil(cacheConfig.numOfWays).W)
      }
      val tagValid = new TagValidBundle
    }))

    /** result for read in all 4 ways */
    val tagValid = Output(Vec(cacheConfig.numOfWays, new TagValidBundle))

    /** whether to invalidate all 4 ways */
    val invalidateAllWays = Input(Bool())
  })

  val tagValidBanks = Module(new TagValidBanks)

  //TODO: this currently uses a dual port LUT, change to single port in the future
  for (i <- 0 until cacheConfig.numOfWays) {
    // read tags and valid from tag valid banks
    tagValidBanks.io.way(i).portA.addr := io.index
    io.tagValid(i)                     := tagValidBanks.io.way(i).portA.data

    // writeBack if required
    tagValidBanks.io.way(i).portB.addr := io.write.bits.addr.index
    tagValidBanks.io
      .way(i)
      .portB
      .writeEnable                          := (io.write.valid && io.write.bits.addr.waySel === i.U) || io.invalidateAllWays
    tagValidBanks.io.way(i).portB.writeData := io.write.bits.tagValid
    when(io.invalidateAllWays) {
      tagValidBanks.io.way(i).portB.writeData.valid := false.B
    }
  }

}
