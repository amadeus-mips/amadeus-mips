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

//*************************************************************************
//   > File Name   : confreg.v
//   > Description : Control module of
//                   16 red leds, 2 green/red leds,
//                   7-segment display,
//                   switchs,
//                   key board,
//                   bottom STEP,
//                   timer.
//
//   > Author      : LOONGSON
//   > Date        : 2017-08-04
//*************************************************************************

/** Modified by Discreater */
package confreg

import chisel3._
import chisel3.util._
import shared.{AXIIO, GPIO}

/**
  * If you want to add address map,  you need to modify function [[axi_interfaceHandle()]] at least.
  *
  * @param simulation is simulate environment
  */
class Confreg(simulation: Boolean = false) extends Module {
  val RANDOM_SEED = Cat("b1010101".U(7.W), "h00ff".U(16.W))
  val CR_ADDRS = Seq(
    "h8000".U(16.W), //32'hbfaf_8000
    "h8004".U(16.W), //32'hbfaf_8004
    "h8008".U(16.W), //32'hbfaf_8008
    "h800c".U(16.W), //32'hbfaf_800c
    "h8010".U(16.W), //32'hbfaf_8010
    "h8014".U(16.W), //32'hbfaf_8014
    "h8018".U(16.W), //32'hbfaf_8018
    "h801c".U(16.W), //32'hbfaf_801c
  )
  // @formatter:off
  val LED_ADDR        =  "hf000".U(16.W)   //32'hbfaf_f000
  val LED_RG0_ADDR    =  "hf004".U(16.W)   //32'hbfaf_f004
  val LED_RG1_ADDR    =  "hf008".U(16.W)   //32'hbfaf_f008
  val NUM_ADDR        =  "hf010".U(16.W)   //32'hbfaf_f010
  val SWITCH_ADDR     =  "hf020".U(16.W)   //32'hbfaf_f020
  val BTN_KEY_ADDR    =  "hf024".U(16.W)   //32'hbfaf_f024
  val BTN_STEP_ADDR   =  "hf028".U(16.W)   //32'hbfaf_f028
  val SW_INTER_ADDR   =  "hf02c".U(16.W)   //32'hbfaf_f02c
  val TIMER_ADDR      =  "he000".U(16.W)   //32'hbfaf_e000
  val IO_SIMU_ADDR      =  "hffec".U(16.W)  //32'hbfaf_ffec
  val VIRTUAL_UART_ADDR =  "hfff0".U(16.W)  //32'hbfaf_fff0
  val SIMU_FLAG_ADDR    =  "hfff4".U(16.W)  //32'hbfaf_fff4
  val OPEN_TRACE_ADDR   =  "hfff8".U(16.W)  //32'hbfaf_fff8
  val NUM_MONITOR_ADDR  =  "hfffc".U(16.W)  //32'hbfaf_fffc
  // @formatter:on
  val io = IO(new Bundle {
    val axi = AXIIO.slave()
    val gp = new GPIO
    val uart = new ValidIO(UInt(8.W))
    // for lab 6 -- ???
    val ram_random_mask = Output(UInt(5.W))
  })
  val switch_led = Wire(UInt(16.W))
  /** Wired in [[ledHandle()]] */

  val led_data = RegInit(Cat(0.U(16.W), switch_led))
  val led_rg0_data = RegInit(0.U(32.W))
  val led_rg1_data = RegInit(0.U(32.W))
  val num_data = RegInit(0.U(32.W))
  val switch_data = Wire(UInt(32.W))
  /** Wired in [[switchHandle()]] */
  val sw_inter_data = Wire(UInt(32.W))
  /** Wired in [[switchHandle()]] */
  // switch interleave
  val btn_key_data = Wire(UInt(32.W))
  /** Wired in [[btn_keyHandle()]] */
  val btn_step_data = Wire(UInt(32.W))
  /** Wired in [[btn_stepHandle()]] */
  val timer = RegInit(0.U(32.W))
  val timer_r1 = RegNext(timer)
  val timer_r2 = RegNext(timer_r1)
  val simu_flag = RegInit(Fill(32, simulation.B))
  val io_simu = RegInit(0.U(32.W))
  val virtual_uart_data = RegInit(0.U(8.W))
  val open_trace = RegInit(true.B)
  val num_monitor = RegInit(true.B)

