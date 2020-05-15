package shared.LRU

import chisel3._
import chisel3.util._

/**
  * io for lru units
  * @param pNumOfSet how many sets there are in the cache
  * @param pNumOfWay how many ways there are in each set
  */
class LRUIO(val pNumOfSet: Int, val pNumOfWay: Int) extends Bundle {
  /**
    * access enable: whether to update LRU for this cycle
    * access set: which set I'm accessing
    * access way: which way in the set I'm accessing
    * lru line: the lru line in the set way
    */
  val accessEnable = Input(Bool())
  val accessSet = Input(UInt(log2Ceil(pNumOfSet).W))
  val accessWay = Input(UInt(log2Ceil(pNumOfWay).W))
  val lruLine = Output(UInt(log2Ceil(pNumOfWay).W))
}

class BaseLRU(pNumOfSets: Int, pNumOfWays: Int) extends Module {
  require(isPow2(pNumOfWays), "number of way should be a power of 2")
  require(isPow2(pNumOfSets), "number of sets should be a power of 2")
  val io = IO(new LRUIO(pNumOfWay = pNumOfWays, pNumOfSet = pNumOfSets))
}
