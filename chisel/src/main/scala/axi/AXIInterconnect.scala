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
import common.{AXIAddrBundle, AXIIO}

class AXIInterconnect(cfg: AXIInterconnectConfig) extends Module {
  val clsCount = log2Ceil(cfg.sCount)
  val clmCount = log2Ceil(cfg.mCount)
  val io = IO(new Bundle {
    val slaves = Vec(cfg.sCount, AXIIO.slave(cfg.dataWidth, cfg.addrWidth))
    val masters = Vec(cfg.mCount, AXIIO.master(cfg.dataWidth, cfg.addrWidth))
  })

  val sIdle :: sDecode :: sWrite :: sWriteResp :: sWriteDrop :: sRead :: sReadDrop :: sWaitIdle :: Nil = Enum(8)
  val state = RegInit(sIdle)

  /** match */
  val hit = RegInit(false.B)

  val m_select_reg = RegInit(0.U(cfg.clmCount.W))
  val axi_id_reg = RegInit(0.U(cfg.idWidth.W))
  val axi_addr_reg = RegInit(0.U(cfg.addrWidth.W))
  val axi_addr_valid_reg = RegInit(false.B)
  val axi_len_reg = RegInit(0.U(4.W))
  val axi_size_reg = RegInit(0.U(3.W))
  val axi_burst_reg = RegInit(0.U(2.W))
  val axi_lock_reg = RegInit(0.U(2.W))
  val axi_cache_reg = RegInit(0.U(4.W))
  val axi_prot_reg = RegInit(0.U(3.W))
  val axi_qos_reg = RegInit(0.U(4.W))
  val axi_region_reg = RegInit(0.U(4.W))
//  val axi_auser_reg = RegInit(0.U(cfg.aUserWidth.W))
  val axi_bresp_reg = RegInit(0.U(2.W))
//  val axi_buser_reg = RegInit(0.U(cfg.bUserWidth.W))

  val s_axi_awready_reg = RegInit(VecInit(Seq.fill(cfg.sCount)(false.B)))
  val s_axi_wready_reg = RegInit(VecInit(Seq.fill(cfg.sCount)(false.B)))
  val s_axi_bvalid_reg = RegInit(VecInit(Seq.fill(cfg.sCount)(false.B)))
  val s_axi_arready_reg = RegInit(VecInit(Seq.fill(cfg.sCount)(false.B)))

  val m_axi_awvalid_reg = RegInit(VecInit(Seq.fill(cfg.mCount)(false.B)))
  val m_axi_bready_reg = RegInit(VecInit(Seq.fill(cfg.mCount)(false.B)))
  val m_axi_arvalid_reg = RegInit(VecInit(Seq.fill(cfg.mCount)(false.B)))
  val m_axi_rready_reg = RegInit(VecInit(Seq.fill(cfg.mCount)(false.B)))

  // internal datapath
  val s_axi_rid_int = Wire(UInt(cfg.idWidth.W))
  val s_axi_rdata_int = Wire(UInt(cfg.dataWidth.W))
  val s_axi_rresp_int = Wire(UInt(2.W))
  val s_axi_rlast_int = Wire(Bool())
//  val s_axi_ruser_int = Wire(UInt(cfg.rUserWidth.W))
  val s_axi_rvalid_int = Wire(Bool())
  val s_axi_rready_int_reg = RegInit(false.B)
  val s_axi_rready_int_early = Wire(Bool())

  val m_axi_wdata_int = Wire(UInt(cfg.dataWidth.W))
  val m_axi_wstrb_int = Wire(UInt(cfg.strbWidth.W))
  val m_axi_wlast_int = Wire(Bool())
//  val m_axi_wuser_int = Wire(UInt(cfg.wUserWidth.W))
  val m_axi_wvalid_int = Wire(Bool())
  val m_axi_wready_int_reg = RegInit(false.B)
  val m_axi_wready_int_early = Wire(Bool())

  io.slaves.zipWithIndex.foreach(zip => {
    val (slave, i) = zip
    slave.aw.ready := s_axi_awready_reg(i)
    slave.w.ready := s_axi_wready_reg(i)
    slave.b.bits.id := axi_id_reg
    slave.b.bits.resp := axi_bresp_reg
    //    slave.b.user := (if(cfg.bUserEnable) axi_buser_reg else 0.U)
    slave.b.valid := s_axi_bvalid_reg(i)
    slave.ar.ready := s_axi_arready_reg(i)
  })