  val crs = RegInit(VecInit(Seq.fill(8)(0.U(32.W))))

  /** wrote in [[axi_interfaceHandle()]]. */
  val conf_we = Wire(Bool())
  val conf_addr = Wire(UInt(32.W))
  val conf_wdata = Wire(UInt(32.W))

  axi_interfaceHandle()
  confreg_registerHandle()
  timerHandle()
  simulation_flagHandle()
  io_simulationHandle()
  open_traceHandle()
  num_monitorHandle()
  virtual_uartHandle()
  axirandom_maskHandle()
  ledHandle()
  switchHandle()
  btn_keyHandle()
  btn_stepHandle()
  led_rgHandle()
  digital_numberHandle()

  def axi_interfaceHandle(): Unit = {
    val busy = RegInit(false.B)
    //  val write ???
    val R_or_W = RegInit(false.B)
    //  val swready = ???
    val ar_enter = io.axi.ar.valid & io.axi.ar.ready
    val r_retire = io.axi.r.valid & io.axi.r.ready & io.axi.r.bits.last
    val aw_enter = io.axi.aw.valid & io.axi.aw.ready
    val w_enter = io.axi.w.valid & io.axi.w.ready & io.axi.w.bits.last
    val b_retire = io.axi.b.valid & io.axi.b.ready

    io.axi.ar.ready := !busy & (!R_or_W | !io.axi.aw.valid)
    io.axi.aw.ready := !busy & (R_or_W | !io.axi.ar.valid)

    val buf_id = RegInit(0.U(4.W))
    val buf_addr = RegInit(0.U(32.W))
    val buf_len = RegInit(0.U(8.W))
    val buf_size = RegInit(0.U(3.W))

    when(ar_enter | aw_enter) {
      busy := true.B
    }.elsewhen(r_retire | b_retire) {
      busy := false.B
    }

    when(ar_enter | aw_enter) {
      R_or_W := ar_enter
      buf_id := Mux(ar_enter, io.axi.ar.bits.id, io.axi.aw.bits.id)
      buf_addr := Mux(ar_enter, io.axi.ar.bits.addr, io.axi.aw.bits.addr)
      buf_len := Mux(ar_enter, io.axi.ar.bits.len, io.axi.aw.bits.len)
      buf_size := Mux(ar_enter, io.axi.ar.bits.size, io.axi.aw.bits.size)
    }

    val conf_wready_reg = RegInit(false.B)
    io.axi.w.ready := conf_wready_reg
    when(aw_enter) {
      conf_wready_reg := true.B
    }.elsewhen(w_enter & io.axi.w.bits.last) {
      conf_wready_reg := false.B
    }

    // read data has one cycle delay
    val conf_rdata_reg = RegInit(0.U(32.W))
    val conf_rvalid_reg = RegInit(false.B)
    val conf_rlast_reg = RegInit(false.B)
    io.axi.r.bits.data := conf_rdata_reg
    io.axi.r.valid := conf_rvalid_reg
    io.axi.r.bits.last := conf_rlast_reg
    when(busy & R_or_W & !r_retire) {
      conf_rvalid_reg := true.B
      conf_rlast_reg := true.B
      conf_rdata_reg := MuxLookup(buf_addr(15, 0), 0.U,
        Seq(
          LED_ADDR -> led_data,
          LED_RG0_ADDR -> led_rg0_data,
          LED_RG1_ADDR -> led_rg1_data,
          NUM_ADDR -> num_data,
          SWITCH_ADDR -> switch_data,
          BTN_KEY_ADDR -> btn_key_data,
          BTN_STEP_ADDR -> btn_step_data,
          SW_INTER_ADDR -> sw_inter_data,
          TIMER_ADDR -> timer_r2,
          SIMU_FLAG_ADDR -> simu_flag,
          IO_SIMU_ADDR -> io_simu,
          VIRTUAL_UART_ADDR -> Cat(0.U(24.W), virtual_uart_data),
          OPEN_TRACE_ADDR -> Cat(0.U(31.W), open_trace),
          NUM_MONITOR_ADDR -> Cat(0.U(31.W), num_monitor),
        ) ++ CR_ADDRS.zip(crs).map(zip => zip._1 -> zip._2)
      )
    }.elsewhen(r_retire) {
      conf_rvalid_reg := false.B
    }

    // conf write, only support a word write
    conf_we := w_enter
    conf_addr := buf_addr
    conf_wdata := io.axi.w.bits.data

    val conf_bvalid_reg = RegInit(false.B)
    io.axi.b.valid := conf_bvalid_reg
    when(w_enter) {
      conf_bvalid_reg := true.B
    }.elsewhen(b_retire) {
      conf_bvalid_reg := false.B
    }

    io.axi.r.bits.id := buf_id
    io.axi.b.bits.id := buf_id
    io.axi.b.bits.resp := 0.U
    io.axi.r.bits.resp := 0.U
  }

