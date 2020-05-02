package shared
import chisel3._
import chisel3.util._

class TrueLRU(numOfWay: Int, numOfSets: Int) extends Module {
  require(numOfWay == 2, "number of way should not be 2. We have a true LRU for 2")
  require(isPow2(numOfWay), "number of way should be a power of 2")
  require(isPow2(numOfSets), "number of sets should be a power of 2")

  val io = IO(new LRUIO(pNumOfSets = numOfSets, pNumOfWays = numOfWay))
  val lruLine = RegInit(VecInit(Seq.fill(numOfSets)(false.B)))
  when(io.accessEnable) {
    lruLine(io.accessSet) := !io.accessWay
  }
  io.lruLine := lruLine(io.accessSet)
}
