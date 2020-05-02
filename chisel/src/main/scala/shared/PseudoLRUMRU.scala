package shared

import chisel3._
import chisel3.util._
import chisel3.util.random._

class PseudoLRUMRU(numOfWay: Int, numOfSets: Int) extends Module {
  require(numOfWay != 2, "number of way should not be 2. We have a true LRU for 2")
  require(isPow2(numOfWay), "number of way should be a power of 2")
  require(isPow2(numOfSets), "number of sets should be a power of 2")

  /**
    * access way is the newly accessed way
    * lru line is the output indicating which line is the lease recently used
    */
  val io = IO(new LRUIO(pNumOfSets = numOfSets, pNumOfWays = numOfWay))

  val lruReg = RegInit(VecInit(Seq.fill(numOfSets)(0.U(numOfWay.W))))
  when(io.accessEnable) {
    // reset all to 0 if all is 1 after the update
    val updatedAccessLine = lruReg(io.accessSet) | true.B << io.accessWay
    // when all mru except the newly accessed is 1
    when(updatedAccessLine.andR) {
      lruReg(io.accessSet) := ~(lruReg(io.accessSet))
    }.otherwise {
      lruReg(io.accessSet) := updatedAccessLine
    }
    assert(!lruReg(io.accessSet).andR, "all mru bits inside a set should not be all set to 1")
  }
  // generate a random number every cycle with the max number not exceeding way
  io.lruLine := 0.U
  //TODO: random eviction
  for (i <- 0 until numOfWay) {
    when(!lruReg(io.accessSet)(i)) {
      // multiple ways not used: evict one with lowest index
      io.lruLine := i.U
    }
  }
//  def update()
}

//object PseudoLRUMRU {
//  def apply(pNumOfWay: Int, pNumOfSets: Int): PseudoLRUMRU = {
//    Module(new PseudoLRUMRU(numOfWay = pNumOfWay, numOfSets = pNumOfSets))
//  }
//
//}
