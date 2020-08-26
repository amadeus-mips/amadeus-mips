package axi

import chisel3._
import chisel3.util._

/**
  * separated read and write transaction
 *
  * @param sCount the number of slave interface
  */
//noinspection DuplicatedCode
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

  val sRIdle :: sRTran :: sRBusy :: Nil = Enum(3)

  val rState      = RegInit(sRIdle)
  val rSelect_reg = RegInit(0.U(log2Ceil(sCount).W))
  switch(rState) {
    is(sRIdle) {
      when(io.slaves(rChosen).ar.valid) {
        rState      := Mux(io.master.ar.ready, sRBusy, sRTran)
        rSelect_reg := rChosen
      }
    }
    is(sRTran) {
      when(io.master.r.fire() && io.master.r.bits.last) {
        rState := sRIdle
      }.elsewhen(io.master.ar.fire()) {
        rState := sRBusy
      }
    }
    is(sRBusy) {
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
  io.master.ar.bits := io.slaves(rSelect).ar.bits
  io.master.ar.valid := rState =/= sRBusy && io.slaves(rSelect).ar.valid
  io.slaves(rSelect).ar.ready := rState =/= sRBusy && io.master.ar.ready
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
  val sWIdle :: sWTran :: sWBusy :: Nil = Enum(3)

  val wState      = RegInit(sWIdle)
  val wSelect_reg = RegInit(0.U(log2Ceil(sCount).W))

  val wHandShake = RegInit(false.B)
  switch(wState) {
    is(sWIdle) {
      when(io.slaves(wChosen).aw.valid) {
        wState      := Mux(io.master.aw.ready, sWBusy, sWTran)
        wSelect_reg := wChosen
      }
    }
    is(sWTran) {
      when(io.master.b.fire()) {
        wState := sWIdle
      }.elsewhen(io.master.aw.fire()) {
        wState := sWBusy
      }
    }
    is(sWBusy) {
      when(io.master.b.fire()) {
        wState := sWIdle
      }
    }
  }

  when(wHandShake && io.master.w.fire() && io.master.w.bits.last){
    wHandShake := false.B
  }.elsewhen(wState === sWIdle) {
    wHandShake := true.B
  }

  val wSelect = Mux(wState === sWIdle, wChosen, wSelect_reg)
  io.slaves
    .map(_.aw)
    .foreach(aw => {
      aw.ready := false.B
    })
  io.master.aw.bits := io.slaves(wSelect).aw.bits
  io.master.aw.valid := wState =/= sWBusy && io.slaves(wSelect).aw.valid
  io.slaves(wSelect).aw.ready := wState =/= sWBusy && io.master.aw.ready
  io.slaves
    .map(_.w)
    .foreach(w => {
      w.ready := false.B
    })
  io.master.w.bits := io.slaves(wSelect).w.bits
  io.master.w.valid := wState =/= sWIdle && wHandShake && io.slaves(wSelect).w.valid
  io.slaves(wSelect).w.ready := wState =/= sWIdle && wHandShake && io.master.w.ready
  io.slaves
    .map(_.b)
    .foreach(b => {
      b.bits  := io.master.b.bits
      b.valid := false.B
    })
  io.master.b <> io.slaves(wSelect).b

}