  io.masters.zipWithIndex.foreach(zip => {
    val (master, i) = zip
    master.aw.bits.id := (if (cfg.forwardID) axi_id_reg else 0.U)
    master.aw.bits.addr := axi_addr_reg
    master.aw.bits.len := axi_len_reg
    master.aw.bits.size := axi_size_reg
    master.aw.bits.burst := axi_burst_reg
    master.aw.bits.lock := axi_lock_reg
    master.aw.bits.cache := axi_cache_reg
    master.aw.bits.prot := axi_prot_reg
    //    master.aw.qos = axi_qos_reg
    //    master.aw.region = axi_region_reg
    //    master.aw.user := (if(cfg.awUserEnable) axi_auser_reg(cfg.awUserWidth-1,0) else 0.U)
    master.aw.valid := m_axi_awvalid_reg(i)

    master.b.ready := m_axi_bready_reg(i)

    master.ar.bits.id := (if (cfg.forwardID) axi_id_reg else 0.U)
    master.ar.bits.addr := axi_addr_reg
    master.ar.bits.len := axi_len_reg
    master.ar.bits.size := axi_size_reg
    master.ar.bits.burst := axi_burst_reg
    master.ar.bits.lock := axi_lock_reg
    master.ar.bits.cache := axi_cache_reg
    master.ar.bits.prot := axi_prot_reg
    //    master.ar.qos := axi_qos_reg
    //    master.ar.region := axi_region_reg
    //    master.ar.user := (if(cfg.arUserEnable) axi_auser_reg(cfg.arUserWidth-1, 0) else 0.U)
    master.ar.valid := m_axi_arvalid_reg(i)

    master.r.ready := m_axi_rready_reg(i)
  })

  /** slave side mux */
  val s_select = Wire(UInt((if (cfg.clsCount > 0) cfg.clsCount - 1 else 0).W))

  val current_s_axi_awid = io.slaves(s_select).aw.bits.id
  val current_s_axi_awaddr = io.slaves(s_select).aw.bits.addr
  val current_s_axi_awlen = io.slaves(s_select).aw.bits.len
  val current_s_axi_awsize = io.slaves(s_select).aw.bits.size
  val current_s_axi_awburst = io.slaves(s_select).aw.bits.burst
  val current_s_axi_awlock = io.slaves(s_select).aw.bits.lock
  val current_s_axi_awcache = io.slaves(s_select).aw.bits.cache
  val current_s_axi_awprot = io.slaves(s_select).aw.bits.prot
  //  val current_s_axi_awqos     = io.slaves(s_select).aw.qos
  //  val current_s_axi_awuser    = io.slaves(s_select).aw.user
  val current_s_axi_awvalid = io.slaves(s_select).aw.valid
  val current_s_axi_awready = io.slaves(s_select).aw.ready
  val current_s_axi_wdata = io.slaves(s_select).w.bits.data
  val current_s_axi_wstrb = io.slaves(s_select).w.bits.strb
  val current_s_axi_wlast = io.slaves(s_select).w.bits.last
  //  val current_s_axi_wuser     = io.slaves(s_select).w.user
  val current_s_axi_wvalid = io.slaves(s_select).w.valid
  val current_s_axi_wready = io.slaves(s_select).w.ready
  val current_s_axi_bid = io.slaves(s_select).b.bits.id
  val current_s_axi_bresp = io.slaves(s_select).b.bits.resp
  //  val current_s_axi_buser     = io.slaves(s_select).b.user
  val current_s_axi_bvalid = io.slaves(s_select).b.valid
  val current_s_axi_bready = io.slaves(s_select).b.ready
  val current_s_axi_arid = io.slaves(s_select).ar.bits.id
  val current_s_axi_araddr = io.slaves(s_select).ar.bits.addr
  val current_s_axi_arlen = io.slaves(s_select).ar.bits.len
  val current_s_axi_arsize = io.slaves(s_select).ar.bits.size
  val current_s_axi_arburst = io.slaves(s_select).ar.bits.burst
  val current_s_axi_arlock = io.slaves(s_select).ar.bits.lock
  val current_s_axi_arcache = io.slaves(s_select).ar.bits.cache
  val current_s_axi_arprot = io.slaves(s_select).ar.bits.prot
  //  val current_s_axi_arqos     = io.slaves(s_select).ar.qos
  //  val current_s_axi_aruser    = io.slaves(s_select).ar.user
  val current_s_axi_arvalid = io.slaves(s_select).ar.valid
  val current_s_axi_arready = io.slaves(s_select).ar.ready
  val current_s_axi_rid = io.slaves(s_select).r.bits.id
  val current_s_axi_rdata = io.slaves(s_select).r.bits.data
  val current_s_axi_rresp = io.slaves(s_select).r.bits.resp
  val current_s_axi_rlast = io.slaves(s_select).r.bits.last
  //  val current_s_axi_ruser     = io.slaves(s_select).r.user
  val current_s_axi_rvalid = io.slaves(s_select).r.valid
  val current_s_axi_rready = io.slaves(s_select).r.ready

