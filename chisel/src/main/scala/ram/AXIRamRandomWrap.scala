/*------------------------------------------------------------------------------
--------------------------------------------------------------------------------
Copyright (c) 2016, Loongson Technology Corporation Limited.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

3. Neither the name of Loongson Technology Corporation Limited nor the names of
its contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL LOONGSON TECHNOLOGY CORPORATION LIMITED BE LIABLE
TO ANY PARTY FOR DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
--------------------------------------------------------------------------------
------------------------------------------------------------------------------*/

package ram

import chisel3._
import axi.AXIIO
import soc.DelayType.{NoDelay, RandomDelay, StaticDelay}
import soc.SocConfig

/**
  * Input: ramRandomMask, from confreg
  */
class AXIRamRandomWrap()(implicit cfg: SocConfig) extends Module {
  val io = IO(new Bundle() {
    val axi           = AXIIO.slave()
    val ramRandomMask = Input(UInt(5.W))
  })

  val ram = Module(new AXIRam(Some(cfg.memFile)))

  val axi_arvalid_m_masked = Wire(Bool())
  val axi_rready_m_masked  = Wire(Bool())
  val axi_awvalid_m_masked = Wire(Bool())
  val axi_wvalid_m_masked  = Wire(Bool())
  val axi_bready_m_masked  = Wire(Bool())

  val ar_and = Wire(Bool())
  val r_and  = Wire(Bool())
  val aw_and = Wire(Bool())
  val w_and  = Wire(Bool())
  val b_and  = Wire(Bool())

  val ar_nomask = RegInit(false.B)
  val aw_nomask = RegInit(false.B)
  val w_nomask  = RegInit(false.B)

  val pf_r2r = RegInit(0.U(5.W))
  val pf_b2b = RegInit(0.U(2.W))

  val pf_r2r_nomask = !pf_r2r.orR()
  val pf_b2b_nomask = !pf_b2b.orR()

  pf_r2r := Mux(axi_arvalid_m_masked && io.axi.ar.ready, 25.U, Mux(!pf_r2r_nomask, pf_r2r - 1.U, pf_r2r))
  pf_b2b := Mux(axi_awvalid_m_masked && io.axi.aw.ready, 3.U, Mux(!pf_b2b_nomask, pf_b2b - 1.U, pf_b2b))

  cfg.delayType match {
    case StaticDelay =>
      ar_and := true.B
      aw_and := true.B
      w_and  := true.B
      r_and  := pf_r2r_nomask
      b_and  := pf_b2b_nomask
    case NoDelay =>
      ar_and := true.B
      aw_and := true.B
      w_and  := true.B
      r_and  := true.B
      b_and  := true.B
    case RandomDelay =>
      ar_and := io.ramRandomMask(4) | ar_nomask
      r_and  := io.ramRandomMask(3)
      aw_and := io.ramRandomMask(2) | aw_nomask
      w_and  := io.ramRandomMask(1) | w_nomask
      b_and  := io.ramRandomMask(0)
    case _ =>
      assert(false.B, "unknown delay type.")
  }

  ar_nomask := Mux(axi_awvalid_m_masked && io.axi.ar.ready, false.B, Mux(axi_arvalid_m_masked, true.B, ar_nomask))
  aw_nomask := Mux(axi_awvalid_m_masked && io.axi.aw.ready, false.B, Mux(axi_awvalid_m_masked, true.B, aw_nomask))
  w_nomask  := Mux(axi_wvalid_m_masked && io.axi.w.ready, false.B, Mux(axi_wvalid_m_masked, true.B, w_nomask))

  axi_arvalid_m_masked := io.axi.ar.valid & ar_and
  axi_rready_m_masked  := io.axi.r.ready & r_and
  axi_awvalid_m_masked := io.axi.aw.valid & aw_and
  axi_wvalid_m_masked  := io.axi.w.valid & w_and
  axi_bready_m_masked  := io.axi.b.ready & b_and

  ram.io.axi <> io.axi

  ram.io.axi.ar.valid := axi_arvalid_m_masked
  io.axi.ar.ready     := ram.io.axi.ar.ready & ar_and

  io.axi.r.valid     := ram.io.axi.r.valid & r_and
  ram.io.axi.r.ready := axi_rready_m_masked

  ram.io.axi.aw.valid := axi_awvalid_m_masked
  io.axi.aw.ready     := ram.io.axi.aw.ready & aw_and

  ram.io.axi.w.valid := axi_wvalid_m_masked
  io.axi.w.ready     := ram.io.axi.w.ready & w_and

  io.axi.b.valid     := ram.io.axi.b.valid & b_and
  ram.io.axi.b.ready := axi_bready_m_masked

}
