package ram

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType
import shared.{AXIAddrBundle, AXIIO, Constants, Util}

/**
  * Only for simulation
  */
//noinspection DuplicatedCode
class AXIRam(memFile: String) extends Module {
  val size = 1 << 20
  val INCR = 1.U
  val WRAP = 2.U
  val io = IO(new Bundle() {
    val axi = AXIIO.slave()
  })
  val ar = io.axi.ar
  val r  = io.axi.r
  val aw = io.axi.aw
  val w  = io.axi.w
  val b  = io.axi.b

  def checkAddrChannel(): Unit = {
    Seq(ar, aw).foreach(a => {
      when(a.valid) {
        assert(a.bits.burst === INCR || a.bits.burst === WRAP, "Only support INCR and WRAP burst.")
        when(a.bits.burst === WRAP) {
          assert(
            Util.listHasElement(Seq(1.U, 3.U, 7.U, 15.U), a.bits.len),
            "Burst len should be 2, 4, 8, 16 for WRAP burst"
          )
        }
      }
    })
  }
  checkAddrChannel()

  val mem = Mem(math.ceil(size.toDouble / 4).toInt, UInt(32.W))
  loadMemoryFromFile(mem, memFile, MemoryLoadFileType.Hex)

  val wrapMap = Seq(
    1.U  -> 3.U,
    3.U  -> 4.U,
    7.U  -> 5.U,
    15.U -> 6.U
  )

  def nextAddr(rawAddr: UInt, burst: UInt, wrapSize: UInt): UInt = {
    require(
      rawAddr.getWidth == 32 && burst.getWidth == 2 && wrapSize.getWidth == 3,
      s"${rawAddr.getWidth}, ${burst.getWidth}, ${wrapSize.getWidth}"
    )
    MuxLookup(
      burst,
      0.U,
      Seq(
        INCR -> (rawAddr + 4.U),
        WRAP -> {
          val high = WireInit(t = UInt(32.W), ((rawAddr >> wrapSize) << wrapSize).asUInt())
          val low = WireInit(
            t = UInt(32.W),
            (((rawAddr + 4.U) << (32.U - wrapSize)).asUInt()(31, 0) >> (32.U - wrapSize)).asUInt()
          )
          high | low
        }
      )
    )
  }

  //-----------------------------------------------------------------------------
  //-------------------------- read channel -------------------------------------
  //-----------------------------------------------------------------------------
  val sRIdle :: sRBurst :: Nil = Enum(2)

  val rState = RegInit(sRIdle)

  val instRQ = Module(new Queue(new AXIAddrBundle(), 10))
  val dataRQ = Module(new Queue(new AXIAddrBundle(), 10))

  val currentID = Mux(instRQ.io.deq.valid, Constants.INST_ID, Constants.DATA_ID)

  val rID      = RegInit(0.U(4.W))
  val rBurst   = RegInit(0.U(2.W))
  val rLen     = RegInit(0.U(4.W))
  val rWrapSel = RegInit(0.U(3.W))
  val rRamAddr = RegInit(0.U(32.W))

  def initRead(a: AXIAddrBundle): Unit = {
    rLen     := a.len
    rBurst   := a.burst
    rRamAddr := a.addr
    rWrapSel := MuxLookup(a.len, 0.U, wrapMap)
  }

  switch(rState) {
    is(sRIdle) {
      when(instRQ.io.deq.ready) {
        initRead(instRQ.io.deq.bits)
        rID    := Constants.INST_ID
        rState := sRBurst
      }.elsewhen(dataRQ.io.deq.ready) {
        initRead(dataRQ.io.deq.bits)
        rID    := Constants.DATA_ID
        rState := sRBurst
      }
    }
    is(sRBurst) {
      when(r.ready) {
        when(rLen === 0.U) {
          rState := sRIdle
        }.otherwise {
          rRamAddr := nextAddr(rRamAddr, rBurst, rWrapSel)
          rLen     := rLen - 1.U
        }
      }
    }
  }

  instRQ.io.enq.valid := ar.ready && ar.valid && ar.bits.id === Constants.INST_ID
  instRQ.io.enq.bits  := ar.bits
  instRQ.io.deq.ready := rState === sRIdle && instRQ.io.deq.valid && currentID === Constants.INST_ID

  dataRQ.io.enq.valid := ar.ready && ar.valid && ar.bits.id === Constants.DATA_ID
  dataRQ.io.enq.bits  := ar.bits
  dataRQ.io.deq.ready := rState === sRIdle && dataRQ.io.deq.valid && currentID === Constants.DATA_ID

  ar.ready    := instRQ.io.enq.ready && dataRQ.io.enq.ready
  r.bits.id   := rID
  r.bits.data := mem.read((rRamAddr(19, 0) >> 2).asUInt())
  r.bits.resp := 0.U // fixed OKAY
  r.bits.last := rLen === 0.U
  r.valid     := rState === sRBurst

  //-----------------------------------------------------------------------------
  //-------------------------- write channel -------------------------------------
  //-----------------------------------------------------------------------------
  val sWIdle :: sWBurst :: sWResp :: Nil = Enum(3)

  val wState = RegInit(sWIdle)

  val dataWQ = Module(new Queue(new AXIAddrBundle(), 10))

  val wLen     = RegInit(0.U(4.W))
  val wBurst   = RegInit(0.U(2.W))
  val wWrapSel = RegInit(0.U(3.W))
  val wRamAddr = RegInit(0.U(32.W))
  switch(wState) {
    is(sWIdle) {
      when(dataWQ.io.deq.valid) {
        wLen     := dataWQ.io.deq.bits.len
        wBurst   := dataWQ.io.deq.bits.burst
        wWrapSel := MuxLookup(dataWQ.io.deq.bits.len, 0.U, wrapMap)
        wRamAddr := dataWQ.io.deq.bits.addr
        wState   := sWBurst
      }
    }
    is(sWBurst) {
      when(w.valid) {
        assert(!(wLen === 0.U ^ w.bits.last), "wLen conflict with w.last")
        when(wLen === 0.U) {
          wState := sWResp
        }.otherwise {
          wRamAddr := nextAddr(wRamAddr, wBurst, wWrapSel)
          wLen     := wLen - 1.U
        }
      }
    }
    is(sWResp) {
      when(b.ready) {
        wState := sWIdle
      }
    }
  }

  when(w.valid && wState === sWBurst) {
    val addr         = (wRamAddr(19, 0) >> 2).asUInt()
    val internalData = mem.read(addr)
    val writeData =
      Cat((3 to 0 by -1).map(i => Mux(w.bits.strb(i), w.bits.data(7 + 8 * i, 8 * i), internalData(7 + 8 * i, 8 * i))))
    mem.write(addr, writeData)
  }

  dataWQ.io.enq.valid := aw.ready && aw.valid && aw.bits.id === Constants.DATA_ID
  dataWQ.io.enq.bits  := aw.bits
  dataWQ.io.deq.ready := wState === sWIdle && dataWQ.io.deq.valid

  aw.ready    := dataWQ.io.enq.ready
  w.ready     := wState === sWBurst
  b.bits.id   := Constants.DATA_ID
  b.bits.resp := 0.U // fixed OKAY
  b.valid     := wState === sWResp

}
