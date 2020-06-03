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

class TLBQuery(vAddrW: Int) extends Bundle {
  val vAddr = UInt(vAddrW.W)
  val asid = UInt(8.W)
}

class TLBResult(TLBSize: Int, physicalAddrWidth: Int) extends Bundle {
  val hit = Bool()
  val hitIndex = UInt(log2Ceil(TLBSize).W)
  val pageInfo = new PhysicalPage(physicalAddrWidth)
  val exception = UInt(3.W)
}

trait TLBException {
  val NONE = 0.U(3.W)
}

class TLBRWReq(addrWidth: Int) extends Bundle {
  val TLBIndex = UInt(addrWidth.W)
  val writeEn = Bool()
  val writeData = new TLBEntry(addrWidth)
}


class TLBLookup(virtualAddrWidth: Int = 20, physicalAddrWidth: Int, TLBSize: Int) extends Module {
  val io = IO(new Bundle {
    val query = Input(new TLBQuery(virtualAddrWidth))
    val result = Output(new TLBResult(TLBSize, physicalAddrWidth))
  })

  val physicalTLB = RegInit(VecInit(Seq.fill(TLBSize)(new TLBEntry(physicalAddrWidth))))

  val hitWire = Wire(Vec(TLBSize, Bool()))
  hitWire := physicalTLB map ((entry: TLBEntry) => (entry.vpn2 === io.query.vAddr(virtualAddrWidth - 1, 1)) && ((entry.asid === io.query.asid) || (entry.global)))

  val hitIndex = Wire(UInt(log2Ceil(TLBSize).W))
  hitIndex := hitWire.indexWhere((b: Bool) => b)

  io.result.hit := hitWire.contains(true.B)
  io.result.hitIndex := hitIndex
  io.result.pageInfo := physicalTLB(hitIndex).pages(io.query.vAddr(0))

}
