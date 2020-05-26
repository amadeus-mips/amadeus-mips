package shared.LRU

import chisel3._

abstract class BaseLRUNM(numOfSets: Int, numOfWay: Int, searchOrder: Boolean = false) {
  def update(index: UInt, way: UInt): Unit

  def getLRU(index: UInt): UInt

}
