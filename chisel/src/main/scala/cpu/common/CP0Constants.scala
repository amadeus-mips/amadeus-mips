package cpu.common

import chisel3._
import chisel3.util.log2Ceil
import cpu.CPUConfig

trait CP0Constants {
  val con_Index    = CP0Struct(0)
  val con_Random   = CP0Struct(1)
  val con_EntryLo0 = CP0Struct(2)
  val con_EntryLo1 = CP0Struct(3)
  val con_PageMask = CP0Struct(5)
  val con_Wired    = CP0Struct(6)
  val con_BadVAddr = CP0Struct(8)
  val con_Count    = CP0Struct(9)
  val con_EntryHi  = CP0Struct(10)
  val con_Status   = CP0Struct(12)
  val con_Cause    = CP0Struct(13)
  val con_EPC      = CP0Struct(14)
}

class CP0Struct(val addr: Int, val sel: Int, val writeMask: UInt) {
  require(addr >= 0 && addr <= 31 && sel >= 0 && sel <= 8)
  val index = addr * 8 + sel
}

object CP0Struct {
  def apply(addr: Int, sel: Int = 0, writeMask: UInt = "hffffffff".U): CP0Struct = new CP0Struct(addr, sel, writeMask)
}

trait BaseCP0 {
  val addr: Int
  val reg:  Data

  /** default soft read only */
  def softWrite(from: UInt): Unit = {
    require(from.getWidth == 32)
  }
  def raw:   UInt = reg.asUInt()
  val sel:   Int  = 0
  def index: Int  = addr * 8 + sel

}

class IndexBundle(tlbSize: Int) extends Bundle {
  private val tlbWidth = log2Ceil(tlbSize)
  val p                = Bool()
  val non              = UInt((31 - tlbWidth).W)
  val index            = UInt(tlbWidth.W)

  override def cloneType: IndexBundle.this.type = new IndexBundle(tlbSize).asInstanceOf[this.type]
}

class IndexCP0(tlbSize: Int) extends BaseCP0 {
  override val addr: Int = 0

  override val reg = RegInit(0.U.asTypeOf(new IndexBundle(tlbSize)))
  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg     := from.asTypeOf(new IndexBundle(tlbSize))
    reg.p   := reg.p
    reg.non := reg.non
  }
}
//noinspection DuplicatedCode
class RandomBundle(tlbSize: Int) extends Bundle {
  private val tlbWidth = log2Ceil(tlbSize)
  val non              = UInt((32 - tlbWidth).W)
  val random           = UInt(tlbWidth.W)

  override def cloneType: RandomBundle.this.type = new RandomBundle(tlbSize).asInstanceOf[this.type]
}

class RandomCP0(tlbSize: Int) extends BaseCP0 {
  override val addr: Int = 1
  override val reg = RegInit({
    val bundle = Wire(new RandomBundle(tlbSize))
    bundle.non    := 0.U
    bundle.random := (tlbSize - 1).U
    bundle
  })
}

class EntryLoBundle extends Bundle {
  val fill = UInt(2.W)
  val non  = UInt(4.W)
  val pfn  = UInt(20.W)

  val cacheControl = UInt(3.W)
  val dirty        = Bool()
  val valid        = Bool()
  val global       = Bool()
}
class EntryLoCP0(lo: Int) extends BaseCP0 {
  require(lo == 0 || lo == 1)
  override val addr: Int = lo + 2
  override val reg = RegInit(0.U.asTypeOf(new EntryLoBundle))

  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg      := from.asTypeOf(new EntryLoBundle)
    reg.fill := reg.fill
    reg.non  := reg.non
  }

}

class PageMaskBundle extends Bundle {
  val non  = UInt(7.W)
  val mask = UInt(12.W)
  val non1 = UInt(13.W)
}
class PageMaskCP0 extends BaseCP0 {
  override val addr: Int = 5
  override val reg = RegInit(0.U.asTypeOf(new PageMaskBundle))
  //noinspection DuplicatedCode
  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg      := from.asTypeOf(new PageMaskBundle)
    reg.non  := reg.non
    reg.non1 := reg.non1
  }

}

//noinspection DuplicatedCode
class WiredBundle(tlbSize: Int) extends Bundle {
  private val tlbWidth = log2Ceil(tlbSize)
  val non              = UInt((32 - tlbWidth).W)
  val wired            = UInt(tlbWidth.W)

  override def cloneType: WiredBundle.this.type = new WiredBundle(tlbSize).asInstanceOf[this.type]
}
class WiredCP0(tlbSize: Int) extends BaseCP0 {
  override val addr: Int = 6

  override val reg = RegInit(0.U.asTypeOf(new WiredBundle(tlbSize)))

