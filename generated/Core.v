module ControlPath(
  input  [31:0] io_data_instruction,
  input         io_data_alu_branch_take,
  output        io_control_PC_isBranch,
  output        io_control_PC_isJump,
  output        io_control_DstRegSelect,
  output        io_control_WBEnable,
  output        io_control_OpBSelect,
  output [3:0]  io_control_AluOp,
  output        io_control_MemWriteEnable,
  output        io_control_WBSelect
);
  wire  _T_1; // @[Lookup.scala 31:38]
  wire [31:0] _T_2; // @[Lookup.scala 31:38]
  wire  _T_3; // @[Lookup.scala 31:38]
  wire  _T_5; // @[Lookup.scala 31:38]
  wire  _T_7; // @[Lookup.scala 31:38]
  wire [31:0] _T_8; // @[Lookup.scala 31:38]
  wire  _T_9; // @[Lookup.scala 31:38]
  wire  _T_11; // @[Lookup.scala 31:38]
  wire  _T_13; // @[Lookup.scala 31:38]
  wire  _T_15; // @[Lookup.scala 31:38]
  wire  _T_17; // @[Lookup.scala 31:38]
  wire  _T_22; // @[Lookup.scala 33:37]
  wire  _T_23; // @[Lookup.scala 33:37]
  wire  _T_24; // @[Lookup.scala 33:37]
  wire  _T_25; // @[Lookup.scala 33:37]
  wire  controlSignals_0; // @[Lookup.scala 33:37]
  wire  _T_29; // @[Lookup.scala 33:37]
  wire  _T_30; // @[Lookup.scala 33:37]
  wire  _T_31; // @[Lookup.scala 33:37]
  wire  _T_32; // @[Lookup.scala 33:37]
  wire  _T_33; // @[Lookup.scala 33:37]
  wire  _T_35; // @[Lookup.scala 33:37]
  wire  _T_36; // @[Lookup.scala 33:37]
  wire  _T_37; // @[Lookup.scala 33:37]
  wire  _T_38; // @[Lookup.scala 33:37]
  wire  _T_39; // @[Lookup.scala 33:37]
  wire  _T_40; // @[Lookup.scala 33:37]
  wire  _T_41; // @[Lookup.scala 33:37]
  wire  _T_44; // @[Lookup.scala 33:37]
  wire  _T_45; // @[Lookup.scala 33:37]
  wire  _T_46; // @[Lookup.scala 33:37]
  wire  _T_47; // @[Lookup.scala 33:37]
  wire  _T_48; // @[Lookup.scala 33:37]
  wire  _T_49; // @[Lookup.scala 33:37]
  wire [2:0] _T_61; // @[Lookup.scala 33:37]
  wire [2:0] _T_62; // @[Lookup.scala 33:37]
  wire [2:0] _T_63; // @[Lookup.scala 33:37]
  wire [2:0] _T_64; // @[Lookup.scala 33:37]
  wire [2:0] _T_65; // @[Lookup.scala 33:37]
  wire [2:0] controlSignals_5; // @[Lookup.scala 33:37]
  wire  _T_67; // @[Lookup.scala 33:37]
  wire  _T_68; // @[Lookup.scala 33:37]
  wire  _T_69; // @[Lookup.scala 33:37]
  wire  _T_70; // @[Lookup.scala 33:37]
  wire  _T_71; // @[Lookup.scala 33:37]
  wire  _T_72; // @[Lookup.scala 33:37]
  wire  _T_73; // @[Lookup.scala 33:37]
  wire  _T_78; // @[Lookup.scala 33:37]
  wire  _T_79; // @[Lookup.scala 33:37]
  wire  _T_80; // @[Lookup.scala 33:37]
  wire  _T_81; // @[Lookup.scala 33:37]
  assign _T_1 = 32'h0 == io_data_instruction; // @[Lookup.scala 31:38]
  assign _T_2 = io_data_instruction & 32'hfc0007ff; // @[Lookup.scala 31:38]
  assign _T_3 = 32'h20 == _T_2; // @[Lookup.scala 31:38]
  assign _T_5 = 32'h22 == _T_2; // @[Lookup.scala 31:38]
  assign _T_7 = 32'h24 == _T_2; // @[Lookup.scala 31:38]
  assign _T_8 = io_data_instruction & 32'hfc000000; // @[Lookup.scala 31:38]
  assign _T_9 = 32'h20000000 == _T_8; // @[Lookup.scala 31:38]
  assign _T_11 = 32'h10000000 == _T_8; // @[Lookup.scala 31:38]
  assign _T_13 = 32'h8000000 == _T_8; // @[Lookup.scala 31:38]
  assign _T_15 = 32'h8c000000 == _T_8; // @[Lookup.scala 31:38]
  assign _T_17 = 32'hac000000 == _T_8; // @[Lookup.scala 31:38]
  assign _T_22 = _T_9 ? 1'h0 : _T_11; // @[Lookup.scala 33:37]
  assign _T_23 = _T_7 ? 1'h0 : _T_22; // @[Lookup.scala 33:37]
  assign _T_24 = _T_5 ? 1'h0 : _T_23; // @[Lookup.scala 33:37]
  assign _T_25 = _T_3 ? 1'h0 : _T_24; // @[Lookup.scala 33:37]
  assign controlSignals_0 = _T_1 ? 1'h0 : _T_25; // @[Lookup.scala 33:37]
  assign _T_29 = _T_11 ? 1'h0 : _T_13; // @[Lookup.scala 33:37]
  assign _T_30 = _T_9 ? 1'h0 : _T_29; // @[Lookup.scala 33:37]
  assign _T_31 = _T_7 ? 1'h0 : _T_30; // @[Lookup.scala 33:37]
  assign _T_32 = _T_5 ? 1'h0 : _T_31; // @[Lookup.scala 33:37]
  assign _T_33 = _T_3 ? 1'h0 : _T_32; // @[Lookup.scala 33:37]
  assign _T_35 = _T_15 ? 1'h0 : 1'h1; // @[Lookup.scala 33:37]
  assign _T_36 = _T_13 | _T_35; // @[Lookup.scala 33:37]
  assign _T_37 = _T_11 | _T_36; // @[Lookup.scala 33:37]
  assign _T_38 = _T_9 ? 1'h0 : _T_37; // @[Lookup.scala 33:37]
  assign _T_39 = _T_7 | _T_38; // @[Lookup.scala 33:37]
  assign _T_40 = _T_5 | _T_39; // @[Lookup.scala 33:37]
  assign _T_41 = _T_3 | _T_40; // @[Lookup.scala 33:37]
  assign _T_44 = _T_13 ? 1'h0 : _T_15; // @[Lookup.scala 33:37]
  assign _T_45 = _T_11 ? 1'h0 : _T_44; // @[Lookup.scala 33:37]
  assign _T_46 = _T_9 | _T_45; // @[Lookup.scala 33:37]
  assign _T_47 = _T_7 | _T_46; // @[Lookup.scala 33:37]
  assign _T_48 = _T_5 | _T_47; // @[Lookup.scala 33:37]
  assign _T_49 = _T_3 | _T_48; // @[Lookup.scala 33:37]
  assign _T_61 = _T_11 ? 3'h4 : {{2'd0}, _T_44}; // @[Lookup.scala 33:37]
  assign _T_62 = _T_9 ? 3'h1 : _T_61; // @[Lookup.scala 33:37]
  assign _T_63 = _T_7 ? 3'h3 : _T_62; // @[Lookup.scala 33:37]
  assign _T_64 = _T_5 ? 3'h2 : _T_63; // @[Lookup.scala 33:37]
  assign _T_65 = _T_3 ? 3'h1 : _T_64; // @[Lookup.scala 33:37]
  assign controlSignals_5 = _T_1 ? 3'h0 : _T_65; // @[Lookup.scala 33:37]
  assign _T_67 = _T_15 ? 1'h0 : _T_17; // @[Lookup.scala 33:37]
  assign _T_68 = _T_13 ? 1'h0 : _T_67; // @[Lookup.scala 33:37]
  assign _T_69 = _T_11 ? 1'h0 : _T_68; // @[Lookup.scala 33:37]
  assign _T_70 = _T_9 ? 1'h0 : _T_69; // @[Lookup.scala 33:37]
  assign _T_71 = _T_7 ? 1'h0 : _T_70; // @[Lookup.scala 33:37]
  assign _T_72 = _T_5 ? 1'h0 : _T_71; // @[Lookup.scala 33:37]
  assign _T_73 = _T_3 ? 1'h0 : _T_72; // @[Lookup.scala 33:37]
  assign _T_78 = _T_9 | _T_37; // @[Lookup.scala 33:37]
  assign _T_79 = _T_7 | _T_78; // @[Lookup.scala 33:37]
  assign _T_80 = _T_5 | _T_79; // @[Lookup.scala 33:37]
  assign _T_81 = _T_3 | _T_80; // @[Lookup.scala 33:37]
  assign io_control_PC_isBranch = controlSignals_0 & io_data_alu_branch_take; // @[ControlPath.scala 75:28]
  assign io_control_PC_isJump = _T_1 ? 1'h0 : _T_33; // @[ControlPath.scala 74:26]
  assign io_control_DstRegSelect = _T_1 | _T_41; // @[ControlPath.scala 76:29]
  assign io_control_WBEnable = _T_1 ? 1'h0 : _T_49; // @[ControlPath.scala 77:25]
  assign io_control_OpBSelect = _T_1 | _T_41; // @[ControlPath.scala 78:26]
  assign io_control_AluOp = {{1'd0}, controlSignals_5}; // @[ControlPath.scala 79:22]
  assign io_control_MemWriteEnable = _T_1 ? 1'h0 : _T_73; // @[ControlPath.scala 80:31]
  assign io_control_WBSelect = _T_1 | _T_81; // @[ControlPath.scala 81:25]
endmodule
module InstrMem(
  input         clock,
  input  [31:0] io_addr,
  output [31:0] io_readData
);
  reg [31:0] mem [0:1023]; // @[Memory.scala 23:26]
  reg [31:0] _RAND_0;
  wire [31:0] mem__T_3_data; // @[Memory.scala 23:26]
  wire [9:0] mem__T_3_addr; // @[Memory.scala 23:26]
  reg  mem__T_3_en_pipe_0;
  reg [31:0] _RAND_1;
  reg [9:0] mem__T_3_addr_pipe_0;
  reg [31:0] _RAND_2;
  assign mem__T_3_addr = mem__T_3_addr_pipe_0;
  assign mem__T_3_data = mem[mem__T_3_addr]; // @[Memory.scala 23:26]
  assign io_readData = mem__T_3_data; // @[Memory.scala 24:17]
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
  _RAND_0 = {1{`RANDOM}};
  `ifdef RANDOMIZE_MEM_INIT
  for (initvar = 0; initvar < 1024; initvar = initvar+1)
    mem[initvar] = _RAND_0[31:0];
  `endif // RANDOMIZE_MEM_INIT
  `ifdef RANDOMIZE_REG_INIT
  _RAND_1 = {1{`RANDOM}};
  mem__T_3_en_pipe_0 = _RAND_1[0:0];
  `endif // RANDOMIZE_REG_INIT
  `ifdef RANDOMIZE_REG_INIT
  _RAND_2 = {1{`RANDOM}};
  mem__T_3_addr_pipe_0 = _RAND_2[9:0];
  `endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`endif // SYNTHESIS
  always @(posedge clock) begin
    mem__T_3_en_pipe_0 <= 1'h1;
    mem__T_3_addr_pipe_0 <= io_addr[9:0];
  end
endmodule
module DataMem(
  input         clock,
  input         io_isWrite,
  input  [31:0] io_addr,
  input  [31:0] io_writeData,
  output [31:0] io_readData
);
  reg [31:0] mem [0:1023]; // @[Memory.scala 46:26]
  reg [31:0] _RAND_0;
  wire [31:0] mem__T_5_data; // @[Memory.scala 46:26]
  wire [9:0] mem__T_5_addr; // @[Memory.scala 46:26]
  wire [31:0] mem__T_1_data; // @[Memory.scala 46:26]
  wire [9:0] mem__T_1_addr; // @[Memory.scala 46:26]
  wire  mem__T_1_mask; // @[Memory.scala 46:26]
  wire  mem__T_1_en; // @[Memory.scala 46:26]
  reg  mem__T_5_en_pipe_0;
  reg [31:0] _RAND_1;
  reg [9:0] mem__T_5_addr_pipe_0;
  reg [31:0] _RAND_2;
  assign mem__T_5_addr = mem__T_5_addr_pipe_0;
  assign mem__T_5_data = mem[mem__T_5_addr]; // @[Memory.scala 46:26]
  assign mem__T_1_data = io_writeData;
  assign mem__T_1_addr = io_addr[9:0];
  assign mem__T_1_mask = 1'h1;
  assign mem__T_1_en = io_isWrite;
  assign io_readData = mem__T_5_data; // @[Memory.scala 52:19]
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
  _RAND_0 = {1{`RANDOM}};
  `ifdef RANDOMIZE_MEM_INIT
  for (initvar = 0; initvar < 1024; initvar = initvar+1)
    mem[initvar] = _RAND_0[31:0];
  `endif // RANDOMIZE_MEM_INIT
  `ifdef RANDOMIZE_REG_INIT
  _RAND_1 = {1{`RANDOM}};
  mem__T_5_en_pipe_0 = _RAND_1[0:0];
  `endif // RANDOMIZE_REG_INIT
  `ifdef RANDOMIZE_REG_INIT
  _RAND_2 = {1{`RANDOM}};
  mem__T_5_addr_pipe_0 = _RAND_2[9:0];
  `endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`endif // SYNTHESIS
  always @(posedge clock) begin
    if(mem__T_1_en & mem__T_1_mask) begin
      mem[mem__T_1_addr] <= mem__T_1_data; // @[Memory.scala 46:26]
    end
    if (io_isWrite) begin
      mem__T_5_en_pipe_0 <= 1'h0;
    end else begin
      mem__T_5_en_pipe_0 <= 1'h1;
    end
    if (io_isWrite ? 1'h0 : 1'h1) begin
      mem__T_5_addr_pipe_0 <= io_addr[9:0];
    end
  end
endmodule
module RegisterFile(
  input         clock,
  input  [4:0]  io_rs1Addr,
  output [31:0] io_rs1Data,
  input  [4:0]  io_rs2Addr,
  output [31:0] io_rs2Data,
  input  [4:0]  io_writeAddr,
  input  [31:0] io_writeData,
  input         io_writeEnable
);
  reg [31:0] registerFile [0:31]; // @[RegisterFile.scala 39:27]
  reg [31:0] _RAND_0;
  wire [31:0] registerFile__T_4_data; // @[RegisterFile.scala 39:27]
  wire [4:0] registerFile__T_4_addr; // @[RegisterFile.scala 39:27]
  wire [31:0] registerFile__T_7_data; // @[RegisterFile.scala 39:27]
  wire [4:0] registerFile__T_7_addr; // @[RegisterFile.scala 39:27]
  wire [31:0] registerFile__T_2_data; // @[RegisterFile.scala 39:27]
  wire [4:0] registerFile__T_2_addr; // @[RegisterFile.scala 39:27]
  wire  registerFile__T_2_mask; // @[RegisterFile.scala 39:27]
  wire  registerFile__T_2_en; // @[RegisterFile.scala 39:27]
  wire  _T; // @[RegisterFile.scala 41:43]
  wire  _T_3; // @[RegisterFile.scala 46:35]
  wire  _T_6; // @[RegisterFile.scala 47:35]
  assign registerFile__T_4_addr = io_rs1Addr;
  assign registerFile__T_4_data = registerFile[registerFile__T_4_addr]; // @[RegisterFile.scala 39:27]
  assign registerFile__T_7_addr = io_rs2Addr;
  assign registerFile__T_7_data = registerFile[registerFile__T_7_addr]; // @[RegisterFile.scala 39:27]
  assign registerFile__T_2_data = io_writeData;
  assign registerFile__T_2_addr = io_writeAddr;
  assign registerFile__T_2_mask = 1'h1;
  assign registerFile__T_2_en = io_writeEnable & _T;
  assign _T = io_writeAddr != 5'h0; // @[RegisterFile.scala 41:43]
  assign _T_3 = io_rs1Addr != 5'h0; // @[RegisterFile.scala 46:35]
  assign _T_6 = io_rs2Addr != 5'h0; // @[RegisterFile.scala 47:35]
  assign io_rs1Data = _T_3 ? registerFile__T_4_data : 32'h0; // @[RegisterFile.scala 46:16]
  assign io_rs2Data = _T_6 ? registerFile__T_7_data : 32'h0; // @[RegisterFile.scala 47:16]
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
  _RAND_0 = {1{`RANDOM}};
  `ifdef RANDOMIZE_MEM_INIT
  for (initvar = 0; initvar < 32; initvar = initvar+1)
    registerFile[initvar] = _RAND_0[31:0];
  `endif // RANDOMIZE_MEM_INIT
  `endif // RANDOMIZE
end // initial
`endif // SYNTHESIS
  always @(posedge clock) begin
    if(registerFile__T_2_en & registerFile__T_2_mask) begin
      registerFile[registerFile__T_2_addr] <= registerFile__T_2_data; // @[RegisterFile.scala 39:27]
    end
  end
endmodule
module ALU(
  input  [31:0] io_input_inputA,
  input  [31:0] io_input_inputB,
  input  [3:0]  io_input_controlSignal,
  output [31:0] io_output_aluOutput,
  output        io_output_branchTake
);
  wire [31:0] _T_1; // @[ALU.scala 41:31]
  wire [31:0] _T_3; // @[ALU.scala 42:31]
  wire [31:0] _T_4; // @[ALU.scala 43:31]
  wire  _T_5; // @[Mux.scala 68:19]
  wire [31:0] _T_6; // @[Mux.scala 68:16]
  wire  _T_8; // @[Mux.scala 68:19]
  wire [31:0] _T_9; // @[Mux.scala 68:16]
  wire  _T_11; // @[Mux.scala 68:19]
  wire [31:0] _T_12; // @[Mux.scala 68:16]
  wire  _T_14; // @[Mux.scala 68:19]
  wire [31:0] _T_15; // @[Mux.scala 68:16]
  wire  _T_17; // @[Mux.scala 68:19]
  wire [31:0] _T_18; // @[Mux.scala 68:16]
  wire  _T_20; // @[Mux.scala 68:19]
  wire [31:0] _T_21; // @[Mux.scala 68:16]
  wire  _T_23; // @[Mux.scala 68:19]
  wire [31:0] _T_24; // @[Mux.scala 68:16]
  wire  _T_25; // @[Mux.scala 68:19]
  wire [31:0] _T_26; // @[Mux.scala 68:16]
  wire  _T_27; // @[Mux.scala 68:19]
  wire  _T_29; // @[ALU.scala 53:41]
  wire  _T_31; // @[ALU.scala 54:25]
  wire  _T_32; // @[ALU.scala 55:47]
  wire  _T_37; // @[ALU.scala 58:32]
  wire  _T_39; // @[Mux.scala 68:16]
  wire  _T_41; // @[Mux.scala 68:16]
  wire  _T_43; // @[Mux.scala 68:16]
  wire  _T_45; // @[Mux.scala 68:16]
  wire  _T_47; // @[Mux.scala 68:16]
  assign _T_1 = io_input_inputA + io_input_inputB; // @[ALU.scala 41:31]
  assign _T_3 = io_input_inputA - io_input_inputB; // @[ALU.scala 42:31]
  assign _T_4 = io_input_inputA & io_input_inputB; // @[ALU.scala 43:31]
  assign _T_5 = 4'h9 == io_input_controlSignal; // @[Mux.scala 68:19]
  assign _T_6 = _T_5 ? 32'h0 : io_input_inputA; // @[Mux.scala 68:16]
  assign _T_8 = 4'h8 == io_input_controlSignal; // @[Mux.scala 68:19]
  assign _T_9 = _T_8 ? 32'h0 : _T_6; // @[Mux.scala 68:16]
  assign _T_11 = 4'h7 == io_input_controlSignal; // @[Mux.scala 68:19]
  assign _T_12 = _T_11 ? 32'h0 : _T_9; // @[Mux.scala 68:16]
  assign _T_14 = 4'h6 == io_input_controlSignal; // @[Mux.scala 68:19]
  assign _T_15 = _T_14 ? 32'h0 : _T_12; // @[Mux.scala 68:16]
  assign _T_17 = 4'h5 == io_input_controlSignal; // @[Mux.scala 68:19]
  assign _T_18 = _T_17 ? 32'h0 : _T_15; // @[Mux.scala 68:16]
  assign _T_20 = 4'h4 == io_input_controlSignal; // @[Mux.scala 68:19]
  assign _T_21 = _T_20 ? 32'h0 : _T_18; // @[Mux.scala 68:16]
  assign _T_23 = 4'h3 == io_input_controlSignal; // @[Mux.scala 68:19]
  assign _T_24 = _T_23 ? _T_4 : _T_21; // @[Mux.scala 68:16]
  assign _T_25 = 4'h2 == io_input_controlSignal; // @[Mux.scala 68:19]
  assign _T_26 = _T_25 ? _T_3 : _T_24; // @[Mux.scala 68:16]
  assign _T_27 = 4'h1 == io_input_controlSignal; // @[Mux.scala 68:19]
  assign _T_29 = io_input_inputA == io_input_inputB; // @[ALU.scala 53:41]
  assign _T_31 = _T_29 == 1'h0; // @[ALU.scala 54:25]
  assign _T_32 = io_input_inputA > 32'h0; // @[ALU.scala 55:47]
  assign _T_37 = _T_32 == 1'h0; // @[ALU.scala 58:32]
  assign _T_39 = _T_5 & _T_37; // @[Mux.scala 68:16]
  assign _T_41 = _T_8 ? 1'h0 : _T_39; // @[Mux.scala 68:16]
  assign _T_43 = _T_11 | _T_41; // @[Mux.scala 68:16]
  assign _T_45 = _T_14 ? _T_32 : _T_43; // @[Mux.scala 68:16]
  assign _T_47 = _T_17 ? _T_31 : _T_45; // @[Mux.scala 68:16]
  assign io_output_aluOutput = _T_27 ? _T_1 : _T_26; // @[ALU.scala 40:25]
  assign io_output_branchTake = _T_20 ? _T_29 : _T_47; // @[ALU.scala 52:26]
endmodule
module DataPath(
  input         clock,
  input         reset,
  input         io_control_PC_isBranch,
  input         io_control_PC_isJump,
  input         io_control_DstRegSelect,
  input         io_control_WBEnable,
  input         io_control_OpBSelect,
  input  [3:0]  io_control_AluOp,
  input         io_control_MemWriteEnable,
  input         io_control_WBSelect,
  output [31:0] io_data_instruction,
  output        io_data_alu_branch_take
);
  wire  instrMem_clock; // @[DataPath.scala 53:26]
  wire [31:0] instrMem_io_addr; // @[DataPath.scala 53:26]
  wire [31:0] instrMem_io_readData; // @[DataPath.scala 53:26]
  wire  dataMem_clock; // @[DataPath.scala 54:25]
  wire  dataMem_io_isWrite; // @[DataPath.scala 54:25]
  wire [31:0] dataMem_io_addr; // @[DataPath.scala 54:25]
  wire [31:0] dataMem_io_writeData; // @[DataPath.scala 54:25]
  wire [31:0] dataMem_io_readData; // @[DataPath.scala 54:25]
  wire  regFile_clock; // @[DataPath.scala 100:25]
  wire [4:0] regFile_io_rs1Addr; // @[DataPath.scala 100:25]
  wire [31:0] regFile_io_rs1Data; // @[DataPath.scala 100:25]
  wire [4:0] regFile_io_rs2Addr; // @[DataPath.scala 100:25]
  wire [31:0] regFile_io_rs2Data; // @[DataPath.scala 100:25]
  wire [4:0] regFile_io_writeAddr; // @[DataPath.scala 100:25]
  wire [31:0] regFile_io_writeData; // @[DataPath.scala 100:25]
  wire  regFile_io_writeEnable; // @[DataPath.scala 100:25]
  wire [31:0] alu_io_input_inputA; // @[DataPath.scala 114:21]
  wire [31:0] alu_io_input_inputB; // @[DataPath.scala 114:21]
  wire [3:0] alu_io_input_controlSignal; // @[DataPath.scala 114:21]
  wire [31:0] alu_io_output_aluOutput; // @[DataPath.scala 114:21]
  wire  alu_io_output_branchTake; // @[DataPath.scala 114:21]
  reg [31:0] reg_PC; // @[DataPath.scala 72:25]
  reg [31:0] _RAND_0;
  wire [3:0] _T_9; // @[DataPath.scala 95:27]
  wire [25:0] address; // @[DataPath.scala 90:30]
  wire [31:0] j_target; // @[Cat.scala 29:58]
  wire [31:0] pc_plus4; // @[DataPath.scala 75:24]
  wire [15:0] immediate; // @[DataPath.scala 89:32]
  wire  _T_6; // @[DataPath.scala 93:50]
  wire [15:0] _T_8; // @[Bitwise.scala 71:12]
  wire [31:0] extendedImmediate; // @[Cat.scala 29:58]
  wire [31:0] br_target; // @[DataPath.scala 96:36]
  wire [4:0] rt_address; // @[DataPath.scala 86:33]
  wire [4:0] rd_address; // @[DataPath.scala 87:33]
  wire [31:0] valRT; // @[DataPath.scala 109:21 DataPath.scala 112:11]
  wire [31:0] aluOutput; // @[DataPath.scala 121:25 DataPath.scala 124:15]
  wire [31:0] readData; // @[DataPath.scala 134:24 DataPath.scala 135:14]
  InstrMem instrMem ( // @[DataPath.scala 53:26]
    .clock(instrMem_clock),
    .io_addr(instrMem_io_addr),
    .io_readData(instrMem_io_readData)
  );
  DataMem dataMem ( // @[DataPath.scala 54:25]
    .clock(dataMem_clock),
    .io_isWrite(dataMem_io_isWrite),
    .io_addr(dataMem_io_addr),
    .io_writeData(dataMem_io_writeData),
    .io_readData(dataMem_io_readData)
  );
  RegisterFile regFile ( // @[DataPath.scala 100:25]
    .clock(regFile_clock),
    .io_rs1Addr(regFile_io_rs1Addr),
    .io_rs1Data(regFile_io_rs1Data),
    .io_rs2Addr(regFile_io_rs2Addr),
    .io_rs2Data(regFile_io_rs2Data),
    .io_writeAddr(regFile_io_writeAddr),
    .io_writeData(regFile_io_writeData),
    .io_writeEnable(regFile_io_writeEnable)
  );
  ALU alu ( // @[DataPath.scala 114:21]
    .io_input_inputA(alu_io_input_inputA),
    .io_input_inputB(alu_io_input_inputB),
    .io_input_controlSignal(alu_io_input_controlSignal),
    .io_output_aluOutput(alu_io_output_aluOutput),
    .io_output_branchTake(alu_io_output_branchTake)
  );
  assign _T_9 = reg_PC[31:28]; // @[DataPath.scala 95:27]
  assign address = instrMem_io_readData[25:0]; // @[DataPath.scala 90:30]
  assign j_target = {_T_9,address,2'h0}; // @[Cat.scala 29:58]
  assign pc_plus4 = reg_PC + 32'h4; // @[DataPath.scala 75:24]
  assign immediate = instrMem_io_readData[15:0]; // @[DataPath.scala 89:32]
  assign _T_6 = immediate[15]; // @[DataPath.scala 93:50]
  assign _T_8 = _T_6 ? 16'hffff : 16'h0; // @[Bitwise.scala 71:12]
  assign extendedImmediate = {_T_8,immediate}; // @[Cat.scala 29:58]
  assign br_target = extendedImmediate + pc_plus4; // @[DataPath.scala 96:36]
  assign rt_address = instrMem_io_readData[20:16]; // @[DataPath.scala 86:33]
  assign rd_address = instrMem_io_readData[15:11]; // @[DataPath.scala 87:33]
  assign valRT = regFile_io_rs2Data; // @[DataPath.scala 109:21 DataPath.scala 112:11]
  assign aluOutput = alu_io_output_aluOutput; // @[DataPath.scala 121:25 DataPath.scala 124:15]
  assign readData = dataMem_io_readData; // @[DataPath.scala 134:24 DataPath.scala 135:14]
  assign io_data_instruction = instrMem_io_readData; // @[DataPath.scala 143:25]
  assign io_data_alu_branch_take = alu_io_output_branchTake; // @[DataPath.scala 142:29]
  assign instrMem_clock = clock;
  assign instrMem_io_addr = reg_PC; // @[DataPath.scala 78:22]
  assign dataMem_clock = clock;
  assign dataMem_io_isWrite = io_control_MemWriteEnable; // @[DataPath.scala 131:24]
  assign dataMem_io_addr = alu_io_output_aluOutput; // @[DataPath.scala 129:21]
  assign dataMem_io_writeData = regFile_io_rs2Data; // @[DataPath.scala 130:26]
  assign regFile_clock = clock;
  assign regFile_io_rs1Addr = instrMem_io_readData[25:21]; // @[DataPath.scala 101:24]
  assign regFile_io_rs2Addr = instrMem_io_readData[20:16]; // @[DataPath.scala 102:24]
  assign regFile_io_writeAddr = io_control_DstRegSelect ? rt_address : rd_address; // @[DataPath.scala 103:26]
  assign regFile_io_writeData = io_control_WBSelect ? aluOutput : readData; // @[DataPath.scala 104:26]
  assign regFile_io_writeEnable = io_control_WBEnable; // @[DataPath.scala 105:28]
  assign alu_io_input_inputA = regFile_io_rs1Data; // @[DataPath.scala 115:25]
  assign alu_io_input_inputB = io_control_OpBSelect ? valRT : extendedImmediate; // @[DataPath.scala 117:25]
  assign alu_io_input_controlSignal = io_control_AluOp; // @[DataPath.scala 118:32]
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
  `ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  reg_PC = _RAND_0[31:0];
  `endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`endif // SYNTHESIS
  always @(posedge clock) begin
    if (reset) begin
      reg_PC <= 32'h0;
    end else if (io_control_PC_isBranch) begin
      reg_PC <= br_target;
    end else if (io_control_PC_isJump) begin
      reg_PC <= j_target;
    end else begin
      reg_PC <= pc_plus4;
    end
  end
endmodule
module Core(
  input         clock,
  input         reset,
  output [31:0] io_out_pc_out
);
  wire [31:0] control_io_data_instruction; // @[Core.scala 14:25]
  wire  control_io_data_alu_branch_take; // @[Core.scala 14:25]
  wire  control_io_control_PC_isBranch; // @[Core.scala 14:25]
  wire  control_io_control_PC_isJump; // @[Core.scala 14:25]
  wire  control_io_control_DstRegSelect; // @[Core.scala 14:25]
  wire  control_io_control_WBEnable; // @[Core.scala 14:25]
  wire  control_io_control_OpBSelect; // @[Core.scala 14:25]
  wire [3:0] control_io_control_AluOp; // @[Core.scala 14:25]
  wire  control_io_control_MemWriteEnable; // @[Core.scala 14:25]
  wire  control_io_control_WBSelect; // @[Core.scala 14:25]
  wire  data_clock; // @[Core.scala 15:22]
  wire  data_reset; // @[Core.scala 15:22]
  wire  data_io_control_PC_isBranch; // @[Core.scala 15:22]
  wire  data_io_control_PC_isJump; // @[Core.scala 15:22]
  wire  data_io_control_DstRegSelect; // @[Core.scala 15:22]
  wire  data_io_control_WBEnable; // @[Core.scala 15:22]
  wire  data_io_control_OpBSelect; // @[Core.scala 15:22]
  wire [3:0] data_io_control_AluOp; // @[Core.scala 15:22]
  wire  data_io_control_MemWriteEnable; // @[Core.scala 15:22]
  wire  data_io_control_WBSelect; // @[Core.scala 15:22]
  wire [31:0] data_io_data_instruction; // @[Core.scala 15:22]
  wire  data_io_data_alu_branch_take; // @[Core.scala 15:22]
  ControlPath control ( // @[Core.scala 14:25]
    .io_data_instruction(control_io_data_instruction),
    .io_data_alu_branch_take(control_io_data_alu_branch_take),
    .io_control_PC_isBranch(control_io_control_PC_isBranch),
    .io_control_PC_isJump(control_io_control_PC_isJump),
    .io_control_DstRegSelect(control_io_control_DstRegSelect),
    .io_control_WBEnable(control_io_control_WBEnable),
    .io_control_OpBSelect(control_io_control_OpBSelect),
    .io_control_AluOp(control_io_control_AluOp),
    .io_control_MemWriteEnable(control_io_control_MemWriteEnable),
    .io_control_WBSelect(control_io_control_WBSelect)
  );
  DataPath data ( // @[Core.scala 15:22]
    .clock(data_clock),
    .reset(data_reset),
    .io_control_PC_isBranch(data_io_control_PC_isBranch),
    .io_control_PC_isJump(data_io_control_PC_isJump),
    .io_control_DstRegSelect(data_io_control_DstRegSelect),
    .io_control_WBEnable(data_io_control_WBEnable),
    .io_control_OpBSelect(data_io_control_OpBSelect),
    .io_control_AluOp(data_io_control_AluOp),
    .io_control_MemWriteEnable(data_io_control_MemWriteEnable),
    .io_control_WBSelect(data_io_control_WBSelect),
    .io_data_instruction(data_io_data_instruction),
    .io_data_alu_branch_take(data_io_data_alu_branch_take)
  );
  assign io_out_pc_out = data_io_data_instruction; // @[Core.scala 19:19]
  assign control_io_data_instruction = data_io_data_instruction; // @[Core.scala 18:21]
  assign control_io_data_alu_branch_take = data_io_data_alu_branch_take; // @[Core.scala 18:21]
  assign data_clock = clock;
  assign data_reset = reset;
  assign data_io_control_PC_isBranch = control_io_control_PC_isBranch; // @[Core.scala 17:24]
  assign data_io_control_PC_isJump = control_io_control_PC_isJump; // @[Core.scala 17:24]
  assign data_io_control_DstRegSelect = control_io_control_DstRegSelect; // @[Core.scala 17:24]
  assign data_io_control_WBEnable = control_io_control_WBEnable; // @[Core.scala 17:24]
  assign data_io_control_OpBSelect = control_io_control_OpBSelect; // @[Core.scala 17:24]
  assign data_io_control_AluOp = control_io_control_AluOp; // @[Core.scala 17:24]
  assign data_io_control_MemWriteEnable = control_io_control_MemWriteEnable; // @[Core.scala 17:24]
  assign data_io_control_WBSelect = control_io_control_WBSelect; // @[Core.scala 17:24]
endmodule
