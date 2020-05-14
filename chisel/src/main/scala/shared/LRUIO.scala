package shared
import chisel3._
import chisel3.util._

/**
  * io for lru units
  * @param pNumOfSets how many sets there are in the cache
  * @param pNumOfWays how many ways there are in each set
  */
class LRUIO(val pNumOfSets: Int, val pNumOfWays: Int) extends Bundle {
  /**
    * access enable: whether to update LRU for this cycle
    * access set: which set I'm accessing
    * access way: which way in the set I'm accessing
    * lru line: the lru line in the set way
    */
  val accessEnable = Input(Bool())
  val accessSet = Input(UInt(log2Ceil(pNumOfSets).W))
  val accessWay = Input(UInt(log2Ceil(pNumOfWays).W))
  val lruLine = Output(UInt(log2Ceil(pNumOfWays).W))
}
