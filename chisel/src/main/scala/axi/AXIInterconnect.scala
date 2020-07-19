/*

Copyright (c) 2018 Alex Forencich

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

Modified from
https://github.com/alexforencich/verilog-axi/blob/master/rtl/axi_interconnect.v
by Discreater
 */

package axi

import chisel3._
import chisel3.util._

class AXIInterconnect(cfg: AXIInterconnectConfig) extends Module {
  val clsCount = log2Ceil(cfg.sCount)
  val clmCount = log2Ceil(cfg.mCount)
  val io = IO(new Bundle {
    val slaves  = Vec(cfg.sCount, AXIIO.slave(cfg.dataWidth, cfg.addrWidth))
    val masters = Vec(cfg.mCount, AXIIO.master(cfg.dataWidth, cfg.addrWidth))
  })

  val sIdle :: sDecode :: sWrite :: sWriteResp :: sWriteDrop :: sRead :: sReadDrop :: sWaitIdle :: Nil = Enum(8)

  val state = RegInit(sIdle)

  /** match */
  val hit = RegInit(false.B)

  val m_select_reg = RegInit(0.U(cfg.clmCount.W))

  val axi_addr_reg       = RegInit(0.U.asTypeOf(new AXIAddrBundle(cfg.addrWidth)))
  val axi_addr_valid_reg = RegInit(false.B)
  val axi_region_reg     = RegInit(0.U(4.W))

  val axi_bresp_reg = RegInit(0.U(2.W))

  val s_axi_arready_reg = RegInit(VecInit(Seq.fill(cfg.sCount)(false.B)))
  val s_axi_awready_reg = RegInit(VecInit(Seq.fill(cfg.sCount)(false.B)))
  val s_axi_wready_reg  = RegInit(VecInit(Seq.fill(cfg.sCount)(false.B)))
  val s_axi_bvalid_reg  = RegInit(VecInit(Seq.fill(cfg.sCount)(false.B)))

  val m_axi_arvalid_reg = RegInit(VecInit(Seq.fill(cfg.mCount)(false.B)))
  val m_axi_awvalid_reg = RegInit(VecInit(Seq.fill(cfg.mCount)(false.B)))
  val m_axi_rready_reg  = RegInit(VecInit(Seq.fill(cfg.mCount)(false.B)))
  val m_axi_bready_reg  = RegInit(VecInit(Seq.fill(cfg.mCount)(false.B)))

  // internal datapath
  val s_axi_r_int = Wire(new AXIDataReadBundle(cfg.dataWidth))

  val s_axi_rvalid_int       = Wire(Bool())
  val s_axi_rready_int_reg   = RegInit(false.B)
  val s_axi_rready_int_early = Wire(Bool())

  val m_axi_w_int = Wire(new AXIDataWriteBundle(cfg.dataWidth))

  val m_axi_wvalid_int       = Wire(Bool())
  val m_axi_wready_int_reg   = RegInit(false.B)
  val m_axi_wready_int_early = Wire(Bool())

  io.slaves.zipWithIndex.foreach(zip => {
    val (slave, i) = zip
    slave.aw.ready    := s_axi_awready_reg(i)
    slave.w.ready     := s_axi_wready_reg(i)
    slave.b.bits.id   := axi_addr_reg.id
    slave.b.bits.resp := axi_bresp_reg
    slave.b.valid     := s_axi_bvalid_reg(i)
    slave.ar.ready    := s_axi_arready_reg(i)
  })

  io.masters.zipWithIndex.foreach(zip => {
    val (master, i) = zip
    master.aw.bits    := axi_addr_reg
    master.aw.bits.id := (if (cfg.forwardID) axi_addr_reg.id else 0.U)

    master.aw.valid := m_axi_awvalid_reg(i)

    master.b.ready := m_axi_bready_reg(i)

    master.ar.bits    := axi_addr_reg
    master.ar.bits.id := (if (cfg.forwardID) axi_addr_reg.id else 0.U)

    master.ar.valid := m_axi_arvalid_reg(i)

    master.r.ready := m_axi_rready_reg(i)
  })

  /** slave side mux */
  val s_select = Wire(UInt((if (cfg.clsCount > 0) cfg.clsCount else 1).W))

  val current_s_axi_aw      = io.slaves(s_select).aw.bits
  val current_s_axi_awvalid = io.slaves(s_select).aw.valid
  val current_s_axi_awready = io.slaves(s_select).aw.ready