  // master side mux
  val current_m_axi_awid = io.masters(m_select_reg).aw.bits.id
  val current_m_axi_awaddr = io.masters(m_select_reg).aw.bits.addr
  val current_m_axi_awlen = io.masters(m_select_reg).aw.bits.len
  val current_m_axi_awsize = io.masters(m_select_reg).aw.bits.size
  val current_m_axi_awburst = io.masters(m_select_reg).aw.bits.burst
  val current_m_axi_awlock = io.masters(m_select_reg).aw.bits.lock
  val current_m_axi_awcache = io.masters(m_select_reg).aw.bits.cache
  val current_m_axi_awprot = io.masters(m_select_reg).aw.bits.prot
  //  val current_m_axi_awqos     = io.masters(m_select_reg).aw.qos
  //  val current_m_axi_awregion  = io.masters(m_select_reg).aw.region
  //  val current_m_axi_awuser    = io.masters(m_select_reg).aw.user
  val current_m_axi_awvalid = io.masters(m_select_reg).aw.valid
  val current_m_axi_awready = io.masters(m_select_reg).aw.ready
  val current_m_axi_wdata = io.masters(m_select_reg).w.bits.data
  val current_m_axi_wstrb = io.masters(m_select_reg).w.bits.strb
  val current_m_axi_wlast = io.masters(m_select_reg).w.bits.last
  //  val current_m_axi_wuser     = io.masters(m_select_reg).w.user
  val current_m_axi_wvalid = io.masters(m_select_reg).w.valid
  val current_m_axi_wready = io.masters(m_select_reg).w.ready
  val current_m_axi_bid = io.masters(m_select_reg).b.bits.id
  val current_m_axi_bresp = io.masters(m_select_reg).b.bits.resp
  //  val current_m_axi_buser     = io.masters(m_select_reg).b.user
  val current_m_axi_bvalid = io.masters(m_select_reg).b.valid
  val current_m_axi_bready = io.masters(m_select_reg).b.ready
  val current_m_axi_arid = io.masters(m_select_reg).ar.bits.id
  val current_m_axi_araddr = io.masters(m_select_reg).ar.bits.addr
  val current_m_axi_arlen = io.masters(m_select_reg).ar.bits.len
  val current_m_axi_arsize = io.masters(m_select_reg).ar.bits.size
  val current_m_axi_arburst = io.masters(m_select_reg).ar.bits.burst
  val current_m_axi_arlock = io.masters(m_select_reg).ar.bits.len
  val current_m_axi_arcache = io.masters(m_select_reg).ar.bits.cache
  val current_m_axi_arprot = io.masters(m_select_reg).ar.bits.prot
  //  val current_m_axi_arqos     = io.masters(m_select_reg).ar.qos
  //  val current_m_axi_arregion  = io.masters(m_select_reg).ar.region
  //  val current_m_axi_aruser    = io.masters(m_select_reg).ar.user
  val current_m_axi_arvalid = io.masters(m_select_reg).ar.valid
  val current_m_axi_arready = io.masters(m_select_reg).ar.ready
  val current_m_axi_rid = io.masters(m_select_reg).r.bits.id
  val current_m_axi_rdata = io.masters(m_select_reg).r.bits.data
  val current_m_axi_rresp = io.masters(m_select_reg).r.bits.resp
  val current_m_axi_rlast = io.masters(m_select_reg).r.bits.last
  //  val current_m_axi_ruser     = io.masters(m_select_reg).r.user
  val current_m_axi_rvalid = io.masters(m_select_reg).r.valid
  val current_m_axi_rready = io.masters(m_select_reg).r.ready

