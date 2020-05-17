package shared.LRU

import chisel3._
import chisel3.util._

class LRUIO(val pNumOfSets: Int, val pNumOfWays: Int) extends Bundle {
  val accessEnable = Input(Bool())
  val accessSet = Input(UInt(log2Ceil(pNumOfSets).W))
  val accessWay = Input(UInt(log2Ceil(pNumOfWays).W))
  val lruLine = Output(UInt(log2Ceil(pNumOfWays).W))
}
