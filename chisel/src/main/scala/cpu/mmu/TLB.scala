package cpu.mmu

import chisel3._
import chisel3.util._

class PhysicalPage() extends Bundle {
  val pfn = UInt(20.W)
  val cacheControl = UInt(3.W)
  val valid = Bool()
  val dirty = Bool()
}

class TLBEntry(physicalAddrWidth: Int) extends Bundle {
  val global = Bool()
  val asid = UInt(8.W)
  val vpn2 = UInt(19.W)
  // Don't need a page mask, only supports 4k page
  //  val pageMask = UInt(12.W)
  val pages = Vec(2, new PhysicalPage)
}

/**
  * the bundle for a TLB query, issued by Icache or memory unit
  *
  */
class TLBQuery extends Bundle {
  val vAddr = UInt(20.W)
}

class TLBResult(TLBSize: Int, physicalAddrWidth: Int) extends Bundle {
  val hit = Bool()
  val mapped = Bool()
  val uncached = Bool()
  val pageInfo = new PhysicalPage(physicalAddrWidth)
}

class TLBRWReq(TLBSize: Int, phyAddrWidth: Int) extends Bundle {
  val TLBIndex = UInt(log2Ceil(TLBSize).W)
  val writeEn = Bool()
  val writeData = new TLBEntry(phyAddrWidth)
}

