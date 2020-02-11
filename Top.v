module Controller(
  input  [31:0] io_input_instr,
  input         io_input_alu_branch_take,
  output        io_output_PC_isBranch,
  output        io_output_PC_isJump,
  output        io_output_DstRegSelect,
  output        io_output_WBEnable,
  output        io_output_OpBSelect,
  output [3:0]  io_output_AluOp,
  output        io_output_MemWriteEnable,
  output        io_output_MemReadEnable,
  output        io_output_WBSelect
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
  wire  _T_86; // @[Lookup.scala 33:37]
  wire  _T_87; // @[Lookup.scala 33:37]
  wire  _T_88; // @[Lookup.scala 33:37]
  wire  _T_89; // @[Lookup.scala 33:37]
  assign _T_1 = 32'h0 == io_input_instr; // @[Lookup.scala 31:38]
  assign _T_2 = io_input_instr & 32'hfc0007ff; // @[Lookup.scala 31:38]
  assign _T_3 = 32'h20 == _T_2; // @[Lookup.scala 31:38]
  assign _T_5 = 32'h22 == _T_2; // @[Lookup.scala 31:38]
  assign _T_7 = 32'h24 == _T_2; // @[Lookup.scala 31:38]
  assign _T_8 = io_input_instr & 32'hfc000000; // @[Lookup.scala 31:38]
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
  assign _T_78 = _T_9 ? 1'h0 : _T_45; // @[Lookup.scala 33:37]
  assign _T_79 = _T_7 ? 1'h0 : _T_78; // @[Lookup.scala 33:37]
  assign _T_80 = _T_5 ? 1'h0 : _T_79; // @[Lookup.scala 33:37]
  assign _T_81 = _T_3 ? 1'h0 : _T_80; // @[Lookup.scala 33:37]
  assign _T_86 = _T_9 | _T_37; // @[Lookup.scala 33:37]
  assign _T_87 = _T_7 | _T_86; // @[Lookup.scala 33:37]
  assign _T_88 = _T_5 | _T_87; // @[Lookup.scala 33:37]
  assign _T_89 = _T_3 | _T_88; // @[Lookup.scala 33:37]
  assign io_output_PC_isBranch = controlSignals_0 & io_input_alu_branch_take; // @[Controller.scala 76:27]
  assign io_output_PC_isJump = _T_1 ? 1'h0 : _T_33; // @[Controller.scala 75:25]
  assign io_output_DstRegSelect = _T_1 | _T_41; // @[Controller.scala 77:28]
  assign io_output_WBEnable = _T_1 ? 1'h0 : _T_49; // @[Controller.scala 78:24]
  assign io_output_OpBSelect = _T_1 | _T_41; // @[Controller.scala 79:25]
  assign io_output_AluOp = {{1'd0}, controlSignals_5}; // @[Controller.scala 80:21]
  assign io_output_MemWriteEnable = _T_1 ? 1'h0 : _T_73; // @[Controller.scala 81:30]
  assign io_output_MemReadEnable = _T_1 ? 1'h0 : _T_81; // @[Controller.scala 82:29]
  assign io_output_WBSelect = _T_1 | _T_89; // @[Controller.scala 83:24]
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
  reg [31:0] registerFile [0:31]; // @[RegisterFile.scala 29:27]
  reg [31:0] _RAND_0;
  wire [31:0] registerFile__T_4_data; // @[RegisterFile.scala 29:27]
  wire [4:0] registerFile__T_4_addr; // @[RegisterFile.scala 29:27]
  wire [31:0] registerFile__T_7_data; // @[RegisterFile.scala 29:27]
  wire [4:0] registerFile__T_7_addr; // @[RegisterFile.scala 29:27]
  wire [31:0] registerFile__T_2_data; // @[RegisterFile.scala 29:27]
  wire [4:0] registerFile__T_2_addr; // @[RegisterFile.scala 29:27]
  wire  registerFile__T_2_mask; // @[RegisterFile.scala 29:27]
  wire  registerFile__T_2_en; // @[RegisterFile.scala 29:27]
  wire  _T; // @[RegisterFile.scala 31:43]
  wire  _T_3; // @[RegisterFile.scala 36:35]
  wire  _T_6; // @[RegisterFile.scala 37:35]
  assign registerFile__T_4_addr = io_rs1Addr;
  assign registerFile__T_4_data = registerFile[registerFile__T_4_addr]; // @[RegisterFile.scala 29:27]
  assign registerFile__T_7_addr = io_rs2Addr;
  assign registerFile__T_7_data = registerFile[registerFile__T_7_addr]; // @[RegisterFile.scala 29:27]
  assign registerFile__T_2_data = io_writeData;
  assign registerFile__T_2_addr = io_writeAddr;
  assign registerFile__T_2_mask = 1'h1;
  assign registerFile__T_2_en = io_writeEnable & _T;
  assign _T = io_writeAddr != 5'h0; // @[RegisterFile.scala 31:43]
  assign _T_3 = io_rs1Addr != 5'h0; // @[RegisterFile.scala 36:35]
  assign _T_6 = io_rs2Addr != 5'h0; // @[RegisterFile.scala 37:35]
  assign io_rs1Data = _T_3 ? registerFile__T_4_data : 32'h0; // @[RegisterFile.scala 36:16]
  assign io_rs2Data = _T_6 ? registerFile__T_7_data : 32'h0; // @[RegisterFile.scala 37:16]
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
      registerFile[registerFile__T_2_addr] <= registerFile__T_2_data; // @[RegisterFile.scala 29:27]
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
  wire [31:0] _T_1; // @[ALU.scala 40:31]
  wire [31:0] _T_3; // @[ALU.scala 41:31]
  wire [31:0] _T_4; // @[ALU.scala 42:31]
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
  wire  _T_29; // @[ALU.scala 52:41]
  wire  _T_31; // @[ALU.scala 53:25]
  wire  _T_32; // @[ALU.scala 54:47]
  wire  _T_37; // @[ALU.scala 57:32]
  wire  _T_39; // @[Mux.scala 68:16]
  wire  _T_41; // @[Mux.scala 68:16]
  wire  _T_43; // @[Mux.scala 68:16]
  wire  _T_45; // @[Mux.scala 68:16]
  wire  _T_47; // @[Mux.scala 68:16]
  assign _T_1 = io_input_inputA + io_input_inputB; // @[ALU.scala 40:31]
  assign _T_3 = io_input_inputA - io_input_inputB; // @[ALU.scala 41:31]
  assign _T_4 = io_input_inputA & io_input_inputB; // @[ALU.scala 42:31]
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
  assign _T_29 = io_input_inputA == io_input_inputB; // @[ALU.scala 52:41]
  assign _T_31 = _T_29 == 1'h0; // @[ALU.scala 53:25]
  assign _T_32 = io_input_inputA > 32'h0; // @[ALU.scala 54:47]
  assign _T_37 = _T_32 == 1'h0; // @[ALU.scala 57:32]
  assign _T_39 = _T_5 & _T_37; // @[Mux.scala 68:16]
  assign _T_41 = _T_8 ? 1'h0 : _T_39; // @[Mux.scala 68:16]
  assign _T_43 = _T_11 | _T_41; // @[Mux.scala 68:16]
  assign _T_45 = _T_14 ? _T_32 : _T_43; // @[Mux.scala 68:16]
  assign _T_47 = _T_17 ? _T_31 : _T_45; // @[Mux.scala 68:16]
  assign io_output_aluOutput = _T_27 ? _T_1 : _T_26; // @[ALU.scala 39:25]
  assign io_output_branchTake = _T_20 ? _T_29 : _T_47; // @[ALU.scala 51:26]
endmodule
module SingleCycleCPU(
  input         clock,
  input         reset,
  output [31:0] io_imem_address,
  input  [31:0] io_imem_instruction,
  output [31:0] io_dmem_address,
  output [31:0] io_dmem_writedata,
  output        io_dmem_memread,
  output        io_dmem_memwrite,
  input  [31:0] io_dmem_readdata
);
  wire [31:0] controller_io_input_instr; // @[SingleCycleCPU.scala 11:26]
  wire  controller_io_input_alu_branch_take; // @[SingleCycleCPU.scala 11:26]
  wire  controller_io_output_PC_isBranch; // @[SingleCycleCPU.scala 11:26]
  wire  controller_io_output_PC_isJump; // @[SingleCycleCPU.scala 11:26]
  wire  controller_io_output_DstRegSelect; // @[SingleCycleCPU.scala 11:26]
  wire  controller_io_output_WBEnable; // @[SingleCycleCPU.scala 11:26]
  wire  controller_io_output_OpBSelect; // @[SingleCycleCPU.scala 11:26]
  wire [3:0] controller_io_output_AluOp; // @[SingleCycleCPU.scala 11:26]
  wire  controller_io_output_MemWriteEnable; // @[SingleCycleCPU.scala 11:26]
  wire  controller_io_output_MemReadEnable; // @[SingleCycleCPU.scala 11:26]
  wire  controller_io_output_WBSelect; // @[SingleCycleCPU.scala 11:26]
  wire  regFile_clock; // @[SingleCycleCPU.scala 12:23]
  wire [4:0] regFile_io_rs1Addr; // @[SingleCycleCPU.scala 12:23]
  wire [31:0] regFile_io_rs1Data; // @[SingleCycleCPU.scala 12:23]
  wire [4:0] regFile_io_rs2Addr; // @[SingleCycleCPU.scala 12:23]
  wire [31:0] regFile_io_rs2Data; // @[SingleCycleCPU.scala 12:23]
  wire [4:0] regFile_io_writeAddr; // @[SingleCycleCPU.scala 12:23]
  wire [31:0] regFile_io_writeData; // @[SingleCycleCPU.scala 12:23]
  wire  regFile_io_writeEnable; // @[SingleCycleCPU.scala 12:23]
  wire [31:0] alu_io_input_inputA; // @[SingleCycleCPU.scala 13:19]
  wire [31:0] alu_io_input_inputB; // @[SingleCycleCPU.scala 13:19]
  wire [3:0] alu_io_input_controlSignal; // @[SingleCycleCPU.scala 13:19]
  wire [31:0] alu_io_output_aluOutput; // @[SingleCycleCPU.scala 13:19]
  wire  alu_io_output_branchTake; // @[SingleCycleCPU.scala 13:19]
  reg [31:0] reg_pc; // @[SingleCycleCPU.scala 10:23]
  reg [31:0] _RAND_0;
  wire [4:0] rt_address; // @[SingleCycleCPU.scala 28:31]
  wire [4:0] rd_address; // @[SingleCycleCPU.scala 29:31]
  wire [15:0] immediate; // @[SingleCycleCPU.scala 30:30]
  wire [25:0] address; // @[SingleCycleCPU.scala 31:28]
  wire  _T; // @[SingleCycleCPU.scala 32:48]
  wire [15:0] _T_2; // @[Bitwise.scala 71:12]
  wire [31:0] extendedImmediate; // @[Cat.scala 29:58]
  wire [31:0] pc_plus_four; // @[SingleCycleCPU.scala 42:26]
  wire [3:0] _T_5; // @[SingleCycleCPU.scala 45:25]
  wire [31:0] j_target; // @[Cat.scala 29:58]
  wire [31:0] br_target; // @[SingleCycleCPU.scala 46:34]
  wire [31:0] valRT; // @[SingleCycleCPU.scala 60:19 SingleCycleCPU.scala 62:9]
  wire [31:0] aluOutput; // @[SingleCycleCPU.scala 70:23 SingleCycleCPU.scala 71:13]
  Controller controller ( // @[SingleCycleCPU.scala 11:26]
    .io_input_instr(controller_io_input_instr),
    .io_input_alu_branch_take(controller_io_input_alu_branch_take),
    .io_output_PC_isBranch(controller_io_output_PC_isBranch),
    .io_output_PC_isJump(controller_io_output_PC_isJump),
    .io_output_DstRegSelect(controller_io_output_DstRegSelect),
    .io_output_WBEnable(controller_io_output_WBEnable),
    .io_output_OpBSelect(controller_io_output_OpBSelect),
    .io_output_AluOp(controller_io_output_AluOp),
    .io_output_MemWriteEnable(controller_io_output_MemWriteEnable),
    .io_output_MemReadEnable(controller_io_output_MemReadEnable),
    .io_output_WBSelect(controller_io_output_WBSelect)
  );
  RegisterFile regFile ( // @[SingleCycleCPU.scala 12:23]
    .clock(regFile_clock),
    .io_rs1Addr(regFile_io_rs1Addr),
    .io_rs1Data(regFile_io_rs1Data),
    .io_rs2Addr(regFile_io_rs2Addr),
    .io_rs2Data(regFile_io_rs2Data),
    .io_writeAddr(regFile_io_writeAddr),
    .io_writeData(regFile_io_writeData),
    .io_writeEnable(regFile_io_writeEnable)
  );
  ALU alu ( // @[SingleCycleCPU.scala 13:19]
    .io_input_inputA(alu_io_input_inputA),
    .io_input_inputB(alu_io_input_inputB),
    .io_input_controlSignal(alu_io_input_controlSignal),
    .io_output_aluOutput(alu_io_output_aluOutput),
    .io_output_branchTake(alu_io_output_branchTake)
  );
  assign rt_address = io_imem_instruction[20:16]; // @[SingleCycleCPU.scala 28:31]
  assign rd_address = io_imem_instruction[15:11]; // @[SingleCycleCPU.scala 29:31]
  assign immediate = io_imem_instruction[15:0]; // @[SingleCycleCPU.scala 30:30]
  assign address = io_imem_instruction[25:0]; // @[SingleCycleCPU.scala 31:28]
  assign _T = immediate[15]; // @[SingleCycleCPU.scala 32:48]
  assign _T_2 = _T ? 16'hffff : 16'h0; // @[Bitwise.scala 71:12]
  assign extendedImmediate = {_T_2,immediate}; // @[Cat.scala 29:58]
  assign pc_plus_four = reg_pc + 32'h4; // @[SingleCycleCPU.scala 42:26]
  assign _T_5 = reg_pc[31:28]; // @[SingleCycleCPU.scala 45:25]
  assign j_target = {_T_5,address,2'h0}; // @[Cat.scala 29:58]
  assign br_target = extendedImmediate + pc_plus_four; // @[SingleCycleCPU.scala 46:34]
  assign valRT = regFile_io_rs2Data; // @[SingleCycleCPU.scala 60:19 SingleCycleCPU.scala 62:9]
  assign aluOutput = alu_io_output_aluOutput; // @[SingleCycleCPU.scala 70:23 SingleCycleCPU.scala 71:13]
  assign io_imem_address = reg_pc; // @[SingleCycleCPU.scala 17:19]
  assign io_dmem_address = alu_io_output_aluOutput; // @[SingleCycleCPU.scala 75:19]
  assign io_dmem_writedata = regFile_io_rs2Data; // @[SingleCycleCPU.scala 76:21]
  assign io_dmem_memread = controller_io_output_MemReadEnable; // @[SingleCycleCPU.scala 77:19]
  assign io_dmem_memwrite = controller_io_output_MemWriteEnable; // @[SingleCycleCPU.scala 78:20]
  assign controller_io_input_instr = io_imem_instruction; // @[SingleCycleCPU.scala 36:29]
  assign controller_io_input_alu_branch_take = alu_io_output_branchTake; // @[SingleCycleCPU.scala 72:39]
  assign regFile_clock = clock;
  assign regFile_io_rs1Addr = io_imem_instruction[25:21]; // @[SingleCycleCPU.scala 53:22]
  assign regFile_io_rs2Addr = io_imem_instruction[20:16]; // @[SingleCycleCPU.scala 54:22]
  assign regFile_io_writeAddr = controller_io_output_DstRegSelect ? rt_address : rd_address; // @[SingleCycleCPU.scala 55:24]
  assign regFile_io_writeData = controller_io_output_WBSelect ? aluOutput : io_dmem_readdata; // @[SingleCycleCPU.scala 56:24]
  assign regFile_io_writeEnable = controller_io_output_WBEnable; // @[SingleCycleCPU.scala 57:26]
  assign alu_io_input_inputA = regFile_io_rs1Data; // @[SingleCycleCPU.scala 65:23]
  assign alu_io_input_inputB = controller_io_output_OpBSelect ? valRT : extendedImmediate; // @[SingleCycleCPU.scala 67:23]
  assign alu_io_input_controlSignal = controller_io_output_AluOp; // @[SingleCycleCPU.scala 68:30]
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
  reg_pc = _RAND_0[31:0];
  `endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`endif // SYNTHESIS
  always @(posedge clock) begin
    if (reset) begin
      reg_pc <= 32'h0;
    end else if (controller_io_output_PC_isBranch) begin
      reg_pc <= br_target;
    end else if (controller_io_output_PC_isJump) begin
      reg_pc <= j_target;
    end else begin
      reg_pc <= pc_plus_four;
    end
  end
endmodule
module DualPortedCombinMemory(
  input         clock,
  input         reset,
  input  [31:0] io_imem_request_bits_address,
  output [31:0] io_imem_response_bits_data,
  input         io_dmem_request_valid,
  input  [31:0] io_dmem_request_bits_address,
  input  [31:0] io_dmem_request_bits_writedata,
  input  [1:0]  io_dmem_request_bits_operation,
  output        io_dmem_response_valid,
  output [31:0] io_dmem_response_bits_data
);
  reg [31:0] memory [0:16383]; // @[BaseMemComponents.scala 41:19]
  reg [31:0] _RAND_0;
  wire [31:0] memory__T_9_data; // @[BaseMemComponents.scala 41:19]
  wire [13:0] memory__T_9_addr; // @[BaseMemComponents.scala 41:19]
  wire [31:0] memory__T_20_data; // @[BaseMemComponents.scala 41:19]
  wire [13:0] memory__T_20_addr; // @[BaseMemComponents.scala 41:19]
  wire [31:0] memory__T_24_data; // @[BaseMemComponents.scala 41:19]
  wire [13:0] memory__T_24_addr; // @[BaseMemComponents.scala 41:19]
  wire  memory__T_24_mask; // @[BaseMemComponents.scala 41:19]
  wire  memory__T_24_en; // @[BaseMemComponents.scala 41:19]
  wire  _T_3; // @[Memory.scala 28:11]
  wire  _T_6; // @[Memory.scala 33:27]
  wire [29:0] _T_7; // @[Memory.scala 35:60]
  wire  _T_10; // @[Memory.scala 54:31]
  wire  _T_12; // @[Memory.scala 54:12]
  wire  _T_13; // @[Memory.scala 54:12]
  wire  _T_14; // @[Memory.scala 56:29]
  wire  _T_16; // @[Memory.scala 56:12]
  wire  _T_17; // @[Memory.scala 56:12]
  wire [29:0] _T_18; // @[Memory.scala 59:58]
  wire  _T_21; // @[Memory.scala 63:29]
  assign memory__T_9_addr = _T_7[13:0];
  assign memory__T_9_data = memory[memory__T_9_addr]; // @[BaseMemComponents.scala 41:19]
  assign memory__T_20_addr = _T_18[13:0];
  assign memory__T_20_data = memory[memory__T_20_addr]; // @[BaseMemComponents.scala 41:19]
  assign memory__T_24_data = io_dmem_request_bits_writedata;
  assign memory__T_24_addr = _T_18[13:0];
  assign memory__T_24_mask = 1'h1;
  assign memory__T_24_en = io_dmem_request_valid & _T_21;
  assign _T_3 = $unsigned(reset); // @[Memory.scala 28:11]
  assign _T_6 = io_imem_request_bits_address < 32'h10000; // @[Memory.scala 33:27]
  assign _T_7 = io_imem_request_bits_address[31:2]; // @[Memory.scala 35:60]
  assign _T_10 = io_dmem_request_bits_operation != 2'h1; // @[Memory.scala 54:31]
  assign _T_12 = _T_10 | _T_3; // @[Memory.scala 54:12]
  assign _T_13 = _T_12 == 1'h0; // @[Memory.scala 54:12]
  assign _T_14 = io_dmem_request_bits_address < 32'h10000; // @[Memory.scala 56:29]
  assign _T_16 = _T_14 | _T_3; // @[Memory.scala 56:12]
  assign _T_17 = _T_16 == 1'h0; // @[Memory.scala 56:12]
  assign _T_18 = io_dmem_request_bits_address[31:2]; // @[Memory.scala 59:58]
  assign _T_21 = io_dmem_request_bits_operation == 2'h2; // @[Memory.scala 63:29]
  assign io_imem_response_bits_data = _T_6 ? memory__T_9_data : 32'h0; // @[BaseMemComponents.scala 38:20 Memory.scala 35:34]
  assign io_dmem_response_valid = io_dmem_request_valid; // @[BaseMemComponents.scala 39:20 Memory.scala 15:27 Memory.scala 60:28 Memory.scala 67:28]
  assign io_dmem_response_bits_data = io_dmem_request_valid ? memory__T_20_data : 32'h0; // @[BaseMemComponents.scala 39:20 Memory.scala 59:32]
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
  for (initvar = 0; initvar < 16384; initvar = initvar+1)
    memory[initvar] = _RAND_0[31:0];
  `endif // RANDOMIZE_MEM_INIT
  `endif // RANDOMIZE
end // initial
`endif // SYNTHESIS
  always @(posedge clock) begin
    if(memory__T_24_en & memory__T_24_mask) begin
      memory[memory__T_24_addr] <= memory__T_24_data; // @[BaseMemComponents.scala 41:19]
    end
    `ifndef SYNTHESIS
    `ifdef PRINTF_COND
      if (`PRINTF_COND) begin
    `endif
        if (io_dmem_request_valid & _T_13) begin
          $fwrite(32'h80000002,"Assertion failed\n    at Memory.scala:54 assert (request.operation =/= Write)\n"); // @[Memory.scala 54:12]
        end
    `ifdef PRINTF_COND
      end
    `endif
    `endif // SYNTHESIS
    `ifndef SYNTHESIS
    `ifdef STOP_COND
      if (`STOP_COND) begin
    `endif
        if (io_dmem_request_valid & _T_13) begin
          $fatal; // @[Memory.scala 54:12]
        end
    `ifdef STOP_COND
      end
    `endif
    `endif // SYNTHESIS
    `ifndef SYNTHESIS
    `ifdef PRINTF_COND
      if (`PRINTF_COND) begin
    `endif
        if (io_dmem_request_valid & _T_17) begin
          $fwrite(32'h80000002,"Assertion failed\n    at Memory.scala:56 assert (request.address < size.U)\n"); // @[Memory.scala 56:12]
        end
    `ifdef PRINTF_COND
      end
    `endif
    `endif // SYNTHESIS
    `ifndef SYNTHESIS
    `ifdef STOP_COND
      if (`STOP_COND) begin
    `endif
        if (io_dmem_request_valid & _T_17) begin
          $fatal; // @[Memory.scala 56:12]
        end
    `ifdef STOP_COND
      end
    `endif
    `endif // SYNTHESIS
  end
endmodule
module ICombinMemPort(
  input  [31:0] io_pipeline_address,
  output [31:0] io_pipeline_instruction,
  output [31:0] io_bus_request_bits_address,
  input  [31:0] io_bus_response_bits_data
);
  assign io_pipeline_instruction = io_bus_response_bits_data; // @[BaseMemComponents.scala 54:15 MemCombinPort.scala 31:27]
  assign io_bus_request_bits_address = io_pipeline_address; // @[MemCombinPort.scala 23:26]
endmodule
module DCombinMemPort(
  input         clock,
  input         reset,
  input  [31:0] io_pipeline_address,
  input  [31:0] io_pipeline_writedata,
  input         io_pipeline_memread,
  input         io_pipeline_memwrite,
  output [31:0] io_pipeline_readdata,
  output        io_bus_request_valid,
  output [31:0] io_bus_request_bits_address,
  output [31:0] io_bus_request_bits_writedata,
  output [1:0]  io_bus_request_bits_operation,
  input         io_bus_response_valid,
  input  [31:0] io_bus_response_bits_data
);
  wire  _T_1; // @[MemCombinPort.scala 42:51]
  wire  _T_3; // @[MemCombinPort.scala 44:34]
  wire  _T_4; // @[MemCombinPort.scala 44:12]
  wire  _T_5; // @[MemCombinPort.scala 44:11]
  wire  _T_6; // @[MemCombinPort.scala 44:11]
  wire  _T_7; // @[MemCombinPort.scala 44:11]
  wire [31:0] _GEN_4; // @[MemCombinPort.scala 97:39]
  wire [31:0] _GEN_6; // @[MemCombinPort.scala 70:33]
  assign _T_1 = io_pipeline_memread | io_pipeline_memwrite; // @[MemCombinPort.scala 42:51]
  assign _T_3 = io_pipeline_memread & io_pipeline_memwrite; // @[MemCombinPort.scala 44:34]
  assign _T_4 = _T_3 == 1'h0; // @[MemCombinPort.scala 44:12]
  assign _T_5 = $unsigned(reset); // @[MemCombinPort.scala 44:11]
  assign _T_6 = _T_4 | _T_5; // @[MemCombinPort.scala 44:11]
  assign _T_7 = _T_6 == 1'h0; // @[MemCombinPort.scala 44:11]
  assign _GEN_4 = io_pipeline_memread ? io_bus_response_bits_data : 32'h0; // @[MemCombinPort.scala 97:39]
  assign _GEN_6 = io_pipeline_memwrite ? 32'h0 : _GEN_4; // @[MemCombinPort.scala 70:33]
  assign io_pipeline_readdata = io_bus_response_valid ? _GEN_6 : 32'h0; // @[BaseMemComponents.scala 71:15 MemCombinPort.scala 128:28]
  assign io_bus_request_valid = io_pipeline_memread | io_pipeline_memwrite; // @[MemCombinPort.scala 47:26 MemCombinPort.scala 65:26]
  assign io_bus_request_bits_address = io_pipeline_address; // @[MemCombinPort.scala 46:33]
  assign io_bus_request_bits_writedata = io_pipeline_writedata; // @[MemCombinPort.scala 96:37]
  assign io_bus_request_bits_operation = io_pipeline_memwrite ? 2'h2 : 2'h0; // @[MemCombinPort.scala 58:37 MemCombinPort.scala 61:37]
  always @(posedge clock) begin
    `ifndef SYNTHESIS
    `ifdef PRINTF_COND
      if (`PRINTF_COND) begin
    `endif
        if (_T_1 & _T_7) begin
          $fwrite(32'h80000002,"Assertion failed\n    at MemCombinPort.scala:44 assert(!(io.pipeline.memread && io.pipeline.memwrite))\n"); // @[MemCombinPort.scala 44:11]
        end
    `ifdef PRINTF_COND
      end
    `endif
    `endif // SYNTHESIS
    `ifndef SYNTHESIS
    `ifdef STOP_COND
      if (`STOP_COND) begin
    `endif
        if (_T_1 & _T_7) begin
          $fatal; // @[MemCombinPort.scala 44:11]
        end
    `ifdef STOP_COND
      end
    `endif
    `endif // SYNTHESIS
  end
endmodule
module Top(
  input   clock,
  input   reset,
  output  io_success
);
  wire  cpu_clock; // @[Top.scala 10:19]
  wire  cpu_reset; // @[Top.scala 10:19]
  wire [31:0] cpu_io_imem_address; // @[Top.scala 10:19]
  wire [31:0] cpu_io_imem_instruction; // @[Top.scala 10:19]
  wire [31:0] cpu_io_dmem_address; // @[Top.scala 10:19]
  wire [31:0] cpu_io_dmem_writedata; // @[Top.scala 10:19]
  wire  cpu_io_dmem_memread; // @[Top.scala 10:19]
  wire  cpu_io_dmem_memwrite; // @[Top.scala 10:19]
  wire [31:0] cpu_io_dmem_readdata; // @[Top.scala 10:19]
  wire  mem_clock; // @[Top.scala 11:19]
  wire  mem_reset; // @[Top.scala 11:19]
  wire [31:0] mem_io_imem_request_bits_address; // @[Top.scala 11:19]
  wire [31:0] mem_io_imem_response_bits_data; // @[Top.scala 11:19]
  wire  mem_io_dmem_request_valid; // @[Top.scala 11:19]
  wire [31:0] mem_io_dmem_request_bits_address; // @[Top.scala 11:19]
  wire [31:0] mem_io_dmem_request_bits_writedata; // @[Top.scala 11:19]
  wire [1:0] mem_io_dmem_request_bits_operation; // @[Top.scala 11:19]
  wire  mem_io_dmem_response_valid; // @[Top.scala 11:19]
  wire [31:0] mem_io_dmem_response_bits_data; // @[Top.scala 11:19]
  wire [31:0] imem_io_pipeline_address; // @[Top.scala 12:20]
  wire [31:0] imem_io_pipeline_instruction; // @[Top.scala 12:20]
  wire [31:0] imem_io_bus_request_bits_address; // @[Top.scala 12:20]
  wire [31:0] imem_io_bus_response_bits_data; // @[Top.scala 12:20]
  wire  dmem_clock; // @[Top.scala 13:20]
  wire  dmem_reset; // @[Top.scala 13:20]
  wire [31:0] dmem_io_pipeline_address; // @[Top.scala 13:20]
  wire [31:0] dmem_io_pipeline_writedata; // @[Top.scala 13:20]
  wire  dmem_io_pipeline_memread; // @[Top.scala 13:20]
  wire  dmem_io_pipeline_memwrite; // @[Top.scala 13:20]
  wire [31:0] dmem_io_pipeline_readdata; // @[Top.scala 13:20]
  wire  dmem_io_bus_request_valid; // @[Top.scala 13:20]
  wire [31:0] dmem_io_bus_request_bits_address; // @[Top.scala 13:20]
  wire [31:0] dmem_io_bus_request_bits_writedata; // @[Top.scala 13:20]
  wire [1:0] dmem_io_bus_request_bits_operation; // @[Top.scala 13:20]
  wire  dmem_io_bus_response_valid; // @[Top.scala 13:20]
  wire [31:0] dmem_io_bus_response_bits_data; // @[Top.scala 13:20]
  SingleCycleCPU cpu ( // @[Top.scala 10:19]
    .clock(cpu_clock),
    .reset(cpu_reset),
    .io_imem_address(cpu_io_imem_address),
    .io_imem_instruction(cpu_io_imem_instruction),
    .io_dmem_address(cpu_io_dmem_address),
    .io_dmem_writedata(cpu_io_dmem_writedata),
    .io_dmem_memread(cpu_io_dmem_memread),
    .io_dmem_memwrite(cpu_io_dmem_memwrite),
    .io_dmem_readdata(cpu_io_dmem_readdata)
  );
  DualPortedCombinMemory mem ( // @[Top.scala 11:19]
    .clock(mem_clock),
    .reset(mem_reset),
    .io_imem_request_bits_address(mem_io_imem_request_bits_address),
    .io_imem_response_bits_data(mem_io_imem_response_bits_data),
    .io_dmem_request_valid(mem_io_dmem_request_valid),
    .io_dmem_request_bits_address(mem_io_dmem_request_bits_address),
    .io_dmem_request_bits_writedata(mem_io_dmem_request_bits_writedata),
    .io_dmem_request_bits_operation(mem_io_dmem_request_bits_operation),
    .io_dmem_response_valid(mem_io_dmem_response_valid),
    .io_dmem_response_bits_data(mem_io_dmem_response_bits_data)
  );
  ICombinMemPort imem ( // @[Top.scala 12:20]
    .io_pipeline_address(imem_io_pipeline_address),
    .io_pipeline_instruction(imem_io_pipeline_instruction),
    .io_bus_request_bits_address(imem_io_bus_request_bits_address),
    .io_bus_response_bits_data(imem_io_bus_response_bits_data)
  );
  DCombinMemPort dmem ( // @[Top.scala 13:20]
    .clock(dmem_clock),
    .reset(dmem_reset),
    .io_pipeline_address(dmem_io_pipeline_address),
    .io_pipeline_writedata(dmem_io_pipeline_writedata),
    .io_pipeline_memread(dmem_io_pipeline_memread),
    .io_pipeline_memwrite(dmem_io_pipeline_memwrite),
    .io_pipeline_readdata(dmem_io_pipeline_readdata),
    .io_bus_request_valid(dmem_io_bus_request_valid),
    .io_bus_request_bits_address(dmem_io_bus_request_bits_address),
    .io_bus_request_bits_writedata(dmem_io_bus_request_bits_writedata),
    .io_bus_request_bits_operation(dmem_io_bus_request_bits_operation),
    .io_bus_response_valid(dmem_io_bus_response_valid),
    .io_bus_response_bits_data(dmem_io_bus_response_bits_data)
  );
  assign io_success = 1'h0;
  assign cpu_clock = clock;
  assign cpu_reset = reset;
  assign cpu_io_imem_instruction = imem_io_pipeline_instruction; // @[Top.scala 15:15]
  assign cpu_io_dmem_readdata = dmem_io_pipeline_readdata; // @[Top.scala 16:15]
  assign mem_clock = clock;
  assign mem_reset = reset;
  assign mem_io_imem_request_bits_address = imem_io_bus_request_bits_address; // @[BaseMemComponents.scala 22:26]
  assign mem_io_dmem_request_valid = dmem_io_bus_request_valid; // @[BaseMemComponents.scala 24:26]
  assign mem_io_dmem_request_bits_address = dmem_io_bus_request_bits_address; // @[BaseMemComponents.scala 24:26]
  assign mem_io_dmem_request_bits_writedata = dmem_io_bus_request_bits_writedata; // @[BaseMemComponents.scala 24:26]
  assign mem_io_dmem_request_bits_operation = dmem_io_bus_request_bits_operation; // @[BaseMemComponents.scala 24:26]
  assign imem_io_pipeline_address = cpu_io_imem_address; // @[Top.scala 15:15]
  assign imem_io_bus_response_bits_data = mem_io_imem_response_bits_data; // @[BaseMemComponents.scala 23:26]
  assign dmem_clock = clock;
  assign dmem_reset = reset;
  assign dmem_io_pipeline_address = cpu_io_dmem_address; // @[Top.scala 16:15]
  assign dmem_io_pipeline_writedata = cpu_io_dmem_writedata; // @[Top.scala 16:15]
  assign dmem_io_pipeline_memread = cpu_io_dmem_memread; // @[Top.scala 16:15]
  assign dmem_io_pipeline_memwrite = cpu_io_dmem_memwrite; // @[Top.scala 16:15]
  assign dmem_io_bus_response_valid = mem_io_dmem_response_valid; // @[BaseMemComponents.scala 25:26]
  assign dmem_io_bus_response_bits_data = mem_io_dmem_response_bits_data; // @[BaseMemComponents.scala 25:26]
endmodule
