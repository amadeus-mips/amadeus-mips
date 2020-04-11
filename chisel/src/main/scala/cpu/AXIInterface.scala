// See README.md for license details.

package cpu

import chisel3._
import _root_.common.AXIIO
import chisel3.util._

class AXIInterface extends Module {
  val io = IO(new Bundle {
    val bus = AXIIO.master()
    val inst = AXIIO.slave()
    val data = AXIIO.slave()
    val flush = Input(Bool())
  })

  val sRIdle :: sARWait :: sARFinish :: sRWait :: sRFinish :: Nil = Enum(5)
  val rState = RegInit(sRIdle)
  val cnt = RegInit(0.U(2.W))
  val cntEn = RegInit(false.B)
  val arvalid_reg = RegInit(false.B)
  val rready_reg = RegInit(false.B)


  /** I forgot it :( */
  val rvalid_s = !(!io.flush && rState === sRWait && io.bus.r.valid && io.bus.r.ready && cntEn && cnt =/= 1.U) && io.bus.r.valid && io.bus.r.ready

  when(io.flush){
    rState := sRIdle
    arvalid_reg := false.B
    rready_reg := false.B
    cntEn := true.B
  }.otherwise{
    switch(rState) {
      is(sRIdle){
        when(io.bus.ar.valid && !io.bus.ar.ready){
          rState := sARWait
          arvalid_reg := true.B
        }.elsewhen(io.bus.ar.valid && io.bus.ar.ready){
          rState := sARFinish
          cnt := cnt + 1.U
          arvalid_reg := false.B
        }
      }
      is(sARWait){
        when(arvalid_reg && io.bus.ar.ready){
          rState := sARFinish
          cnt := cnt + 1.U
          arvalid_reg := false.B
        }
      }
      is(sARFinish){
        rState := sRWait
        rready_reg := true.B
      }
      is(sRWait){
        when(io.bus.r.valid && rready_reg){
          when(cntEn && cnt =/= 1.U){
            rready_reg := true.B
            when(io.bus.r.last){
              cnt := cnt - 1.U
            }
          }.elsewhen(io.bus.r.last) {
            rState := sRFinish
            cnt := 0.U
            rready_reg := false.B
          }
        }
      }
      is(sRFinish){
        rState := sRIdle
        cntEn := false.B
      }
    }
  }

  val sWIdle :: sAWWait :: sAWFinish :: sWWait :: sWFinish :: sBWait :: Nil = Enum(6)
  val wState = RegInit(sWIdle)
  val awvalid_reg = RegInit(false.B)
  val wvalid_reg = RegInit(false.B)
  val bready_reg = RegInit(false.B)

  switch(wState) {
    is(sWIdle){
      when(io.bus.aw.valid && !io.bus.aw.ready){
        wState := sAWWait
        awvalid_reg := true.B
        wvalid_reg := false.B
      }.elsewhen(io.bus.aw.valid && io.bus.aw.ready){
        wState := sAWFinish
        awvalid_reg := false.B
        wvalid_reg := false.B
      }.otherwise{
        awvalid_reg := false.B
        wvalid_reg := false.B
      }
    }
    is(sAWWait){
      when(awvalid_reg && io.bus.aw.ready){
        wState := sAWFinish
        awvalid_reg := false.B
      }
    }
    is(sAWFinish) {
      wState := sWWait
      wvalid_reg := true.B
    }
    is(sWWait){
      when(wvalid_reg && io.bus.w.ready){
        when(io.bus.w.last){
          wState := sWFinish
          wvalid_reg := false.B
        }
      }
    }
    is(sWFinish){
      wState := sBWait
      bready_reg := true.B
    }
    is(sBWait){
      when(bready_reg && io.bus.b.valid){
        wState := sWIdle
        bready_reg := false.B
      }
    }
  }

  io.inst.aw := DontCare
  io.inst.w := DontCare
  io.inst.b := DontCare
  io.inst.ar := DontCare
  io.data.ar := DontCare

  when(io.inst.ar.valid){
    io.bus.ar <> io.inst.ar
  }.otherwise {
    io.bus.ar <> io.data.ar
  }
  val arvalid_s = io.inst.ar.valid || io.data.ar.valid
  io.bus.ar.valid := !io.flush && Mux(rState === sRIdle, arvalid_s, arvalid_reg)

  io.inst.r <> io.bus.r
  io.data.r <> io.bus.r
  io.bus.r.ready := rready_reg
  io.data.r.valid := io.bus.r.valid && rready_reg

  io.bus.aw <> io.data.aw
  io.bus.aw.valid := Mux(wState === sWIdle, io.data.aw.valid, awvalid_reg)

  io.bus.w <> io.data.w
  io.bus.w.valid := wvalid_reg && io.data.w.valid
  io.data.w.ready := io.bus.w.ready && io.bus.w.valid

  io.bus.b <> io.data.b
  io.bus.b.ready := bready_reg
}
