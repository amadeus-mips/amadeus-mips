package cpu.mmu

import chisel3._
import chisel3.util._

class PhysicalPage(physicalAddrW: Int) extends Bundle {
  val pfn = UInt(physicalAddrW.W)
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
  val pages = Vec(2, new PhysicalPage(physicalAddrWidth))
}

/**
  * the bundle for a TLB query, issued by Icache or memory unit
  *
  * @param vAddrW the address width of the virtual address
  */
class TLBQuery(vAddrW: Int) extends Bundle {
  val vAddr = UInt(vAddrW.W)
  val asid = UInt(8.W)
}

class TLBResult(TLBSize: Int, physicalAddrWidth: Int) extends Bundle {
  val hit = Bool()
  val mapped = Bool()
  val cached = Bool()
  val pageInfo = new PhysicalPage(physicalAddrWidth)
}

class TLBRWReq(TLBSize: Int, phyAddrWidth: Int) extends Bundle {
  val TLBIndex = UInt(log2Ceil(TLBSize).W)
  val writeEn = Bool()
  val writeData = new TLBEntry(phyAddrWidth)
}