  def confreg_registerHandle(): Unit = {
    for (i <- 0 until 8) {
      crs(i) := Mux(conf_we & (conf_addr(15, 0) === CR_ADDRS(i)), conf_wdata, crs(i))
    }
  }

  def timerHandle(): Unit = {
    val write_timer_begin = RegInit(false.B)
    val write_timer_begin_r1 = RegNext(write_timer_begin)
    val write_timer_begin_r2 = RegNext(write_timer_begin_r1)
    val write_timer_begin_r3 = RegNext(write_timer_begin_r2)

    val write_timer_end_r1 = RegNext(write_timer_begin_r2)
    val write_timer_end_r2 = RegNext(write_timer_end_r1)

    val conf_wdata_r = Reg(UInt(32.W))
    val conf_wdata_r1 = RegNext(conf_wdata_r)
    val conf_wdata_r2 = RegNext(conf_wdata_r1)

    val write_timer = conf_we & (conf_addr(15, 0) === TIMER_ADDR)
    when(write_timer) {
      write_timer_begin := true.B
      conf_wdata_r := conf_wdata
    }.elsewhen(write_timer_end_r2) {
      write_timer_begin := false.B
    }

    when(write_timer_begin_r2 && !write_timer_begin_r3) {
      timer := conf_wdata_r2
    }.otherwise {
      timer := timer + 1.U
    }
  }

  /** see the declare of  [[simu_flag]], only need to change value when reset */
  def simulation_flagHandle(): Unit = {

  }

  def io_simulationHandle(): Unit = {
    when(conf_we & (conf_addr(15, 0) === IO_SIMU_ADDR)) {
      io_simu := Cat(conf_wdata(15, 0), conf_wdata(31, 16))
    }
  }

  def open_traceHandle(): Unit = {
    when(conf_we & (conf_addr(15, 0) === OPEN_TRACE_ADDR)) {
      open_trace := conf_wdata.orR()
    }
  }

  def num_monitorHandle(): Unit = {
    when(conf_we & (conf_addr(15, 0) === NUM_MONITOR_ADDR)) {
      num_monitor := conf_wdata(0)
    }
  }

  def virtual_uartHandle(): Unit = {
    io.uart.bits := conf_wdata(7,0)
    io.uart.valid := conf_we & (conf_addr(15, 0) === VIRTUAL_UART_ADDR)
    when(io.uart.valid) {
      virtual_uart_data := conf_wdata(7, 0)
    }
  }

  def axirandom_maskHandle(): Unit = {
    val led_r_n = ~switch_led

    val pseudo_random_23 = RegInit(Mux(simu_flag(0), RANDOM_SEED, Cat("b1010101".U(7.W), led_r_n)))
    pseudo_random_23 := Cat(pseudo_random_23(21, 0), pseudo_random_23(22) ^ pseudo_random_23(17))
    val no_mask = RegInit(pseudo_random_23(15, 0) === "h00ff".U(16.W))
    val short_delay = RegInit(pseudo_random_23(7, 0) === "hff".U(8.W))

    io.ram_random_mask := Cat(
      (pseudo_random_23(10) & pseudo_random_23(20)) & (short_delay | (pseudo_random_23(11) ^ pseudo_random_23(5))) | no_mask,
      (pseudo_random_23(9) & pseudo_random_23(17)) & (short_delay | (pseudo_random_23(12) ^ pseudo_random_23(4))) | no_mask,
      (pseudo_random_23(8) & pseudo_random_23(22)) & (short_delay | (pseudo_random_23(13) ^ pseudo_random_23(3))) | no_mask,
      (pseudo_random_23(7) & pseudo_random_23(19)) & (short_delay | (pseudo_random_23(14) ^ pseudo_random_23(2))) | no_mask,
      (pseudo_random_23(6) & pseudo_random_23(16)) & (short_delay | (pseudo_random_23(15) ^ pseudo_random_23(1))) | no_mask,
    )
  }

