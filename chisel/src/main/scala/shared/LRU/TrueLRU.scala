package shared.LRU

import chisel3._
import chisel3.util._

class TrueLRU(numOfSet: Int, numOfWay: Int) extends BaseLRU(pNumOfSets = numOfSet, pNumOfWays = numOfWay) {
  require(numOfWay == 2, "number of way should not be 2. We have a true LRU for 2")

  val lruLine = RegInit(VecInit(Seq.fill(numOfSet)(false.B)))
  when(io.accessEnable) {
    lruLine(io.accessSet) := !io.accessWay
  }
  io.lruLine := lruLine(io.accessSet)
}