  // arbiter instance
  val request = Wire(Vec(cfg.sCount * 2, Bool()))
  val acknowledge = Wire(Vec(cfg.sCount * 2, Bool()))
  val grant = Wire(new EncoderBundle(cfg.sCount * 2))

  val read = grant.encoded(0)
  s_select := grant.encoded >> 1

  val arbiter = Module(new Arbiter(
    PORTS = cfg.sCount * 2,
    TYPE = "ROUND_ROBIN",
    BLOCK = "ACKNOWLEDGE",
    LSB_PRIORITY = "HIGH")
  )
  arbiter.io.request := Cat((cfg.sCount * 2 - 1 to 0 by -1) map (i => request(i))) // reverse it
  arbiter.io.acknowledge := Cat((cfg.sCount * 2 - 1 to 0 by -1) map (i => acknowledge(i))) // ^
  arbiter.io.grant <> grant

  for (i <- 0 until cfg.sCount) {
    // request generation
    request(2 * i) := io.slaves(i).aw.valid
    request(2 * i + 1) := io.slaves(i).ar.valid
    // acknowledge generation
    acknowledge(2 * i) := grant.bits(2 * i) && io.slaves(i).b.valid && io.slaves(i).b.ready
    acknowledge(2 * i + 1) := grant.bits(2 * i + 1) && io.slaves(i).r.valid && io.slaves(i).r.ready && io.slaves(i).r.bits.last
  }

  hit := false.B
  for (i <- 0 until cfg.sCount) {
    s_axi_awready_reg(i) := false.B
    s_axi_wready_reg(i) := false.B
    s_axi_bvalid_reg(i) := s_axi_bvalid_reg(i) & !io.slaves(i).b.ready
    s_axi_arready_reg(i) := false.B
  }
  for (i <- 0 until cfg.mCount) {
    m_axi_awvalid_reg(i) := m_axi_awvalid_reg(i) & !io.masters(i).aw.ready
    m_axi_bready_reg(i) := false.B
    m_axi_arvalid_reg(i) := m_axi_arvalid_reg(i) & !io.masters(i).ar.ready
    m_axi_rready_reg(i) := false.B
  }
  s_axi_rid_int := axi_id_reg
  s_axi_rdata_int := current_m_axi_rdata
  s_axi_rresp_int := current_m_axi_rresp
  s_axi_rlast_int := current_m_axi_rlast
  //  s_axi_ruser_int := current_m_axi_ruser
  s_axi_rvalid_int := false.B

  m_axi_wdata_int := current_s_axi_wdata
  m_axi_wstrb_int := current_s_axi_wstrb
  m_axi_wlast_int := current_s_axi_wlast
  //  m_axi_wuser_int := current_s_axi_wuser
  m_axi_wvalid_int := false.B

