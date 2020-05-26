package shared.LRU

import chisel3._
import chisel3.util._

/**
  * @param numOfSets how many sets there are in the cache
  * @param numOfWay how many ways there are in each set
  * @param searchOrder when it is false, search from backward to forward ( prioritize large index )
  */
class PseudoLRUMRU(numOfSets: Int, numOfWay: Int, searchOrder: Boolean = false) extends BaseLRU(pNumOfSets = numOfSets, pNumOfWays = numOfWay) {
  require(numOfWay != 2, "number of way should not be 2. We have a true LRU for 2")
  require(isPow2(numOfWay), "number of way should be a power of 2")
  require(isPow2(numOfSets), "number of sets should be a power of 2")

  // lruReg(setIndex)(wayIndex)
  val lruReg = RegInit(VecInit(Seq.fill(numOfSets)(VecInit(Seq.fill(numOfWay)(false.B)))))

  // set mru wire is mru bits in the accessed set with the new
  val setMRUWire = WireDefault(lruReg(io.accessSet))
  val invertedWire = WireDefault(lruReg(io.accessSet))

  invertedWire := 0.U.asTypeOf(invertedWire)
  invertedWire(io.accessWay) := true.B

  when(io.accessEnable) {
    setMRUWire(io.accessWay) := true.B

    // when all mru except the newly accessed is 1
    when(setMRUWire.asUInt.andR) {
      lruReg(io.accessSet) := invertedWire
    }.otherwise {
      lruReg(io.accessSet) := setMRUWire
    }
    assert(!lruReg(io.accessSet).asUInt.andR, "all mru bits inside a set should not be all set to 1")
  }
  // if it is a 1, don't take it.
  // if it is a 0, take the first element
  if (searchOrder) {
    io.lruLine := Mux(setMRUWire.asUInt.andR, invertedWire, setMRUWire).indexWhere((isMRU => !isMRU))
  } else {
    io.lruLine :=  Mux(setMRUWire.asUInt.andR, invertedWire, setMRUWire).lastIndexWhere((isMRU => !isMRU))
  }
}
