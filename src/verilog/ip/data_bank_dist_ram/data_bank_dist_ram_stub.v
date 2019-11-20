// Copyright 1986-2018 Xilinx, Inc. All Rights Reserved.
// --------------------------------------------------------------------------------
// Tool Version: Vivado v.2018.3 (win64) Build 2405991 Thu Dec  6 23:38:27 MST 2018
// Date        : Fri Aug 16 14:33:59 2019
// Host        : DESKTOP-RAIG9UV running 64-bit major release  (build 9200)
// Command     : write_verilog -force -mode synth_stub
//               D:/dragoncore/MIPS-CPU/axiCPURTL/ip/data_bank_dist_ram/data_bank_dist_ram_stub.v
// Design      : data_bank_dist_ram
// Purpose     : Stub declaration of top-level module interface
// Device      : xc7a200tfbg676-2
// --------------------------------------------------------------------------------

// This empty module with port declaration file causes synthesis tools to infer a black box for IP.
// The synthesis directives are for Synopsys Synplify support to prevent IO buffer insertion.
// Please paste the declaration into a Verilog source file or add the file as an additional source.
(* x_core_info = "dist_mem_gen_v8_0_12,Vivado 2018.3" *)
module data_bank_dist_ram(a, d, dpra, clk, we, dpo)
/* synthesis syn_black_box black_box_pad_pin="a[6:0],d[7:0],dpra[6:0],clk,we,dpo[7:0]" */;
  input [6:0]a;
  input [7:0]d;
  input [6:0]dpra;
  input clk;
  input we;
  output [7:0]dpo;
endmodule
