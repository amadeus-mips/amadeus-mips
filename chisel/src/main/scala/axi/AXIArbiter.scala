package axi

import chisel3._
import chisel3.util._

/**
  * separated read and write transaction
  * @param sCount the number of slave interface
  */
class AXIArbiter(sCount: Int = 3) extends Module {
  val io = IO(new Bundle() {
    val slaves = Vec(sCount, AXIIO.slave())
    val master = AXIIO.master()
  })

  def chosen(request: Seq[Bool]): UInt = {
    val chosen = WireDefault((sCount - 1).U)
    for (i <- sCount - 2 to 0 by -1) {
      when(request(i)) {
        chosen := i.U
      }
    }
    chosen
  }

  //---------------------------------------------------------------------------
  //---------------------------read--------------------------------------------
  //---------------------------------------------------------------------------
  val rChosen = chosen(io.slaves.map(_.ar.valid))

  val sRIdle :: sRTran :: Nil = Enum(2)

  val rState      = RegInit(sRIdle)
  val rSelect_reg = RegInit(0.U(log2Ceil(sCount).W))
  switch(rState) {
    is(sRIdle) {
      when(io.slaves(rChosen).ar.valid) {
        rState      := sRTran
        rSelect_reg := rChosen
      }
    }
    is(sRTran) {
      when(io.master.r.fire() && io.master.r.bits.last) {
        rState := sRIdle
      }
    }
  }
  val rSelect = Mux(rState === sRIdle, rChosen, rSelect_reg)
  io.slaves
    .map(_.ar)
    .foreach(ar => {
      ar.ready := false.B
    })
  io.master.ar <> io.slaves(rSelect).ar
  io.slaves
    .map(_.r)
    .foreach(r => {
      r.bits  := io.master.r.bits
      r.valid := false.B
    })
  io.master.r <> io.slaves(rSelect).r

  //---------------------------------------------------------------------------
  //--------------------------write--------------------------------------------
  //---------------------------------------------------------------------------
  val wChosen                 = chosen(io.slaves.map(_.aw.valid))
  val sWIdle :: sWTran :: Nil = Enum(2)

  val wState      = RegInit(sWIdle)
  val wSelect_reg = RegInit(0.U(log2Ceil(sCount).W))
  switch(wState) {
    is(sWIdle) {
      when(io.slaves(wChosen).aw.valid) {
        wState      := sWTran
        wSelect_reg := wChosen
      }
    }
    is(sWTran) {
      when(io.master.b.fire()) {
        wState := sWIdle
      }
    }
  }

  val wSelect = Mux(wState === sWIdle, wChosen, wSelect_reg)
  io.slaves
    .map(_.aw)
    .foreach(aw => {
      aw.ready := false.B
    })
  io.master.aw <> io.slaves(wSelect).aw
  io.slaves
    .map(_.w)
    .foreach(w => {
      w.ready := false.B
    })
  io.master.w <> io.slaves(wSelect).w
  io.slaves
    .map(_.b)
    .foreach(b => {
      b.bits  := io.master.b.bits
      b.valid := false.B
    })
  io.master.b <> io.slaves(wSelect).b

}