  switch(state) {
    is(sIdle) {
      // idle state; wait for arbitration
      when(grant.valid) {
        axi_addr_valid_reg := true.B
        when(read) {
          // reading
          axi_addr_reg := current_s_axi_araddr
          axi_prot_reg := current_s_axi_arprot
          axi_id_reg := current_s_axi_arid
          axi_addr_reg := current_s_axi_araddr
          axi_len_reg := current_s_axi_arlen
          axi_size_reg := current_s_axi_arsize
          axi_burst_reg := current_s_axi_arburst
          axi_lock_reg := current_s_axi_arlock
          axi_cache_reg := current_s_axi_arcache
          axi_prot_reg := current_s_axi_arprot
          //          axi_qos_reg := current_s_axi_arqos
          //          axi_auser_reg := current_s_axi_aruser
          s_axi_arready_reg(s_select) := true.B
        }.otherwise {
          // writing
          axi_addr_reg := current_s_axi_awaddr
          axi_prot_reg := current_s_axi_awprot
          axi_id_reg := current_s_axi_awid
          axi_addr_reg := current_s_axi_awaddr
          axi_len_reg := current_s_axi_awlen
          axi_size_reg := current_s_axi_awsize
          axi_burst_reg := current_s_axi_awburst
          axi_lock_reg := current_s_axi_awlock
          axi_cache_reg := current_s_axi_awcache
          axi_prot_reg := current_s_axi_awprot
          //          axi_qos_reg := current_s_axi_awqos
          //          axi_auser_reg := current_s_axi_awuser
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
      for (i <- 0 until cfg.mCount) {
        for (j <- 0 until cfg.mRegions) {
          val temp = axi_addr_reg >> cfg.mAddrWidth(i)(j).U
          if (cfg.mAddrWidth(i)(j) != 0) {
            when(
              (!cfg.mSecure(i).B || !axi_prot_reg(1)) &&
                ((Mux(read, cfg.mConnectWrite.asUInt(), cfg.mConnectRead.asUInt()) &
                  (1.U << (s_select + (i * cfg.sCount).U)).asUInt()) =/= 0.U) &&
                ((axi_addr_reg >> cfg.mAddrWidth(i)(j)).asUInt() === (cfg.mBaseAddr(i)(j) >> cfg.mAddrWidth(i)(j)).asUInt())
            ) {
              m_select_reg := i.U
              axi_region_reg := j.U
              hit := true.B
              flag := true.B
            }
          }
        }
      }
      when(flag) {
        when(read){
          // reading
          m_axi_rready_reg(m_select_reg) := s_axi_rready_int_early
          state := sRead
        }.otherwise{
          s_axi_wready_reg(s_select) := m_axi_wready_int_early
          state := sWrite
        }
      }.otherwise{
        // no match; return decode error
        when(read){
          // reading
          state := sReadDrop
        }otherwise{
          // writing
          axi_bresp_reg := 3.U(2.W)
          s_axi_wready_reg(s_select) := true.B
          state := sWriteDrop
        }
      }
    }
    is(sWrite){
      // write state; store and forward write data
      s_axi_wready_reg(s_select) := m_axi_wready_int_early

      when(axi_addr_valid_reg) {
        m_axi_awvalid_reg(m_select_reg) := true.B
      }
      axi_addr_valid_reg := false.B
      when(current_s_axi_wready && current_s_axi_wvalid){
        m_axi_wdata_int := current_s_axi_wdata
        m_axi_wstrb_int := current_s_axi_wstrb
        m_axi_wlast_int := current_s_axi_wlast
//        m_axi_wuser_int := current_s_axi_wuser
        m_axi_wvalid_int := true.B
        when(current_s_axi_wlast) {
          s_axi_wready_reg(s_select) := false.B
          m_axi_bready_reg(m_select_reg) := true.B
          state := sWriteResp
        }.otherwise{
          state := sWrite
        }
      }.otherwise{
        state := sWrite
      }
    }
    is(sWriteResp) {
      // write response state; store and forward write response
      m_axi_bready_reg(m_select_reg) := true.B

      when(current_m_axi_bready && current_m_axi_bvalid){
        m_axi_bready_reg(m_select_reg) := false.B
        axi_bresp_reg := current_m_axi_bresp
        s_axi_bvalid_reg(s_select) := true.B
        state := sWaitIdle
      }.otherwise{
        state := sWriteResp
      }
    }
    is(sWriteDrop){
      // write drop state; drop write data
      s_axi_wready_reg(s_select) := true.B
      axi_addr_valid_reg := false.B
      when(current_s_axi_wready && current_s_axi_wvalid){
        s_axi_wready_reg(s_select) := false.B
        s_axi_bvalid_reg(s_select) := true.B
        state := sWaitIdle
      }.otherwise{
        state := sWriteDrop
      }
    }
    is(sRead){
      // read state; store and forward read response
      m_axi_rready_reg(m_select_reg) := s_axi_rready_int_early
      when(axi_addr_valid_reg){
        m_axi_arvalid_reg(m_select_reg) := true.B
      }
      axi_addr_valid_reg := false.B
      when(current_m_axi_rready && current_m_axi_rvalid) {
        s_axi_rid_int := axi_id_reg
        s_axi_rdata_int := current_m_axi_rdata
        s_axi_rresp_int := current_m_axi_rresp
        s_axi_rlast_int := current_m_axi_rlast
//        s_axi_ruser_int := current_m_axi_ruser
        s_axi_rvalid_int := true.B
        when(current_m_axi_rlast){
          m_axi_rready_reg(m_select_reg) := true.B
          state := sWaitIdle
        }.otherwise{
          state := sRead
        }
      }.otherwise{
        state := sRead
      }
    }
    is(sReadDrop){
      // read drop state; generate decode error read response

      s_axi_rid_int := axi_id_reg
      s_axi_rdata_int := 0.U
      s_axi_rresp_int := 3.U(2.W)
      s_axi_rlast_int := axi_len_reg === 0.U
//      s_axi_ruser_int := 0.U
      s_axi_rvalid_int := true.B

      when(s_axi_rready_int_reg) {
        axi_len_reg := axi_len_reg - 1.U
        when(axi_len_reg === 0.U) {
          state := sWaitIdle
        }.otherwise{
          state := sReadDrop
        }
      }.otherwise{
        state := sReadDrop
      }
    }
    is(sWaitIdle){
      when(!grant.valid || acknowledge.asUInt().orR()){
        state := sIdle
      }.otherwise{
        state := sWaitIdle
      }
    }
  }

  // output datapath logic (R channel)
  val s_axi_rid_reg    = RegInit(0.U(cfg.idWidth.W))
  val s_axi_rdata_reg  = RegInit(0.U(cfg.dataWidth.W))
  val s_axi_rresp_reg  = RegInit(0.U(2.W))
  val s_axi_rlast_reg  = RegInit(false.B)
//  val s_axi_ruser_reg  = RegInit(0.U(cfg.rUserWidth.W))
  val s_axi_rvalid_reg = RegInit(VecInit(Seq.fill(cfg.sCount)(false.B)))

  val temp_s_axi_rid_reg    = RegInit(0.U(cfg.idWidth.W))
  val temp_s_axi_rdata_reg  = RegInit(0.U(cfg.dataWidth.W))
  val temp_s_axi_rresp_reg  = RegInit(0.U(2.W))
  val temp_s_axi_rlast_reg  = RegInit(false.B)
//  val temp_s_axi_ruser_reg  = RegInit(0.U(cfg.rUserWidth))
  val temp_s_axi_rvalid_reg = RegInit(false.B)

  // datapath control
  val store_axi_r_int_to_output = Wire(Bool())
  val store_axi_r_int_to_temp = Wire(Bool())
  val store_axi_r_temp_to_output = Wire(Bool())

  io.slaves.zipWithIndex.foreach(zip => {
    val (slave, i) = zip
    slave.r.bits.id := s_axi_rid_reg
    slave.r.bits.data := s_axi_rdata_reg
    slave.r.bits.resp := s_axi_rresp_reg
    slave.r.bits.last := s_axi_rlast_reg
//    slave.r.user := (if(cfg.rUserEnable) s_axi_ruser_reg else 0.U)
    slave.r.valid := s_axi_rvalid_reg(i)
  })

  // enable ready input next cycle if output is ready or the temp reg will not be filled on the next cycle (output reg empty or no input)
  s_axi_rready_int_early := current_s_axi_rready | (!temp_s_axi_rvalid_reg & (!current_s_axi_rvalid | !s_axi_rvalid_int))

  // transfer sink ready state to source
  store_axi_r_int_to_output := false.B
  store_axi_r_int_to_temp := false.B
  store_axi_r_temp_to_output := false.B
  when(s_axi_rready_int_reg){
    // input is ready
    when(current_s_axi_rready | !current_s_axi_rvalid){
      // output is ready or currently not valid, transfer data to output
      s_axi_rvalid_reg(s_select) := s_axi_rvalid_int
      store_axi_r_int_to_output := true.B
    }.otherwise{
      // output is not ready, store input in temp
      temp_s_axi_rvalid_reg := s_axi_rvalid_int
      store_axi_r_int_to_temp := true.B
    }
  }.elsewhen(current_s_axi_rready){
    // input is not ready, but output is ready
    s_axi_rvalid_reg(s_select) := temp_s_axi_rvalid_reg
    temp_s_axi_rvalid_reg := false.B
    store_axi_r_temp_to_output := true.B
  }

  s_axi_rready_int_reg := s_axi_rready_int_early

  // datapath
  when(store_axi_r_int_to_output){
    s_axi_rid_reg := s_axi_rid_int
    s_axi_rdata_reg := s_axi_rdata_int
    s_axi_rresp_reg := s_axi_rresp_int
    s_axi_rlast_reg := s_axi_rlast_int
//    s_axi_ruser_reg := s_axi_ruser_int
  }.elsewhen(store_axi_r_temp_to_output){
    s_axi_rid_reg := temp_s_axi_rid_reg
    s_axi_rdata_reg := temp_s_axi_rdata_reg
    s_axi_rresp_reg := temp_s_axi_rresp_reg
    s_axi_rlast_reg := temp_s_axi_rlast_reg
//    s_axi_ruser_reg := temp_s_axi_ruser_reg
  }

  when(store_axi_r_int_to_temp){
    temp_s_axi_rid_reg := s_axi_rid_int
    temp_s_axi_rdata_reg := s_axi_rdata_int
    temp_s_axi_rresp_reg := s_axi_rresp_int
    temp_s_axi_rlast_reg := s_axi_rlast_int
//    temp_s_axi_ruser_reg := s_axi_ruser_int
  }

  // output datapath logic (W channel)
  val m_axi_wdata_reg  = RegInit(0.U(cfg.dataWidth.W))
  val m_axi_wstrb_reg  = RegInit(0.U(cfg.strbWidth.W))
  val m_axi_wlast_reg  = RegInit(false.B)
//  val m_axi_wuser_reg  = RegInit(0.U(cfg.wUserWidth.W))
  val m_axi_wvalid_reg = RegInit(VecInit(Seq.fill(cfg.mCount)(false.B)))

  val temp_m_axi_wdata_reg  = RegInit(0.U(cfg.dataWidth.W))
  val temp_m_axi_wstrb_reg  = RegInit(0.U(cfg.strbWidth.W))
  val temp_m_axi_wlast_reg  = RegInit(false.B)
//  val temp_m_axi_wuser_reg  = RegInit(0.U(cfg.wUserWidth.W))
  val temp_m_axi_wvalid_reg = RegInit(false.B)

  // datapath control
  val store_axi_w_int_to_output = Wire(Bool())
  val store_axi_w_int_to_temp = Wire(Bool())
  val store_axi_w_temp_to_output = Wire(Bool())

  io.masters.zipWithIndex.foreach(zip => {
    val (master, i) = zip
    master.w.bits.id := io.slaves(m_select_reg).w.bits.id
    master.w.bits.data := m_axi_wdata_reg
    master.w.bits.strb := m_axi_wstrb_reg
    master.w.bits.last := m_axi_wlast_reg
//    master.w.user := (if(cfg.wUserEnable) m_axi_wuser_reg else 0.U)
    master.w.valid := m_axi_wvalid_reg(i)
  })

  // enable ready input next cycle if output is ready or the temp reg will not be filled on the next cycle (output reg empty or no input)
  m_axi_wready_int_early := current_m_axi_wready | (!temp_m_axi_wvalid_reg & (!current_m_axi_wvalid | !m_axi_wvalid_int))

  // transfer sink ready state to source
  store_axi_w_int_to_output := false.B
  store_axi_w_int_to_temp := false.B
  store_axi_w_temp_to_output := false.B
  when(m_axi_wready_int_reg){
    // input is ready
    when(current_m_axi_wready | !current_m_axi_wvalid){
      // output is ready or currently not valid, transfer data to output
      m_axi_wvalid_reg(m_select_reg) := m_axi_wvalid_int
      store_axi_w_int_to_output := true.B
    }.otherwise{
      // output is not ready, store input in temp
      temp_m_axi_wvalid_reg := m_axi_wvalid_int
      store_axi_w_int_to_temp := true.B
    }
  }.elsewhen(current_m_axi_wready){
    // input is not ready, but output is ready
    m_axi_wvalid_reg(m_select_reg) := temp_m_axi_wvalid_reg
    temp_m_axi_wvalid_reg := false.B
    store_axi_w_temp_to_output := true.B
  }

  m_axi_wready_int_reg := m_axi_wready_int_early

  // datapath
  when(store_axi_w_int_to_output) {
    m_axi_wdata_reg := m_axi_wdata_int
    m_axi_wstrb_reg := m_axi_wstrb_int
    m_axi_wlast_reg := m_axi_wlast_int
//    m_axi_wuser_reg := m_axi_wuser_int
  }.elsewhen(store_axi_w_temp_to_output){
    m_axi_wdata_reg := temp_m_axi_wdata_reg
    m_axi_wstrb_reg := temp_m_axi_wstrb_reg
    m_axi_wlast_reg := temp_m_axi_wlast_reg
//    m_axi_wuser_reg := temp_m_axi_wuser_reg
  }
  when(store_axi_w_int_to_temp) {
    temp_m_axi_wdata_reg := m_axi_wdata_int
    temp_m_axi_wstrb_reg := m_axi_wstrb_int
    temp_m_axi_wlast_reg := m_axi_wlast_int
//    temp_m_axi_wuser_reg := m_axi_wuser_int
  }

}