  val current_s_axi_w      = io.slaves(s_select).w.bits
  val current_s_axi_wvalid = io.slaves(s_select).w.valid
  val current_s_axi_wready = io.slaves(s_select).w.ready

  // not-used
  val current_s_axi_b      = io.slaves(s_select).b.bits
  val current_s_axi_bvalid = io.slaves(s_select).b.valid
  val current_s_axi_bready = io.slaves(s_select).b.ready

  val current_s_axi_ar      = io.slaves(s_select).ar.bits
  val current_s_axi_arvalid = io.slaves(s_select).ar.valid
  val current_s_axi_arready = io.slaves(s_select).ar.ready

  // not-used
  val current_s_axi_r      = io.slaves(s_select).r.bits
  val current_s_axi_rvalid = io.slaves(s_select).r.valid
  val current_s_axi_rready = io.slaves(s_select).r.ready

  // master side mux
  val current_m_axi_aw      = io.masters(m_select_reg).aw.bits
  val current_m_axi_awvalid = io.masters(m_select_reg).aw.valid
  val current_m_axi_awready = io.masters(m_select_reg).aw.ready

  val current_m_axi_w      = io.masters(m_select_reg).w.bits
  val current_m_axi_wvalid = io.masters(m_select_reg).w.valid
  val current_m_axi_wready = io.masters(m_select_reg).w.ready

  val current_m_axi_b      = io.masters(m_select_reg).b.bits
  val current_m_axi_bvalid = io.masters(m_select_reg).b.valid
  val current_m_axi_bready = io.masters(m_select_reg).b.ready

  val current_m_axi_ar      = io.masters(m_select_reg).ar.bits
  val current_m_axi_arvalid = io.masters(m_select_reg).ar.valid
  val current_m_axi_arready = io.masters(m_select_reg).ar.ready

  val current_m_axi_r      = io.masters(m_select_reg).r.bits
  val current_m_axi_rvalid = io.masters(m_select_reg).r.valid
  val current_m_axi_rready = io.masters(m_select_reg).r.ready

  // arbiter instance
  val request     = Wire(Vec(cfg.sCount * 2, Bool()))
  val acknowledge = Wire(Vec(cfg.sCount * 2, Bool()))
  val grant       = Wire(new EncoderBundle(cfg.sCount * 2))

  val read = grant.encoded(0)
  s_select := grant.encoded >> 1

  val arbiter = Module(
    new Arbiter(PORTS = cfg.sCount * 2, TYPE = "ROUND_ROBIN", BLOCK = "ACKNOWLEDGE", LSB_PRIORITY = "HIGH")
  )
  arbiter.io.request     := Cat((cfg.sCount * 2 - 1 to 0 by -1).map(i => request(i))) // reverse it
  arbiter.io.acknowledge := Cat((cfg.sCount * 2 - 1 to 0 by -1).map(i => acknowledge(i))) // ^

  grant := arbiter.io.grant

  for (i <- 0 until cfg.sCount) {
    // request generation
    request(2 * i)     := io.slaves(i).aw.valid
    request(2 * i + 1) := io.slaves(i).ar.valid
    // acknowledge generation
    acknowledge(2 * i) := grant.bits(2 * i) && io.slaves(i).b.valid && io.slaves(i).b.ready
    acknowledge(2 * i + 1) := grant.bits(2 * i + 1) && io.slaves(i).r.valid && io
      .slaves(i)
      .r
      .ready && io.slaves(i).r.bits.last
  }

  hit := false.B
  for (i <- 0 until cfg.sCount) {
    s_axi_awready_reg(i) := false.B
    s_axi_wready_reg(i)  := false.B
    s_axi_bvalid_reg(i)  := s_axi_bvalid_reg(i) & !io.slaves(i).b.ready
    s_axi_arready_reg(i) := false.B
  }
  for (i <- 0 until cfg.mCount) {
    m_axi_awvalid_reg(i) := m_axi_awvalid_reg(i) & !io.masters(i).aw.ready
    m_axi_bready_reg(i)  := false.B
    m_axi_arvalid_reg(i) := m_axi_arvalid_reg(i) & !io.masters(i).ar.ready
    m_axi_rready_reg(i)  := false.B
  }
  s_axi_r_int      := current_m_axi_r
  s_axi_r_int.id   := axi_addr_reg.id
  s_axi_rvalid_int := false.B