  /** led display */
  def ledHandle(): Unit = {
    io.gp.led := led_data(15, 0)
    switch_led := io.gp.switch.asBools().
      map(e => Cat(e, e)).
      foldRight(0.U)((e, r) => Cat(r, e))(15, 0)

    when(conf_we & (conf_addr(15, 0) === LED_ADDR)) {
      led_data := conf_wdata
    }
  }

  /** switch data */
  def switchHandle(): Unit = {
    switch_data := Cat(0.U(24.W), io.gp.switch)
    sw_inter_data := Cat(
      0.U(16.W),
      io.gp.switch.asBools().
        map(e => Cat(e, 0.U)).
        foldRight(0.U)((e, r) => Cat(r, e))(15, 0)
    )

  }

  /** btn key data */
  def btn_keyHandle(): Unit = {
    val btn_key_r = RegInit(0.U(16.W))
    btn_key_data := Cat(0.U(16.W), btn_key_r)

    // state machine
    val state = RegInit(0.U(3.W))
    val next_state = Wire(UInt(3.W))

    // eliminate jitter
    val key_flag = RegInit(false.B)
    val key_count = RegInit(0.U(20.W))
    val state_count = RegInit(0.U(4.W))

    val key_start = state === 0.U && !io.gp.btn_key_row.andR()
    val key_end = state === 7.U && io.gp.btn_key_row.andR()
    val key_sample = key_count(19)
    when(key_sample && state_count(3)) {
      key_flag := false.B
    }.elsewhen(key_start || key_end) {
      key_flag := true.B
    }
    when(!key_flag) {
      key_count := 0.U
    }.otherwise {
      key_count := key_count + 1.U
    }
    when(state_count(3)) {
      state_count := 0.U
    }.otherwise {
      state_count := state_count + 1.U
    }
    when(state_count(3)) {
      state := next_state
    }
    next_state := MuxLookup(state, 0.U,
      Array(
        0.U -> Mux(key_sample && !io.gp.btn_key_row.andR(), 1.U, 0.U),
        1.U -> Mux(!io.gp.btn_key_row.andR(), 7.U, 2.U),
        2.U -> Mux(!io.gp.btn_key_row.andR(), 7.U, 3.U),
        3.U -> Mux(!io.gp.btn_key_row.andR(), 7.U, 4.U),
        4.U -> Mux(!io.gp.btn_key_row.andR(), 7.U, 0.U),
        7.U -> Mux(key_sample && !io.gp.btn_key_row.andR(), 0.U, 7.U),
      )
    )
    io.gp.btn_key_col := MuxLookup(state, 0.U,
      Array(
        0.U -> 0.U,
        1.U -> "b1110".U,
        2.U -> "b1101".U,
        3.U -> "b1011".U,
        4.U -> "b0111".U
      )
    )

    val btn_key_tmp = Wire(UInt(16.W))
    when(next_state === 0.U) {
      btn_key_r := 0.U
    }.elsewhen(next_state === 7.U && state =/= 7.U) {
      btn_key_r := btn_key_tmp
    }
    btn_key_tmp := MuxLookup(state, 0.U,
      Array(
        1.U -> MuxLookup(io.gp.btn_key_row, 0.U,
          Array(
            "b1110".U -> "h0001".U,
            "b1101".U -> "h0010".U,
            "b1011".U -> "h0100".U,
            "b0111".U -> "h1000".U,
          )
        ),
        2.U -> MuxLookup(io.gp.btn_key_row, 0.U,
          Array(
            "b1110".U -> "h0002".U,
            "b1101".U -> "h0020".U,
            "b1011".U -> "h0200".U,
            "b0111".U -> "h2000".U,
          )
        ),
        3.U -> MuxLookup(io.gp.btn_key_row, 0.U,
          Array(
            "b1110".U -> "h0004".U,
            "b1101".U -> "h0040".U,
            "b1011".U -> "h0400".U,
            "b0111".U -> "h4000".U,
          )
        ),
        4.U -> MuxLookup(io.gp.btn_key_row, 0.U,
          Array(
            "b1110".U -> "h0008".U,
            "b1101".U -> "h0080".U,
            "b1011".U -> "h0800".U,
            "b0111".U -> "h8000".U,
          )
        )
      )
    )
  }

