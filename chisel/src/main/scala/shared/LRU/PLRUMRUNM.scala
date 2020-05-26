package shared.LRU

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._

@chiselName
class PLRUMRUNM(numOfSets: Int, numOfWay: Int, searchOrder: Boolean = false) extends BaseLRUNM(numOfSets, numOfWay, searchOrder) {
  require(numOfWay >= 4, "number of way should not be equal to 2")
  require(isPow2(numOfWay), "number of way should be a power of 2")
  require(isPow2(numOfSets), "number of sets should be a power of 2")


  // lruReg(setIndex)(wayIndex)
  val lruReg = RegInit(VecInit(Seq.fill(numOfSets)(VecInit(Seq.fill(numOfWay)(false.B)))))

  /**
    * update the newly accessed way at index
    *
    * @param index the set accessed
    * @param way   which of the (4) sets?
    */
  override def update(index: UInt, way: UInt): Unit = {
    val setMRUWire = WireDefault(lruReg(index))
    setMRUWire(way) := true.B
    val invertedWire = WireDefault(0.U.asTypeOf(setMRUWire))
    invertedWire(way) := true.B
    lruReg(index) := Mux(setMRUWire.asUInt.andR, invertedWire, setMRUWire)
  }

  /**
    * TODO: reg is synchronous, is it necessary to handle update and getLRU at the same cycle?
    *
    * @param index the index of the set I'm seeking
    * @return the least recently used way
    */
  override def getLRU(index: UInt): UInt = {
    val setMRUWire = WireDefault(lruReg(index))
    if (searchOrder) {
      setMRUWire.indexWhere((isMRU => !isMRU))
    } else {
      setMRUWire.lastIndexWhere((isMRU => !isMRU))
    }
  }
}

object PLRUMRUNM {
  def apply(numOfSets: Int, numOfWay: Int, searchOrder: Boolean = false): PLRUMRUNM = new PLRUMRUNM(numOfSets, numOfWay, searchOrder)
}