  m_axi_w_int      := current_s_axi_w
  m_axi_wvalid_int := false.B

  switch(state) {
    is(sIdle) {
      // idle state; wait for arbitration
      when(grant.valid) {
        axi_addr_valid_reg := true.B
        when(read) {
          // reading
          axi_addr_reg                := current_s_axi_ar
          s_axi_arready_reg(s_select) := true.B
        }.otherwise {
          // writing
          axi_addr_reg                := current_s_axi_aw
          s_axi_awready_reg(s_select) := true.B
        }
        state := sDecode
      }.otherwise {
        state := sIdle
      }
    }
    is(sDecode) {
      // decode state; determine master interface
      hit := false.B
      val flag = Wire(Bool())
      flag := false.B
      if(cfg.mDefaultMatch != -1){
        m_select_reg := cfg.mDefaultMatch.U
      }
      for (i <- 0 until cfg.mCount) {
        for (j <- 0 until cfg.mRegions) {
          if (cfg.mAddrWidth(i)(j) != 0) {
            when(
              (!cfg.mSecure(i).B || !axi_addr_reg.prot(1)) &&
                ((Mux(read, cfg.mConnectWrite.asUInt(), cfg.mConnectRead.asUInt()) &
                  (1.U << (s_select + (i * cfg.sCount).U)).asUInt()) =/= 0.U) &&
                ((axi_addr_reg.addr >> cfg.mAddrWidth(i)(j)).asUInt() === (cfg.mBaseAddr(i)(j) >> cfg.mAddrWidth(i)(j))
                  .asUInt())
            ) {
              m_select_reg   := i.U
              axi_region_reg := j.U
              hit            := true.B
              flag           := true.B
            }
          }
        }
      }
      when(flag) {
        when(read) {
          // reading
          m_axi_rready_reg(m_select_reg) := s_axi_rready_int_early
          state                          := sRead
        }.otherwise {
          s_axi_wready_reg(s_select) := m_axi_wready_int_early
          state                      := sWrite
        }
      }.otherwise {
        // no match; return decode error
        when(read) {
          // reading
          state := sReadDrop
        }.otherwise {
          // writing
          axi_bresp_reg              := 3.U(2.W)
          s_axi_wready_reg(s_select) := true.B
          state                      := sWriteDrop
        }
      }
    }
    is(sWrite) {
      // write state; store and forward write data
      s_axi_wready_reg(s_select) := m_axi_wready_int_early

      when(axi_addr_valid_reg) {
        m_axi_awvalid_reg(m_select_reg) := true.B
      }
      axi_addr_valid_reg := false.B
      when(current_s_axi_wready && current_s_axi_wvalid) {
        m_axi_w_int      := current_s_axi_w
        m_axi_wvalid_int := true.B
        when(current_s_axi_w.last) {
          s_axi_wready_reg(s_select)     := false.B
          m_axi_bready_reg(m_select_reg) := true.B
          state                          := sWriteResp
        }.otherwise {
          state := sWrite
        }
      }.otherwise {
        state := sWrite
      }
    }
    is(sWriteResp) {
      // write response state; store and forward write response
      m_axi_bready_reg(m_select_reg) := true.B

      when(current_m_axi_bready && current_m_axi_bvalid) {
        m_axi_bready_reg(m_select_reg) := false.B
        axi_bresp_reg                  := current_m_axi_b.resp
        s_axi_bvalid_reg(s_select)     := true.B
        state                          := sWaitIdle
      }.otherwise {
        state := sWriteResp
      }
    }
    is(sWriteDrop) {
      // write drop state; drop write data
      s_axi_wready_reg(s_select) := true.B
      axi_addr_valid_reg         := false.B
      when(current_s_axi_wready && current_s_axi_wvalid) {
        s_axi_wready_reg(s_select) := false.B
        s_axi_bvalid_reg(s_select) := true.B
        state                      := sWaitIdle
      }.otherwise {
        state := sWriteDrop
      }
    }
    is(sRead) {
      // read state; store and forward read response
      m_axi_rready_reg(m_select_reg) := s_axi_rready_int_early
      when(axi_addr_valid_reg) {
        m_axi_arvalid_reg(m_select_reg) := true.B
      }
      axi_addr_valid_reg := false.B
      when(current_m_axi_rready && current_m_axi_rvalid) {
        s_axi_r_int      := current_m_axi_r
        s_axi_r_int.id   := axi_addr_reg.id
        s_axi_rvalid_int := true.B
        when(current_m_axi_r.last) {
          m_axi_rready_reg(m_select_reg) := true.B
          state                          := sWaitIdle
        }.otherwise {
          state := sRead
        }
      }.otherwise {
        state := sRead
      }
    }
    is(sReadDrop) {
      // read drop state; generate decode error read response

      s_axi_r_int.id   := axi_addr_reg.id
      s_axi_r_int.data := 0.U
      s_axi_r_int.resp := 3.U(2.W)
      s_axi_r_int.last := axi_addr_reg.len === 0.U
      s_axi_rvalid_int := true.B

      when(s_axi_rready_int_reg) {
        axi_addr_reg.len := axi_addr_reg.len - 1.U
        when(axi_addr_reg.len === 0.U) {
          state := sWaitIdle
        }.otherwise {
          state := sReadDrop
        }
      }.otherwise {
        state := sReadDrop
      }
    }
    is(sWaitIdle) {
      when(!grant.valid || (acknowledge.asUInt() =/= 0.U)) {
        state := sIdle
      }.otherwise {
        state := sWaitIdle
      }
    }
  }

