package cpu.pipelinedCache.dataCache.query

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.metaBanks.TagValidBundle

/**
  * hold the tag and valid information for evict stage ( write back cannot take place before
  * evict stage )
  */
class MissTagValidHolder(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** record the miss address other information on a new miss
      * connected to [[QueryTop.newMiss]] */
    val insertTagValid = Flipped(Valid(Vec(cacheConfig.numOfWays, new TagValidBundle)))

    /** extract when evicting to form new address, connected to [[cpu.pipelinedCache.components.WriteQueue]] */
    val extractTagValid = Output(Vec(cacheConfig.numOfWays, new TagValidBundle))
  })
  val tagValidReg = Reg(Vec(cacheConfig.numOfWays, new TagValidBundle))
  when(io.insertTagValid.valid) {
    tagValidReg := io.insertTagValid.bits
  }
  io.extractTagValid := tagValidReg
}