  /** btn step data */
  def btn_stepHandle(): Unit = {
    val btn_step_r = RegInit(VecInit(Seq.fill(2)(true.B)))
    btn_step_data := Cat(0.U(30.W), !btn_step_r(0), !btn_step_r(1)) //1:press

    for (i <- 0 until 2) {
      val step_flag = RegInit(false.B)
      val step_count = RegInit(0.U(20.W))
      val step_start = btn_step_r(i) && !io.gp.btn_step(i)
      val step_end = !btn_step_r(i) && io.gp.btn_step(i)
      val step_sample = step_count(19)
      when(step_sample) {
        step_flag := false.B
      }.elsewhen(step_start || step_end) {
        step_flag := true.B
      }
      when(!step_flag) {
        step_count := 0.U
      }.otherwise {
        step_count := step_count + 1.U
      }
      when(step_sample) {
        btn_step_r(i) := io.gp.btn_step(i)
      }
    }
  }

  /**
    * led_rg0_data -- 0xbfd0_f010
    * led_rg1_data -- 0xbfd0_f014
    */
  def led_rgHandle(): Unit = {
    io.gp.led_rg0 := led_rg0_data(1, 0)
    io.gp.led_rg1 := led_rg1_data(1, 0)
    when(conf_we && conf_addr(15, 0) === LED_RG0_ADDR) {
      led_rg0_data := conf_wdata
    }
    when(conf_we && conf_addr(15, 0) === LED_RG1_ADDR) {
      led_rg1_data := conf_wdata
    }
  }

  /** digital number display */
  def digital_numberHandle(): Unit = {
    when(conf_we && conf_addr(15, 0) === NUM_ADDR) {
      num_data := conf_wdata
    }
    val count = RegInit(0.U(20.W))
    count := count + 1.U

    // scan data
    val scan_data = RegInit(0.U(4.W))
    scan_data := MuxLookup(count(19, 17), scan_data,
      Array(
        0.U -> num_data(31, 28),
        1.U -> num_data(27, 24),
        2.U -> num_data(23, 20),
        3.U -> num_data(19, 16),
        4.U -> num_data(15, 12),
        5.U -> num_data(11, 8),
        6.U -> num_data(7, 4),
        7.U -> num_data(3, 0),
      )
    )

    val num_csn = RegInit("hff".U(8.W))
    io.gp.num_csn := num_csn
    num_csn := MuxLookup(count(19, 17), num_csn,
      Array(
        0.U -> "b0111_1111".U,
        1.U -> "b1011_1111".U,
        2.U -> "b1101_1111".U,
        3.U -> "b1110_1111".U,
        4.U -> "b1111_0111".U,
        5.U -> "b1111_1011".U,
        6.U -> "b1111_1101".U,
        7.U -> "b1111_1110".U,
      )
    )

    val num_a_g = RegInit(0.U(7.W))
    io.gp.num_a_g := num_a_g
    num_a_g := MuxLookup(scan_data, num_a_g,
      Array(
        0.U -> "b1111110".U, //0
        1.U -> "b0110000".U, //1
        2.U -> "b1101101".U, //2
        3.U -> "b1111001".U, //3
        4.U -> "b0110011".U, //4
        5.U -> "b1011011".U, //5
        6.U -> "b1011111".U, //6
        7.U -> "b1110000".U, //7
        8.U -> "b1111111".U, //8
        9.U -> "b1111011".U, //9
        10.U -> "b1110111".U, //a
        11.U -> "b0011111".U, //b
        12.U -> "b1001110".U, //c
        13.U -> "b0111101".U, //d
        14.U -> "b1001111".U, //e
        15.U -> "b1000111".U, //f
      )
    )
  }

}