  // output datapath logic (R channel)
  val s_axi_rid_reg    = RegInit(0.U(cfg.idWidth.W))
  val s_axi_rdata_reg  = RegInit(0.U(cfg.dataWidth.W))
  val s_axi_rresp_reg  = RegInit(0.U(2.W))
  val s_axi_rlast_reg  = RegInit(false.B)
  val s_axi_rvalid_reg = RegInit(VecInit(Seq.fill(cfg.sCount)(false.B)))

  val temp_s_axi_rid_reg    = RegInit(0.U(cfg.idWidth.W))
  val temp_s_axi_rdata_reg  = RegInit(0.U(cfg.dataWidth.W))
  val temp_s_axi_rresp_reg  = RegInit(0.U(2.W))
  val temp_s_axi_rlast_reg  = RegInit(false.B)
  val temp_s_axi_rvalid_reg = RegInit(false.B)

  // datapath control
  val store_axi_r_int_to_output  = Wire(Bool())
  val store_axi_r_int_to_temp    = Wire(Bool())
  val store_axi_r_temp_to_output = Wire(Bool())

  io.slaves.zipWithIndex.foreach(zip => {
    val (slave, i) = zip
    slave.r.bits.id   := s_axi_rid_reg
    slave.r.bits.data := s_axi_rdata_reg
    slave.r.bits.resp := s_axi_rresp_reg
    slave.r.bits.last := s_axi_rlast_reg
    slave.r.valid     := s_axi_rvalid_reg(i)
  })

  // enable ready input next cycle if output is ready or the temp reg will not be filled on the next cycle (output reg empty or no input)
  s_axi_rready_int_early := current_s_axi_rready | (!temp_s_axi_rvalid_reg & (!current_s_axi_rvalid | !s_axi_rvalid_int))

  // transfer sink ready state to source
  store_axi_r_int_to_output  := false.B
  store_axi_r_int_to_temp    := false.B
  store_axi_r_temp_to_output := false.B
  when(s_axi_rready_int_reg) {
    // input is ready
    when(current_s_axi_rready | !current_s_axi_rvalid) {
      // output is ready or currently not valid, transfer data to output
      s_axi_rvalid_reg(s_select) := s_axi_rvalid_int
      store_axi_r_int_to_output  := true.B
    }.otherwise {
      // output is not ready, store input in temp
      temp_s_axi_rvalid_reg   := s_axi_rvalid_int
      store_axi_r_int_to_temp := true.B
    }
  }.elsewhen(current_s_axi_rready) {
    // input is not ready, but output is ready
    s_axi_rvalid_reg(s_select) := temp_s_axi_rvalid_reg
    temp_s_axi_rvalid_reg      := false.B
    store_axi_r_temp_to_output := true.B
  }

  s_axi_rready_int_reg := s_axi_rready_int_early

