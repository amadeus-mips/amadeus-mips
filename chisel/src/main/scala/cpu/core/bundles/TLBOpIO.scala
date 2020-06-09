package cpu.core.bundles

import chisel3._
import cpu.mmu.{TLBEntry, TLBRWReq}

class TLBOpIO(tlbSize: Int = 32) extends Bundle {
  val asid          = Output(UInt(8.W))
  val kseg0Uncached = Output(UInt(8.W))
  val instrReq      = Output(new TLBRWReq(tlbSize))
  val readResp      = Input(new TLBEntry())
  val probeReq      = Output(UInt(20.W))
  val probeResp     = Input(UInt(32.W))

  val except = Input(new Bundle {
    val inst = new Bundle {
      val refill  = Bool()
      val invalid = Bool()
    }
    val data = new Bundle {
      val refill   = Bool()
      val invalid  = Bool()
      val modified = Bool()
    }
  })
  override def cloneType: TLBOpIO.this.type = new TLBOpIO(tlbSize).asInstanceOf[this.type]
}

class TLBReadBundle extends Bundle {
  val readResp  = new TLBEntry
  val probeResp = UInt(32.W)
}
