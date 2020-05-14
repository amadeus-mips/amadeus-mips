package shared

import chisel3._
import chisel3.util._
import chisel3.util.random._

/**
  * @note Don't read from its output when updating it
  * @param numOfSets how many sets there are in the cache
  * @param numOfWay how many ways there are in each set
  * @param searchOrder when it is false, search from backward to forward ( prioritize large index )
  */
class PseudoLRUMRU(numOfSets: Int, numOfWay: Int, searchOrder: Boolean = false) extends Module {
  require(numOfWay != 2, "number of way should not be 2. We have a true LRU for 2")
  require(isPow2(numOfWay), "number of way should be a power of 2")
  require(isPow2(numOfSets), "number of sets should be a power of 2")

  /**
    * access way is the newly accessed way
    * lru line is the output indicating which line is the lease recently used
    */
  val io = IO(new LRUIO(pNumOfSets = numOfSets, pNumOfWays = numOfWay))

  // lruReg(setIndex)(wayIndex)
  val lruReg = RegInit(VecInit(Seq.fill(numOfSets)(VecInit(Seq.fill(numOfWay)(false.B)))))

  when(io.accessEnable) {
    // lru line of set is lru status of set before the update
    val lruLineofSet = WireDefault(lruReg(io.accessSet))
    lruLineofSet(io.accessWay) := true.B

    // when all mru except the newly accessed is 1
    when(lruLineofSet.asUInt.andR) {
      lruReg(io.accessSet) := 0.U.asTypeOf(lruReg(io.accessSet))
      lruReg(io.accessSet)(io.accessWay) := true.B
    }.otherwise {
      lruReg(io.accessSet) := lruLineofSet
    }
    assert(!lruReg(io.accessSet).asUInt.andR, "all mru bits inside a set should not be all set to 1")
  }
  // if it is a 1, don't take it.
  // if it is a 0, take the first element
  if (searchOrder) {
    io.lruLine := lruReg(io.accessSet).indexWhere((isMRU => !isMRU))
  } else {
    io.lruLine := lruReg(io.accessSet).lastIndexWhere((isMRU => !isMRU))
  }
}
