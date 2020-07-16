package cpu.pipelinedCache.components

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.addressBundle.RecordAddressBundle
import cpu.pipelinedCache.components.metaBanks.TagValidBundle

class MissComparator(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    /* tag valid bundle for all ways*/
    val tagValid = Input(Vec(cacheConfig.numOfWays, new TagValidBundle))
    /* physical tag for the request */
    val phyTag            = Input(UInt(cacheConfig.tagLen.W))
    val index             = Input(UInt(cacheConfig.indexLen.W))
    val mshr              = Flipped(Valid(new RecordAddressBundle))
    val refillBufferValid = Input(Bool())

    val bankHitWay        = Valid(UInt(log2Ceil(cacheConfig.numOfWays).W))
    val hitInRefillBuffer = Output(Bool())
  })

  val bankHitVec = Wire(Vec(cacheConfig.numOfWays, Bool()))
  bankHitVec := io.tagValid.map {
    case (tagValidBundle: TagValidBundle) => tagValidBundle.valid && tagValidBundle.tag === io.phyTag
  }
  io.bankHitWay.valid := bankHitVec.contains(true.B)
  io.bankHitWay.bits  := bankHitVec.indexWhere(hit => hit === true.B)

  /**
    * when there is a hit in the refill buffer, the tag much be equal to missing tag, index much match
    * and valid must be returned
    */
  io.hitInRefillBuffer :=
    io.phyTag === io.mshr.bits.tag &&
      io.index === io.mshr.bits.index &&
      io.refillBufferValid
}
