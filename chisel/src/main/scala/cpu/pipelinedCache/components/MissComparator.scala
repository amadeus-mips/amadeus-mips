package cpu.pipelinedCache.components

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.addressBundle.RecordAddressBundle
import cpu.pipelinedCache.components.metaBanks.TagValidBundle

class MissComparator(implicit cacheConfig: CacheConfig) extends MultiIOModule {
  val io = IO(new Bundle {
    /* tag valid bundle for all ways*/
    val tagValid = Input(Vec(cacheConfig.numOfWays, new TagValidBundle))
    /* physical tag for the request */
    val phyTag = Input(UInt(cacheConfig.tagLen.W))
    val index  = Input(UInt(cacheConfig.indexLen.W))
    val mshr   = Input(new RecordAddressBundle)

    /** when the cache is not handling a miss, it should not hit in refill buffer */
    val isCacheInMiss = Input(Bool())

    /** which way is bank is there is a hit
      * bits refer to the way hit
      * valid denotes a hit */
    val bankHitWay = Valid(UInt(log2Ceil(cacheConfig.numOfWays).W))

    /** truly hit in the refill buffer */
    val addrHitInRefillBuffer = Output(Bool())
  })

  val bankHitVec = Wire(Vec(cacheConfig.numOfWays, Bool()))
  bankHitVec := io.tagValid.map { tagValidBundle =>
    tagValidBundle.valid && tagValidBundle.tag === io.phyTag
  }
  assert(bankHitVec.map(_.asUInt()).reduce(_ + _) <= 1.U)
  io.bankHitWay.valid := bankHitVec.contains(true.B)
  io.bankHitWay.bits  := bankHitVec.indexWhere(hit => hit === true.B)

  /**
    * when there is a hit in the refill buffer, the tag much be equal to missing tag, index much match
    * and valid must be returned
    */
  io.addrHitInRefillBuffer :=
    io.phyTag === io.mshr.tag &&
      io.index === io.mshr.index && !io.isCacheInMiss
}