  // datapath
  when(store_axi_r_int_to_output) {
    s_axi_rid_reg   := s_axi_r_int.id
    s_axi_rdata_reg := s_axi_r_int.data
    s_axi_rresp_reg := s_axi_r_int.resp
    s_axi_rlast_reg := s_axi_r_int.last
  }.elsewhen(store_axi_r_temp_to_output) {
    s_axi_rid_reg   := temp_s_axi_rid_reg
    s_axi_rdata_reg := temp_s_axi_rdata_reg
    s_axi_rresp_reg := temp_s_axi_rresp_reg
    s_axi_rlast_reg := temp_s_axi_rlast_reg
  }

  when(store_axi_r_int_to_temp) {
    temp_s_axi_rid_reg   := s_axi_r_int.id
    temp_s_axi_rdata_reg := s_axi_r_int.data
    temp_s_axi_rresp_reg := s_axi_r_int.resp
    temp_s_axi_rlast_reg := s_axi_r_int.last
  }

  // output datapath logic (W channel)
  val m_axi_wdata_reg  = RegInit(0.U(cfg.dataWidth.W))
  val m_axi_wstrb_reg  = RegInit(0.U(cfg.strbWidth.W))
  val m_axi_wlast_reg  = RegInit(false.B)
  val m_axi_wvalid_reg = RegInit(VecInit(Seq.fill(cfg.mCount)(false.B)))

  val temp_m_axi_wdata_reg  = RegInit(0.U(cfg.dataWidth.W))
  val temp_m_axi_wstrb_reg  = RegInit(0.U(cfg.strbWidth.W))
  val temp_m_axi_wlast_reg  = RegInit(false.B)
  val temp_m_axi_wvalid_reg = RegInit(false.B)

  // datapath control
  val store_axi_w_int_to_output  = Wire(Bool())
  val store_axi_w_int_to_temp    = Wire(Bool())
  val store_axi_w_temp_to_output = Wire(Bool())

  io.masters.zipWithIndex.foreach(zip => {
    val (master, i) = zip
    master.w.bits.id   := io.slaves(m_select_reg).w.bits.id
    master.w.bits.data := m_axi_wdata_reg
    master.w.bits.strb := m_axi_wstrb_reg
    master.w.bits.last := m_axi_wlast_reg
    master.w.valid     := m_axi_wvalid_reg(i)
  })

  // enable ready input next cycle if output is ready or the temp reg will not be filled on the next cycle (output reg empty or no input)
  m_axi_wready_int_early := current_m_axi_wready | (!temp_m_axi_wvalid_reg & (!current_m_axi_wvalid | !m_axi_wvalid_int))

  // transfer sink ready state to source
  store_axi_w_int_to_output  := false.B
  store_axi_w_int_to_temp    := false.B
  store_axi_w_temp_to_output := false.B
  when(m_axi_wready_int_reg) {
    // input is ready
    when(current_m_axi_wready | !current_m_axi_wvalid) {
      // output is ready or currently not valid, transfer data to output
      m_axi_wvalid_reg(m_select_reg) := m_axi_wvalid_int
      store_axi_w_int_to_output      := true.B
    }.otherwise {
      // output is not ready, store input in temp
      temp_m_axi_wvalid_reg   := m_axi_wvalid_int
      store_axi_w_int_to_temp := true.B
    }
  }.elsewhen(current_m_axi_wready) {
    // input is not ready, but output is ready
    m_axi_wvalid_reg(m_select_reg) := temp_m_axi_wvalid_reg
    temp_m_axi_wvalid_reg          := false.B
    store_axi_w_temp_to_output     := true.B
  }

  m_axi_wready_int_reg := m_axi_wready_int_early

  // datapath
  when(store_axi_w_int_to_output) {
    m_axi_wdata_reg := m_axi_w_int.data
    m_axi_wstrb_reg := m_axi_w_int.strb
    m_axi_wlast_reg := m_axi_w_int.last
  }.elsewhen(store_axi_w_temp_to_output) {
    m_axi_wdata_reg := temp_m_axi_wdata_reg
    m_axi_wstrb_reg := temp_m_axi_wstrb_reg
    m_axi_wlast_reg := temp_m_axi_wlast_reg
  }
  when(store_axi_w_int_to_temp) {
    temp_m_axi_wdata_reg := m_axi_w_int.data
    temp_m_axi_wstrb_reg := m_axi_w_int.strb
    temp_m_axi_wlast_reg := m_axi_w_int.last
  }

}
