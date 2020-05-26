// See README.md for license details.

package cpu

import chisel3._
import chisel3.util._
import axi.AXIIO

class AXIInterface extends Module {
  val io = IO(new Bundle {
    val bus = AXIIO.master()
//    val inst = AXIIO.slave()
    val data = AXIIO.slave()
  })

  val sRIdle :: sARWait :: sARFinish :: sRWait :: sRFinish :: Nil = Enum(5)
  val rState = RegInit(sRIdle)
  val arvalid_reg = RegInit(false.B)
  val rready_reg = RegInit(true.B)

  /** I forgot it :( */
  val rvalid_s =
    !(rState === sRWait && io.bus.r.valid && io.bus.r.ready) && io.bus.r.valid && io.bus.r.ready

  switch(rState) {
    is(sRIdle) {
      // wait for the slave to become ready
      when(io.bus.ar.valid && !io.bus.ar.ready) {
        rState := sARWait
        arvalid_reg := true.B
        // TODO: waht is this
      }.elsewhen(io.bus.ar.valid && io.bus.ar.ready) {
        rState := sARFinish
        arvalid_reg := false.B
      }
    }
    is(sARWait) {
      when(arvalid_reg && io.bus.ar.ready) {
        rState := sARFinish
        arvalid_reg := false.B
      }
    }
    is(sARFinish) {
      rState := sRWait
      rready_reg := true.B
    }
    is(sRWait) {
      when(io.bus.r.valid && rready_reg) {
        when(io.bus.r.bits.last) {
          rState := sRFinish
          rready_reg := false.B
        }
      }
    }
    // why is there a seperate r finish state?
    is(sRFinish) {
      rState := sRIdle
    }
  }

  val sWIdle :: sAWWait :: sAWFinish :: sWWait :: sWFinish :: sBWait :: Nil = Enum(6)
  val wState = RegInit(sWIdle)
  val awvalid_reg = RegInit(false.B)
  val wvalid_reg = RegInit(false.B)
  val bready_reg = RegInit(false.B)

  switch(wState) {
    is(sWIdle) {
      when(io.bus.aw.valid && !io.bus.aw.ready) {
        wState := sAWWait
        awvalid_reg := true.B
        wvalid_reg := false.B
      }.elsewhen(io.bus.aw.valid && io.bus.aw.ready) {
          wState := sAWFinish
          awvalid_reg := false.B
          wvalid_reg := false.B
        }
        .otherwise {
          awvalid_reg := false.B
          wvalid_reg := false.B
        }
    }
    is(sAWWait) {
      when(awvalid_reg && io.bus.aw.ready) {
        wState := sAWFinish
        awvalid_reg := false.B
      }
    }
    is(sAWFinish) {
      wState := sWWait
      wvalid_reg := true.B
    }
    is(sWWait) {
      when(wvalid_reg && io.bus.w.ready) {
        when(io.bus.w.bits.last) {
          wState := sWFinish
          wvalid_reg := false.B
        }
      }
    }
    is(sWFinish) {
      wState := sBWait
      bready_reg := true.B
    }
    is(sBWait) {
      when(bready_reg && io.bus.b.valid) {
        wState := sWIdle
        bready_reg := false.B
      }
    }
  }

  io.data.ar := DontCare

  io.bus.ar <> io.data.ar
  val arvalid_s = io.data.ar.valid
  io.bus.ar.valid := Mux(rState === sRIdle, arvalid_s, arvalid_reg)

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
