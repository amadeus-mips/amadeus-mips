// See README.md for license details.

package cpu.core.components

import chisel3._
import chisel3.util._
import cpu.common._
import cpu.core.Constants._
import cpu.core.bundles.{CPBundle, TLBReadBundle}
import cpu.mmu.TLBEntry

class TLBHandleBundle(tlbSize: Int) extends Bundle {
  val entryHi  = new EntryHiBundle
  val pageMask = new PageMaskBundle
  val entryLo0 = new EntryLoBundle
  val entryLo1 = new EntryLoBundle
  val index    = new IndexBundle(tlbSize)
  val random   = new RandomBundle(tlbSize)

  override def cloneType: TLBHandleBundle.this.type = new TLBHandleBundle(tlbSize).asInstanceOf[this.type]
}

class ExceptionHandleBundle extends Bundle {
  val status = new StatusBundle
  val cause  = new CauseBundle
  val EPC    = UInt(dataLen.W)
}

class CP0IO(tlbSize: Int) extends Bundle {
  val intr        = Input(UInt(intrLen.W))
  val cp0Write    = Input(new CPBundle)
  val addr        = Input(UInt(regAddrLen.W))
  val sel         = Input(UInt(3.W))
  val except      = Input(Vec(exceptAmount, Bool()))
  val inDelaySlot = Input(Bool())
  val pc          = Input(UInt(addrLen.W))
  val badAddr     = Input(UInt(addrLen.W))

  val op = Input(UInt(opLen.W))
  val tlb = Input(new TLBReadBundle)

  val data = Output(UInt(dataLen.W))

  val exceptionCP0 = Output(new ExceptionHandleBundle)

  val tlbCP0 = Output(new TLBHandleBundle(tlbSize))

  override def cloneType: CP0IO.this.type = new CP0IO(tlbSize).asInstanceOf[this.type ]
}

class CP0(tlbSize: Int = 32) extends Module {
  val tlbWidth = log2Ceil(tlbSize)
  val io       = IO(new CP0IO(tlbSize))

  val index    = new IndexCP0(tlbSize)
  val random   = new RandomCP0(tlbSize)
  val entryLo0 = new EntryLoCP0(lo = 0)
  val entryLo1 = new EntryLoCP0(lo = 1)
  val pageMask = new PageMaskCP0
  val wired    = new WiredCP0(tlbSize)
  val badVAddr = new BadVAddrCP0
  val count    = new CountCP0
  val entryHi  = new EntryHiCP0
//  val status = RegInit(Cat(0.U(9.W), 1.U(1.W), 0.U(22.W))) ???
  val status = new StatusCP0
  val cause  = new CauseCP0
  val epc    = new EPCCP0

  val cp0Seq = Seq(index, random, entryLo0, entryLo1, pageMask, wired, badVAddr, count, entryHi, status, cause, epc)

  // soft write
  when(io.cp0Write.enable) {
    val c = io.cp0Write
    cp0Seq.foreach(cp0 => {
      when(cp0.index.U === Cat(c.addr, c.sel)) {
        cp0.softWrite(c.data)
      }
    })
  }

  val tick = RegInit(false.B)
  tick := !tick

  val except = io.except.asUInt().orR()

  def compareWriteCP0(p: BaseCP0): Bool = {
    val c = io.cp0Write
    c.enable && c.addr === p.addr.U && c.sel === p.sel.U
  }

  val excCode =
    MuxCase(
      cause.reg.excCode,
      Seq(
        io.except(EXCEPT_INTR)         -> 0.U(5.W), // int
        io.except(EXCEPT_FETCH)        -> "h04".U(5.W), // AdEL
        io.except(EXCEPT_INST_INVALID) -> "h0a".U(5.W), // RI
        io.except(EXCEPT_OVERFLOW)     -> "h0c".U(5.W), // Ov
        io.except(EXCEPT_SYSCALL)      -> "h08".U(5.W), // Sys
        io.except(EXCEPT_BREAK)        -> "h09".U(5.W), // Break
        io.except(EXCEPT_LOAD)         -> "h04".U(5.W), // AdEL
        io.except(EXCEPT_STORE)        -> "h05".U(5.W) // AdES
      )
    )

  // hard write
  // TODO add hardware write index entryLo0/1 pageMask entryHi
  when(io.op === TLB_R) {
    entryHi.reg.vpn2 := io.tlb.readResp.vpn2
    entryHi.reg.asid :=  io.tlb.readResp.asid
    pageMask.reg.mask := 0.U
    Seq(entryLo0.reg, entryLo1.reg).zip(io.tlb.readResp.pages).foreach(e => {
      e._1.pfn := e._2.pfn
      e._1.cacheControl := e._2.cacheControl
      e._1.valid := e._2.valid
      e._1.dirty := e._2.dirty
      e._1.global := io.tlb.readResp.global
    })
  }.elsewhen(io.op === TLB_P) {
    index.reg.p := io.tlb.probeResp(31)
    index.reg.index := io.tlb.probeResp(tlbWidth-1, 0)
  }

  when(compareWriteCP0(wired)) {
    random.reg.random := (tlbSize - 1).U
  }.elsewhen(io.op === TLB_WR) {
    random.reg.random := Mux(random.reg.random.andR(), wired.reg.wired, random.reg.random + 1.U)
  }

  when(io.except(EXCEPT_FETCH) || io.except(EXCEPT_LOAD) || io.except(EXCEPT_STORE)) {
    badVAddr.reg := io.badAddr
  }

  /** increase 1 every two cycle */
  when(!compareWriteCP0(count) && tick) {
    count.reg := count.reg + 1.U
  }

  when(except) {
    status.reg.exl := true.B
  }

  when(except) {
    cause.reg.bd := io.inDelaySlot
  }
  cause.reg.ipHard  := io.intr
  cause.reg.excCode := excCode

  when(except) {
    epc.reg := Mux(io.inDelaySlot, io.pc - 4.U, io.pc)
  }

  io.data := MuxLookup(
    Cat(io.addr, io.sel),
    0.U,
    cp0Seq.map(e => e.index.U -> e.raw)
  )

  io.exceptionCP0.status := status.reg
  io.exceptionCP0.cause  := cause.reg
  io.exceptionCP0.EPC    := epc.reg
  io.tlbCP0.index        := index.reg
  io.tlbCP0.random       := random.reg
  io.tlbCP0.pageMask     := pageMask.reg
  io.tlbCP0.entryHi      := entryHi.reg
  io.tlbCP0.entryLo0     := entryLo0.reg
  io.tlbCP0.entryLo1     := entryLo1.reg
}