  //noinspection DuplicatedCode
  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg     := from.asTypeOf(new WiredBundle(tlbSize))
    reg.non := reg.non
  }
}

class BadVAddrCP0 extends BaseCP0 {
  override val addr: Int = 8
  override val reg = RegInit(0.U(32.W))
}

class CountCP0 extends BaseCP0 {
  override val addr: Int = 9
  override val reg = RegInit(0.U(32.W))

  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg := from
  }
}

class EntryHiBundle extends Bundle {
  val vpn2  = UInt(19.W)
  val vpn2x = UInt(2.W)
  val non   = UInt(3.W)
  val asid  = UInt(8.W)
}
class EntryHiCP0 extends BaseCP0 {
  override val addr: Int = 10

  override val reg = RegInit(0.U.asTypeOf(new EntryHiBundle))

  //noinspection DuplicatedCode
  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg       := from.asTypeOf(new EntryHiBundle)
    reg.vpn2x := reg.vpn2x
    reg.non   := reg.non
  }
}

class StatusBundle extends Bundle {
  val cu    = UInt(4.W) // 31:28
  val rp    = Bool() // 27
  val fr    = Bool() // 26
  val re    = Bool() // 25
  val mx    = Bool() // 24
  val non   = UInt(1.W) // 23
  val bev   = Bool() // 22
  val ts    = Bool() // 21
  val sr    = Bool() // 20
  val nmi   = Bool() // 19
  val ase   = Bool() // 18
  val impl1 = UInt(2.W) // 17:16
  val IM    = UInt(8.W) // 15:8
  val non1  = UInt(3.W) // 7:5
  val um    = Bool() // 4
  val r0    = Bool() // 3
  val erl   = Bool() // 2
  val exl   = Bool() // 1
  val ie    = Bool() // 0
}
class StatusCP0 extends BaseCP0 {
  override val addr: Int = 12

  override val reg = RegInit({
    val bundle = WireDefault(0.U.asTypeOf(new StatusBundle))
    bundle.bev := true.B
    bundle.erl := true.B
    bundle
  })

  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg       := from.asTypeOf(new StatusBundle)
    reg.fr    := reg.fr
    reg.re    := reg.re
    reg.mx    := reg.mx
    reg.sr    := reg.sr
    reg.nmi   := reg.nmi
    reg.ase   := reg.ase
    reg.impl1 := reg.impl1
    reg.r0    := reg.r0
    reg.non   := reg.non
    reg.non1  := reg.non1
  }
}

class CauseBundle extends Bundle {
  val bd      = Bool() // 31
  val ti      = Bool() // 30
  val ce      = UInt(2.W) // 29:28
  val dc      = Bool() // 27
  val pci     = Bool() // 26
  val ase0    = UInt(2.W) // 25:24
  val iv      = Bool() // 23
  val wp      = Bool() // 22
  val fdci    = Bool() // 21
  val non     = UInt(3.W) // 20:18
  val ase1    = UInt(2.W) // 17:16
  val ipHard  = UInt(6.W) // 15:10
  val ipSoft  = UInt(2.W) // 9:8
  val non1    = Bool() // 7
  val excCode = UInt(5.W) // 6:2
  val non2    = UInt(2.W) // 1:0
}
class CauseCP0 extends BaseCP0 {
  override val addr: Int = 13

  override val reg = RegInit(0.U.asTypeOf(new CauseBundle))

  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg         := from.asTypeOf(new CauseBundle)
    reg.bd      := reg.bd
    reg.ti      := reg.ti
    reg.ce      := reg.ce
    reg.dc      := reg.dc
    reg.pci     := reg.pci
    reg.ase0    := reg.ase0
    reg.wp      := reg.wp
    reg.ase1    := reg.ase1
    reg.fdci    := reg.fdci
    reg.ipHard  := reg.ipHard
    reg.excCode := reg.excCode
    reg.non     := reg.non
    reg.non1    := reg.non1
    reg.non2    := reg.non2
  }
}

class EPCCP0 extends BaseCP0 {
  override val addr: Int = 14
  override val reg = RegInit(0.U(32.W))

  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg := from
  }
}

class EBaseBundle extends Bundle {

  /** fixed mapping to kseg0 */
  val upper  = UInt(2.W)
  val ebase  = UInt(18.W)
  val zero   = UInt(2.W)
  val CPUNum = UInt(10.W)
}

class EBaseCP0 extends BaseCP0 {
  override val addr: Int = 15
  override val sel = 1
  override val reg = RegInit({
    val bundle = WireInit(0.U(32.W).asTypeOf(new EBaseBundle))
    bundle.upper := "b10".U(2.W)
    bundle.zero  := 0.U(2.W)
    bundle
  })

  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg        := from.asTypeOf(new EBaseBundle)
    reg.upper  := "b10".U(2.W)
    reg.zero   := 0.U(2.W)
    reg.CPUNum := 0.U
  }
}

