package cpu.common

import chisel3._

class MemReqBundle extends Bundle {

  /** virtual tag in a TLB Page
    * This *tag* denotes the tag of a virtual page
    * it's different from the tag of a cache line
    * which uniquely identify a line in the cache
    * */
  val virtualTag = UInt(20.W)

  /** physical index *within a virtual page*
    * this is, again, different from the index of a cache
    * which uniquely represents a position in the virtual page
    * */
  val physicalIndex = UInt(12.W)

  /** when write mask is 0, this is a read request
    * otherwise, this is a write request */
  val writeMask = UInt(4.W)

  /** the data to write into data cache or uncached unit */
  val writeData = UInt(32.W)
}