class ContextBundle extends Bundle {
  val pteBase      = UInt(9.W)
  val badVPN2      = UInt(19.W)
  val trailingZero = UInt(4.W)
}

class ContextCP0 extends BaseCP0 {
  override val addr = 4
  override val reg  = RegInit(0.U.asTypeOf(new ContextBundle))

  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg              := from.asTypeOf(new ContextBundle)
    reg.badVPN2      := reg.badVPN2
    reg.trailingZero := 0.U(4.W)
  }

}

class Config0Bundle extends Bundle {

  /** denotes the config1 is implemented */
  val m    = Bool()
  val k23  = UInt(3.W)
  val ku   = UInt(3.W)
  val impl = UInt(9.W)
  val be   = Bool()
  val at   = UInt(2.W)
  val ar   = UInt(3.W)
  val mt   = UInt(3.W)
  val non  = UInt(3.W)
  val vi   = Bool()
  val k0   = UInt(3.W)
}

class Config0CP0(implicit conf: CPUConfig) extends BaseCP0 {
  override val addr: Int = 16
  override val reg = RegInit({
    val bundle = WireInit(0.U.asTypeOf(new Config0Bundle))
    bundle.m  := true.B
    bundle.mt := 1.U
    bundle.k0 := 2.U
    bundle
  })

  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg   := from.asTypeOf(new Config0Bundle)
    reg.m := true.B
    // not fixed mapping mmu
    reg.k23 := 0.U
    reg.ku  := 0.U
    // little endian
    reg.be := false.B
    // MIPS32
    reg.at  := 0.U
    reg.ar  := 0.U
    reg.mt  := 1.U
    reg.non := 0.U
    reg.vi  := 0.U
  }
}

class Config1Bundle extends Bundle {
  val m       = Bool()
  val mmuSize = UInt(6.W)
  val is      = UInt(3.W)
  val il      = UInt(3.W)
  val ia      = UInt(3.W)
  val ds      = UInt(3.W)
  val dl      = UInt(3.W)
  val da      = UInt(3.W)
  val c2      = Bool()
  val md      = Bool()
  val pc      = Bool()
  val wr      = Bool()
  val ca      = Bool()
  val ep      = Bool()
  val fp      = Bool()
}

class Config1CP0(implicit conf: CPUConfig) extends BaseCP0 {
  override val addr: Int = 16
  override val sel:  Int = 1

  override val reg = RegInit({
    val bundle = WireInit(0.U.asTypeOf(new Config1Bundle))
    bundle.m       := false.B
    bundle.mmuSize := (conf.tlbSize - 1).U
    bundle.is := {
      conf.iCacheConfig.numOfSets match {
        case 64   => 0.U
        case 128  => 1.U
        case 256  => 2.U
        case 512  => 3.U
        case 1024 => 4.U
        case 2048 => 5.U
        case 4096 => 6.U
        case 32   => 7.U
      }
    }
    bundle.il := {
      conf.iCacheConfig.numOfBanks * 4 match {
        case 4   => 1.U
        case 8   => 2.U
        case 16  => 3.U
        case 32  => 4.U
        case 64  => 5.U
        case 128 => 6.U
      }
    }
    bundle.ia := (conf.iCacheConfig.numOfWays - 1).U
    bundle.ds := {
      conf.dCacheConfig.numOfSets match {
        case 64   => 0.U
        case 128  => 1.U
        case 256  => 2.U
        case 512  => 3.U
        case 1024 => 4.U
        case 2048 => 5.U
        case 4096 => 6.U
        case 32   => 7.U
      }
    }
    bundle.dl := {
      conf.dCacheConfig.numOfBanks * 4 match {
        case 4   => 1.U
        case 8   => 2.U
        case 16  => 3.U
        case 32  => 4.U
        case 64  => 5.U
        case 128 => 6.U
      }
    }
    bundle.da := (conf.dCacheConfig.numOfWays - 1).U
    bundle.c2 := false.B
    bundle.md := false.B
    bundle.pc := false.B
    bundle.wr := false.B
    bundle.ca := false.B
    bundle.ep := false.B
    bundle.fp := false.B
    bundle
  })
}

class CompareCP0 extends BaseCP0 {
  override val addr: Int = 11

  override val reg = RegInit(0.U(32.W))

  override def softWrite(from: UInt): Unit = {
    super.softWrite(from)
    reg := from
  }
}

class PRIDCP0 extends BaseCP0 {
  override val addr: Int = 15

  override val reg = RegInit("hffffffff".U)
}
