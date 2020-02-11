circuit Top :
  module Controller :
    input clock : Clock
    input reset : UInt<1>
    input io_input_instr : UInt<32>
    input io_input_alu_branch_take : UInt<1>
    output io_output_PC_isBranch : UInt<1>
    output io_output_PC_isJump : UInt<1>
    output io_output_DstRegSelect : UInt<1>
    output io_output_WBEnable : UInt<1>
    output io_output_OpBSelect : UInt<1>
    output io_output_AluOp : UInt<4>
    output io_output_MemWriteEnable : UInt<1>
    output io_output_MemReadEnable : UInt<1>
    output io_output_WBSelect : UInt<1>
  
    node _T = and(io_input_instr, UInt<32>("hffffffff")) @[Lookup.scala 31:38]
    node _T_1 = eq(UInt<1>("h0"), _T) @[Lookup.scala 31:38]
    node _T_2 = and(io_input_instr, UInt<32>("hfc0007ff")) @[Lookup.scala 31:38]
    node _T_3 = eq(UInt<6>("h20"), _T_2) @[Lookup.scala 31:38]
    node _T_4 = and(io_input_instr, UInt<32>("hfc0007ff")) @[Lookup.scala 31:38]
    node _T_5 = eq(UInt<6>("h22"), _T_4) @[Lookup.scala 31:38]
    node _T_6 = and(io_input_instr, UInt<32>("hfc0007ff")) @[Lookup.scala 31:38]
    node _T_7 = eq(UInt<6>("h24"), _T_6) @[Lookup.scala 31:38]
    node _T_8 = and(io_input_instr, UInt<32>("hfc000000")) @[Lookup.scala 31:38]
    node _T_9 = eq(UInt<30>("h20000000"), _T_8) @[Lookup.scala 31:38]
    node _T_10 = and(io_input_instr, UInt<32>("hfc000000")) @[Lookup.scala 31:38]
    node _T_11 = eq(UInt<29>("h10000000"), _T_10) @[Lookup.scala 31:38]
    node _T_12 = and(io_input_instr, UInt<32>("hfc000000")) @[Lookup.scala 31:38]
    node _T_13 = eq(UInt<28>("h8000000"), _T_12) @[Lookup.scala 31:38]
    node _T_14 = and(io_input_instr, UInt<32>("hfc000000")) @[Lookup.scala 31:38]
    node _T_15 = eq(UInt<32>("h8c000000"), _T_14) @[Lookup.scala 31:38]
    node _T_16 = and(io_input_instr, UInt<32>("hfc000000")) @[Lookup.scala 31:38]
    node _T_17 = eq(UInt<32>("hac000000"), _T_16) @[Lookup.scala 31:38]
    node _T_18 = mux(_T_17, UInt<1>("h0"), UInt<1>("h0")) @[Lookup.scala 33:37]
    node _T_19 = mux(_T_15, UInt<1>("h0"), _T_18) @[Lookup.scala 33:37]
    node _T_20 = mux(_T_13, UInt<1>("h0"), _T_19) @[Lookup.scala 33:37]
    node _T_21 = mux(_T_11, UInt<1>("h1"), _T_20) @[Lookup.scala 33:37]
    node _T_22 = mux(_T_9, UInt<1>("h0"), _T_21) @[Lookup.scala 33:37]
    node _T_23 = mux(_T_7, UInt<1>("h0"), _T_22) @[Lookup.scala 33:37]
    node _T_24 = mux(_T_5, UInt<1>("h0"), _T_23) @[Lookup.scala 33:37]
    node _T_25 = mux(_T_3, UInt<1>("h0"), _T_24) @[Lookup.scala 33:37]
    node controlSignals_0 = mux(_T_1, UInt<1>("h0"), _T_25) @[Lookup.scala 33:37]
    node _T_26 = mux(_T_17, UInt<1>("h0"), UInt<1>("h0")) @[Lookup.scala 33:37]
    node _T_27 = mux(_T_15, UInt<1>("h0"), _T_26) @[Lookup.scala 33:37]
    node _T_28 = mux(_T_13, UInt<1>("h1"), _T_27) @[Lookup.scala 33:37]
    node _T_29 = mux(_T_11, UInt<1>("h0"), _T_28) @[Lookup.scala 33:37]
    node _T_30 = mux(_T_9, UInt<1>("h0"), _T_29) @[Lookup.scala 33:37]
    node _T_31 = mux(_T_7, UInt<1>("h0"), _T_30) @[Lookup.scala 33:37]
    node _T_32 = mux(_T_5, UInt<1>("h0"), _T_31) @[Lookup.scala 33:37]
    node _T_33 = mux(_T_3, UInt<1>("h0"), _T_32) @[Lookup.scala 33:37]
    node controlSignals_1 = mux(_T_1, UInt<1>("h0"), _T_33) @[Lookup.scala 33:37]
    node _T_34 = mux(_T_17, UInt<1>("h1"), UInt<1>("h1")) @[Lookup.scala 33:37]
    node _T_35 = mux(_T_15, UInt<1>("h0"), _T_34) @[Lookup.scala 33:37]
    node _T_36 = mux(_T_13, UInt<1>("h1"), _T_35) @[Lookup.scala 33:37]
    node _T_37 = mux(_T_11, UInt<1>("h1"), _T_36) @[Lookup.scala 33:37]
    node _T_38 = mux(_T_9, UInt<1>("h0"), _T_37) @[Lookup.scala 33:37]
    node _T_39 = mux(_T_7, UInt<1>("h1"), _T_38) @[Lookup.scala 33:37]
    node _T_40 = mux(_T_5, UInt<1>("h1"), _T_39) @[Lookup.scala 33:37]
    node _T_41 = mux(_T_3, UInt<1>("h1"), _T_40) @[Lookup.scala 33:37]
    node controlSignals_2 = mux(_T_1, UInt<1>("h1"), _T_41) @[Lookup.scala 33:37]
    node _T_42 = mux(_T_17, UInt<1>("h0"), UInt<1>("h0")) @[Lookup.scala 33:37]
    node _T_43 = mux(_T_15, UInt<1>("h1"), _T_42) @[Lookup.scala 33:37]
    node _T_44 = mux(_T_13, UInt<1>("h0"), _T_43) @[Lookup.scala 33:37]
    node _T_45 = mux(_T_11, UInt<1>("h0"), _T_44) @[Lookup.scala 33:37]
    node _T_46 = mux(_T_9, UInt<1>("h1"), _T_45) @[Lookup.scala 33:37]
    node _T_47 = mux(_T_7, UInt<1>("h1"), _T_46) @[Lookup.scala 33:37]
    node _T_48 = mux(_T_5, UInt<1>("h1"), _T_47) @[Lookup.scala 33:37]
    node _T_49 = mux(_T_3, UInt<1>("h1"), _T_48) @[Lookup.scala 33:37]
    node controlSignals_3 = mux(_T_1, UInt<1>("h0"), _T_49) @[Lookup.scala 33:37]
    node _T_50 = mux(_T_17, UInt<1>("h1"), UInt<1>("h1")) @[Lookup.scala 33:37]
    node _T_51 = mux(_T_15, UInt<1>("h0"), _T_50) @[Lookup.scala 33:37]
    node _T_52 = mux(_T_13, UInt<1>("h1"), _T_51) @[Lookup.scala 33:37]
    node _T_53 = mux(_T_11, UInt<1>("h1"), _T_52) @[Lookup.scala 33:37]
    node _T_54 = mux(_T_9, UInt<1>("h0"), _T_53) @[Lookup.scala 33:37]
    node _T_55 = mux(_T_7, UInt<1>("h1"), _T_54) @[Lookup.scala 33:37]
    node _T_56 = mux(_T_5, UInt<1>("h1"), _T_55) @[Lookup.scala 33:37]
    node _T_57 = mux(_T_3, UInt<1>("h1"), _T_56) @[Lookup.scala 33:37]
    node controlSignals_4 = mux(_T_1, UInt<1>("h1"), _T_57) @[Lookup.scala 33:37]
    node _T_58 = mux(_T_17, UInt<1>("h0"), UInt<1>("h0")) @[Lookup.scala 33:37]
    node _T_59 = mux(_T_15, UInt<1>("h1"), _T_58) @[Lookup.scala 33:37]
    node _T_60 = mux(_T_13, UInt<1>("h0"), _T_59) @[Lookup.scala 33:37]
    node _T_61 = mux(_T_11, UInt<3>("h4"), _T_60) @[Lookup.scala 33:37]
    node _T_62 = mux(_T_9, UInt<1>("h1"), _T_61) @[Lookup.scala 33:37]
    node _T_63 = mux(_T_7, UInt<2>("h3"), _T_62) @[Lookup.scala 33:37]
    node _T_64 = mux(_T_5, UInt<2>("h2"), _T_63) @[Lookup.scala 33:37]
    node _T_65 = mux(_T_3, UInt<1>("h1"), _T_64) @[Lookup.scala 33:37]
    node controlSignals_5 = mux(_T_1, UInt<1>("h0"), _T_65) @[Lookup.scala 33:37]
    node _T_66 = mux(_T_17, UInt<1>("h1"), UInt<1>("h0")) @[Lookup.scala 33:37]
    node _T_67 = mux(_T_15, UInt<1>("h0"), _T_66) @[Lookup.scala 33:37]
    node _T_68 = mux(_T_13, UInt<1>("h0"), _T_67) @[Lookup.scala 33:37]
    node _T_69 = mux(_T_11, UInt<1>("h0"), _T_68) @[Lookup.scala 33:37]
    node _T_70 = mux(_T_9, UInt<1>("h0"), _T_69) @[Lookup.scala 33:37]
    node _T_71 = mux(_T_7, UInt<1>("h0"), _T_70) @[Lookup.scala 33:37]
    node _T_72 = mux(_T_5, UInt<1>("h0"), _T_71) @[Lookup.scala 33:37]
    node _T_73 = mux(_T_3, UInt<1>("h0"), _T_72) @[Lookup.scala 33:37]
    node controlSignals_6 = mux(_T_1, UInt<1>("h0"), _T_73) @[Lookup.scala 33:37]
    node _T_74 = mux(_T_17, UInt<1>("h0"), UInt<1>("h0")) @[Lookup.scala 33:37]
    node _T_75 = mux(_T_15, UInt<1>("h1"), _T_74) @[Lookup.scala 33:37]
    node _T_76 = mux(_T_13, UInt<1>("h0"), _T_75) @[Lookup.scala 33:37]
    node _T_77 = mux(_T_11, UInt<1>("h0"), _T_76) @[Lookup.scala 33:37]
    node _T_78 = mux(_T_9, UInt<1>("h0"), _T_77) @[Lookup.scala 33:37]
    node _T_79 = mux(_T_7, UInt<1>("h0"), _T_78) @[Lookup.scala 33:37]
    node _T_80 = mux(_T_5, UInt<1>("h0"), _T_79) @[Lookup.scala 33:37]
    node _T_81 = mux(_T_3, UInt<1>("h0"), _T_80) @[Lookup.scala 33:37]
    node controlSignals_7 = mux(_T_1, UInt<1>("h0"), _T_81) @[Lookup.scala 33:37]
    node _T_82 = mux(_T_17, UInt<1>("h1"), UInt<1>("h1")) @[Lookup.scala 33:37]
    node _T_83 = mux(_T_15, UInt<1>("h0"), _T_82) @[Lookup.scala 33:37]
    node _T_84 = mux(_T_13, UInt<1>("h1"), _T_83) @[Lookup.scala 33:37]
    node _T_85 = mux(_T_11, UInt<1>("h1"), _T_84) @[Lookup.scala 33:37]
    node _T_86 = mux(_T_9, UInt<1>("h1"), _T_85) @[Lookup.scala 33:37]
    node _T_87 = mux(_T_7, UInt<1>("h1"), _T_86) @[Lookup.scala 33:37]
    node _T_88 = mux(_T_5, UInt<1>("h1"), _T_87) @[Lookup.scala 33:37]
    node _T_89 = mux(_T_3, UInt<1>("h1"), _T_88) @[Lookup.scala 33:37]
    node controlSignals_8 = mux(_T_1, UInt<1>("h1"), _T_89) @[Lookup.scala 33:37]
    node _T_90 = and(controlSignals_0, io_input_alu_branch_take) @[Controller.scala 73:51]
    node control_PC_isBranch = mux(_T_90, UInt<1>("h1"), UInt<1>("h0")) @[Controller.scala 73:34]
    io_output_PC_isBranch <= control_PC_isBranch @[Controller.scala 76:27]
    io_output_PC_isJump <= controlSignals_1 @[Controller.scala 75:25]
    io_output_DstRegSelect <= controlSignals_2 @[Controller.scala 77:28]
    io_output_WBEnable <= controlSignals_3 @[Controller.scala 78:24]
    io_output_OpBSelect <= controlSignals_4 @[Controller.scala 79:25]
    io_output_AluOp <= controlSignals_5 @[Controller.scala 80:21]
    io_output_MemWriteEnable <= controlSignals_6 @[Controller.scala 81:30]
    io_output_MemReadEnable <= controlSignals_7 @[Controller.scala 82:29]
    io_output_WBSelect <= controlSignals_8 @[Controller.scala 83:24]

  module RegisterFile :
    input clock : Clock
    input reset : UInt<1>
    input io_rs1Addr : UInt<5>
    output io_rs1Data : UInt<32>
    input io_rs2Addr : UInt<5>
    output io_rs2Data : UInt<32>
    input io_writeAddr : UInt<5>
    input io_writeData : UInt<32>
    input io_writeEnable : UInt<1>
  
    reg regs_0 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_0) @[RegisterFile.scala 29:17]
    reg regs_1 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_1) @[RegisterFile.scala 29:17]
    reg regs_2 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_2) @[RegisterFile.scala 29:17]
    reg regs_3 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_3) @[RegisterFile.scala 29:17]
    reg regs_4 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_4) @[RegisterFile.scala 29:17]
    reg regs_5 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_5) @[RegisterFile.scala 29:17]
    reg regs_6 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_6) @[RegisterFile.scala 29:17]
    reg regs_7 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_7) @[RegisterFile.scala 29:17]
    reg regs_8 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_8) @[RegisterFile.scala 29:17]
    reg regs_9 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_9) @[RegisterFile.scala 29:17]
    reg regs_10 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_10) @[RegisterFile.scala 29:17]
    reg regs_11 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_11) @[RegisterFile.scala 29:17]
    reg regs_12 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_12) @[RegisterFile.scala 29:17]
    reg regs_13 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_13) @[RegisterFile.scala 29:17]
    reg regs_14 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_14) @[RegisterFile.scala 29:17]
    reg regs_15 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_15) @[RegisterFile.scala 29:17]
    reg regs_16 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_16) @[RegisterFile.scala 29:17]
    reg regs_17 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_17) @[RegisterFile.scala 29:17]
    reg regs_18 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_18) @[RegisterFile.scala 29:17]
    reg regs_19 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_19) @[RegisterFile.scala 29:17]
    reg regs_20 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_20) @[RegisterFile.scala 29:17]
    reg regs_21 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_21) @[RegisterFile.scala 29:17]
    reg regs_22 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_22) @[RegisterFile.scala 29:17]
    reg regs_23 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_23) @[RegisterFile.scala 29:17]
    reg regs_24 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_24) @[RegisterFile.scala 29:17]
    reg regs_25 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_25) @[RegisterFile.scala 29:17]
    reg regs_26 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_26) @[RegisterFile.scala 29:17]
    reg regs_27 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_27) @[RegisterFile.scala 29:17]
    reg regs_28 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_28) @[RegisterFile.scala 29:17]
    reg regs_29 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_29) @[RegisterFile.scala 29:17]
    reg regs_30 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_30) @[RegisterFile.scala 29:17]
    reg regs_31 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), regs_31) @[RegisterFile.scala 29:17]
    node _regs_io_writeAddr = io_writeData @[RegisterFile.scala 33:24 RegisterFile.scala 33:24]
    node _GEN_0 = mux(eq(UInt<1>("h0"), io_writeAddr), _regs_io_writeAddr, regs_0) @[RegisterFile.scala 33:24]
    node _GEN_1 = mux(eq(UInt<1>("h1"), io_writeAddr), _regs_io_writeAddr, regs_1) @[RegisterFile.scala 33:24]
    node _GEN_2 = mux(eq(UInt<2>("h2"), io_writeAddr), _regs_io_writeAddr, regs_2) @[RegisterFile.scala 33:24]
    node _GEN_3 = mux(eq(UInt<2>("h3"), io_writeAddr), _regs_io_writeAddr, regs_3) @[RegisterFile.scala 33:24]
    node _GEN_4 = mux(eq(UInt<3>("h4"), io_writeAddr), _regs_io_writeAddr, regs_4) @[RegisterFile.scala 33:24]
    node _GEN_5 = mux(eq(UInt<3>("h5"), io_writeAddr), _regs_io_writeAddr, regs_5) @[RegisterFile.scala 33:24]
    node _GEN_6 = mux(eq(UInt<3>("h6"), io_writeAddr), _regs_io_writeAddr, regs_6) @[RegisterFile.scala 33:24]
    node _GEN_7 = mux(eq(UInt<3>("h7"), io_writeAddr), _regs_io_writeAddr, regs_7) @[RegisterFile.scala 33:24]
    node _GEN_8 = mux(eq(UInt<4>("h8"), io_writeAddr), _regs_io_writeAddr, regs_8) @[RegisterFile.scala 33:24]
    node _GEN_9 = mux(eq(UInt<4>("h9"), io_writeAddr), _regs_io_writeAddr, regs_9) @[RegisterFile.scala 33:24]
    node _GEN_10 = mux(eq(UInt<4>("ha"), io_writeAddr), _regs_io_writeAddr, regs_10) @[RegisterFile.scala 33:24]
    node _GEN_11 = mux(eq(UInt<4>("hb"), io_writeAddr), _regs_io_writeAddr, regs_11) @[RegisterFile.scala 33:24]
    node _GEN_12 = mux(eq(UInt<4>("hc"), io_writeAddr), _regs_io_writeAddr, regs_12) @[RegisterFile.scala 33:24]
    node _GEN_13 = mux(eq(UInt<4>("hd"), io_writeAddr), _regs_io_writeAddr, regs_13) @[RegisterFile.scala 33:24]
    node _GEN_14 = mux(eq(UInt<4>("he"), io_writeAddr), _regs_io_writeAddr, regs_14) @[RegisterFile.scala 33:24]
    node _GEN_15 = mux(eq(UInt<4>("hf"), io_writeAddr), _regs_io_writeAddr, regs_15) @[RegisterFile.scala 33:24]
    node _GEN_16 = mux(eq(UInt<5>("h10"), io_writeAddr), _regs_io_writeAddr, regs_16) @[RegisterFile.scala 33:24]
    node _GEN_17 = mux(eq(UInt<5>("h11"), io_writeAddr), _regs_io_writeAddr, regs_17) @[RegisterFile.scala 33:24]
    node _GEN_18 = mux(eq(UInt<5>("h12"), io_writeAddr), _regs_io_writeAddr, regs_18) @[RegisterFile.scala 33:24]
    node _GEN_19 = mux(eq(UInt<5>("h13"), io_writeAddr), _regs_io_writeAddr, regs_19) @[RegisterFile.scala 33:24]
    node _GEN_20 = mux(eq(UInt<5>("h14"), io_writeAddr), _regs_io_writeAddr, regs_20) @[RegisterFile.scala 33:24]
    node _GEN_21 = mux(eq(UInt<5>("h15"), io_writeAddr), _regs_io_writeAddr, regs_21) @[RegisterFile.scala 33:24]
    node _GEN_22 = mux(eq(UInt<5>("h16"), io_writeAddr), _regs_io_writeAddr, regs_22) @[RegisterFile.scala 33:24]
    node _GEN_23 = mux(eq(UInt<5>("h17"), io_writeAddr), _regs_io_writeAddr, regs_23) @[RegisterFile.scala 33:24]
    node _GEN_24 = mux(eq(UInt<5>("h18"), io_writeAddr), _regs_io_writeAddr, regs_24) @[RegisterFile.scala 33:24]
    node _GEN_25 = mux(eq(UInt<5>("h19"), io_writeAddr), _regs_io_writeAddr, regs_25) @[RegisterFile.scala 33:24]
    node _GEN_26 = mux(eq(UInt<5>("h1a"), io_writeAddr), _regs_io_writeAddr, regs_26) @[RegisterFile.scala 33:24]
    node _GEN_27 = mux(eq(UInt<5>("h1b"), io_writeAddr), _regs_io_writeAddr, regs_27) @[RegisterFile.scala 33:24]
    node _GEN_28 = mux(eq(UInt<5>("h1c"), io_writeAddr), _regs_io_writeAddr, regs_28) @[RegisterFile.scala 33:24]
    node _GEN_29 = mux(eq(UInt<5>("h1d"), io_writeAddr), _regs_io_writeAddr, regs_29) @[RegisterFile.scala 33:24]
    node _GEN_30 = mux(eq(UInt<5>("h1e"), io_writeAddr), _regs_io_writeAddr, regs_30) @[RegisterFile.scala 33:24]
    node _GEN_31 = mux(eq(UInt<5>("h1f"), io_writeAddr), _regs_io_writeAddr, regs_31) @[RegisterFile.scala 33:24]
    node _GEN_32 = mux(io_writeEnable, _GEN_0, regs_0) @[RegisterFile.scala 32:25]
    node _GEN_33 = mux(io_writeEnable, _GEN_1, regs_1) @[RegisterFile.scala 32:25]
    node _GEN_34 = mux(io_writeEnable, _GEN_2, regs_2) @[RegisterFile.scala 32:25]
    node _GEN_35 = mux(io_writeEnable, _GEN_3, regs_3) @[RegisterFile.scala 32:25]
    node _GEN_36 = mux(io_writeEnable, _GEN_4, regs_4) @[RegisterFile.scala 32:25]
    node _GEN_37 = mux(io_writeEnable, _GEN_5, regs_5) @[RegisterFile.scala 32:25]
    node _GEN_38 = mux(io_writeEnable, _GEN_6, regs_6) @[RegisterFile.scala 32:25]
    node _GEN_39 = mux(io_writeEnable, _GEN_7, regs_7) @[RegisterFile.scala 32:25]
    node _GEN_40 = mux(io_writeEnable, _GEN_8, regs_8) @[RegisterFile.scala 32:25]
    node _GEN_41 = mux(io_writeEnable, _GEN_9, regs_9) @[RegisterFile.scala 32:25]
    node _GEN_42 = mux(io_writeEnable, _GEN_10, regs_10) @[RegisterFile.scala 32:25]
    node _GEN_43 = mux(io_writeEnable, _GEN_11, regs_11) @[RegisterFile.scala 32:25]
    node _GEN_44 = mux(io_writeEnable, _GEN_12, regs_12) @[RegisterFile.scala 32:25]
    node _GEN_45 = mux(io_writeEnable, _GEN_13, regs_13) @[RegisterFile.scala 32:25]
    node _GEN_46 = mux(io_writeEnable, _GEN_14, regs_14) @[RegisterFile.scala 32:25]
    node _GEN_47 = mux(io_writeEnable, _GEN_15, regs_15) @[RegisterFile.scala 32:25]
    node _GEN_48 = mux(io_writeEnable, _GEN_16, regs_16) @[RegisterFile.scala 32:25]
    node _GEN_49 = mux(io_writeEnable, _GEN_17, regs_17) @[RegisterFile.scala 32:25]
    node _GEN_50 = mux(io_writeEnable, _GEN_18, regs_18) @[RegisterFile.scala 32:25]
    node _GEN_51 = mux(io_writeEnable, _GEN_19, regs_19) @[RegisterFile.scala 32:25]
    node _GEN_52 = mux(io_writeEnable, _GEN_20, regs_20) @[RegisterFile.scala 32:25]
    node _GEN_53 = mux(io_writeEnable, _GEN_21, regs_21) @[RegisterFile.scala 32:25]
    node _GEN_54 = mux(io_writeEnable, _GEN_22, regs_22) @[RegisterFile.scala 32:25]
    node _GEN_55 = mux(io_writeEnable, _GEN_23, regs_23) @[RegisterFile.scala 32:25]
    node _GEN_56 = mux(io_writeEnable, _GEN_24, regs_24) @[RegisterFile.scala 32:25]
    node _GEN_57 = mux(io_writeEnable, _GEN_25, regs_25) @[RegisterFile.scala 32:25]
    node _GEN_58 = mux(io_writeEnable, _GEN_26, regs_26) @[RegisterFile.scala 32:25]
    node _GEN_59 = mux(io_writeEnable, _GEN_27, regs_27) @[RegisterFile.scala 32:25]
    node _GEN_60 = mux(io_writeEnable, _GEN_28, regs_28) @[RegisterFile.scala 32:25]
    node _GEN_61 = mux(io_writeEnable, _GEN_29, regs_29) @[RegisterFile.scala 32:25]
    node _GEN_62 = mux(io_writeEnable, _GEN_30, regs_30) @[RegisterFile.scala 32:25]
    node _GEN_63 = mux(io_writeEnable, _GEN_31, regs_31) @[RegisterFile.scala 32:25]
    node _GEN_64 = validif(eq(UInt<1>("h0"), io_rs1Addr), regs_0) @[RegisterFile.scala 38:14]
    node _GEN_65 = mux(eq(UInt<1>("h1"), io_rs1Addr), regs_1, _GEN_64) @[RegisterFile.scala 38:14]
    node _GEN_66 = mux(eq(UInt<2>("h2"), io_rs1Addr), regs_2, _GEN_65) @[RegisterFile.scala 38:14]
    node _GEN_67 = mux(eq(UInt<2>("h3"), io_rs1Addr), regs_3, _GEN_66) @[RegisterFile.scala 38:14]
    node _GEN_68 = mux(eq(UInt<3>("h4"), io_rs1Addr), regs_4, _GEN_67) @[RegisterFile.scala 38:14]
    node _GEN_69 = mux(eq(UInt<3>("h5"), io_rs1Addr), regs_5, _GEN_68) @[RegisterFile.scala 38:14]
    node _GEN_70 = mux(eq(UInt<3>("h6"), io_rs1Addr), regs_6, _GEN_69) @[RegisterFile.scala 38:14]
    node _GEN_71 = mux(eq(UInt<3>("h7"), io_rs1Addr), regs_7, _GEN_70) @[RegisterFile.scala 38:14]
    node _GEN_72 = mux(eq(UInt<4>("h8"), io_rs1Addr), regs_8, _GEN_71) @[RegisterFile.scala 38:14]
    node _GEN_73 = mux(eq(UInt<4>("h9"), io_rs1Addr), regs_9, _GEN_72) @[RegisterFile.scala 38:14]
    node _GEN_74 = mux(eq(UInt<4>("ha"), io_rs1Addr), regs_10, _GEN_73) @[RegisterFile.scala 38:14]
    node _GEN_75 = mux(eq(UInt<4>("hb"), io_rs1Addr), regs_11, _GEN_74) @[RegisterFile.scala 38:14]
    node _GEN_76 = mux(eq(UInt<4>("hc"), io_rs1Addr), regs_12, _GEN_75) @[RegisterFile.scala 38:14]
    node _GEN_77 = mux(eq(UInt<4>("hd"), io_rs1Addr), regs_13, _GEN_76) @[RegisterFile.scala 38:14]
    node _GEN_78 = mux(eq(UInt<4>("he"), io_rs1Addr), regs_14, _GEN_77) @[RegisterFile.scala 38:14]
    node _GEN_79 = mux(eq(UInt<4>("hf"), io_rs1Addr), regs_15, _GEN_78) @[RegisterFile.scala 38:14]
    node _GEN_80 = mux(eq(UInt<5>("h10"), io_rs1Addr), regs_16, _GEN_79) @[RegisterFile.scala 38:14]
    node _GEN_81 = mux(eq(UInt<5>("h11"), io_rs1Addr), regs_17, _GEN_80) @[RegisterFile.scala 38:14]
    node _GEN_82 = mux(eq(UInt<5>("h12"), io_rs1Addr), regs_18, _GEN_81) @[RegisterFile.scala 38:14]
    node _GEN_83 = mux(eq(UInt<5>("h13"), io_rs1Addr), regs_19, _GEN_82) @[RegisterFile.scala 38:14]
    node _GEN_84 = mux(eq(UInt<5>("h14"), io_rs1Addr), regs_20, _GEN_83) @[RegisterFile.scala 38:14]
    node _GEN_85 = mux(eq(UInt<5>("h15"), io_rs1Addr), regs_21, _GEN_84) @[RegisterFile.scala 38:14]
    node _GEN_86 = mux(eq(UInt<5>("h16"), io_rs1Addr), regs_22, _GEN_85) @[RegisterFile.scala 38:14]
    node _GEN_87 = mux(eq(UInt<5>("h17"), io_rs1Addr), regs_23, _GEN_86) @[RegisterFile.scala 38:14]
    node _GEN_88 = mux(eq(UInt<5>("h18"), io_rs1Addr), regs_24, _GEN_87) @[RegisterFile.scala 38:14]
    node _GEN_89 = mux(eq(UInt<5>("h19"), io_rs1Addr), regs_25, _GEN_88) @[RegisterFile.scala 38:14]
    node _GEN_90 = mux(eq(UInt<5>("h1a"), io_rs1Addr), regs_26, _GEN_89) @[RegisterFile.scala 38:14]
    node _GEN_91 = mux(eq(UInt<5>("h1b"), io_rs1Addr), regs_27, _GEN_90) @[RegisterFile.scala 38:14]
    node _GEN_92 = mux(eq(UInt<5>("h1c"), io_rs1Addr), regs_28, _GEN_91) @[RegisterFile.scala 38:14]
    node _GEN_93 = mux(eq(UInt<5>("h1d"), io_rs1Addr), regs_29, _GEN_92) @[RegisterFile.scala 38:14]
    node _GEN_94 = mux(eq(UInt<5>("h1e"), io_rs1Addr), regs_30, _GEN_93) @[RegisterFile.scala 38:14]
    node _GEN_95 = mux(eq(UInt<5>("h1f"), io_rs1Addr), regs_31, _GEN_94) @[RegisterFile.scala 38:14]
    node _GEN_96 = validif(eq(UInt<1>("h0"), io_rs2Addr), regs_0) @[RegisterFile.scala 39:14]
    node _GEN_97 = mux(eq(UInt<1>("h1"), io_rs2Addr), regs_1, _GEN_96) @[RegisterFile.scala 39:14]
    node _GEN_98 = mux(eq(UInt<2>("h2"), io_rs2Addr), regs_2, _GEN_97) @[RegisterFile.scala 39:14]
    node _GEN_99 = mux(eq(UInt<2>("h3"), io_rs2Addr), regs_3, _GEN_98) @[RegisterFile.scala 39:14]
    node _GEN_100 = mux(eq(UInt<3>("h4"), io_rs2Addr), regs_4, _GEN_99) @[RegisterFile.scala 39:14]
    node _GEN_101 = mux(eq(UInt<3>("h5"), io_rs2Addr), regs_5, _GEN_100) @[RegisterFile.scala 39:14]
    node _GEN_102 = mux(eq(UInt<3>("h6"), io_rs2Addr), regs_6, _GEN_101) @[RegisterFile.scala 39:14]
    node _GEN_103 = mux(eq(UInt<3>("h7"), io_rs2Addr), regs_7, _GEN_102) @[RegisterFile.scala 39:14]
    node _GEN_104 = mux(eq(UInt<4>("h8"), io_rs2Addr), regs_8, _GEN_103) @[RegisterFile.scala 39:14]
    node _GEN_105 = mux(eq(UInt<4>("h9"), io_rs2Addr), regs_9, _GEN_104) @[RegisterFile.scala 39:14]
    node _GEN_106 = mux(eq(UInt<4>("ha"), io_rs2Addr), regs_10, _GEN_105) @[RegisterFile.scala 39:14]
    node _GEN_107 = mux(eq(UInt<4>("hb"), io_rs2Addr), regs_11, _GEN_106) @[RegisterFile.scala 39:14]
    node _GEN_108 = mux(eq(UInt<4>("hc"), io_rs2Addr), regs_12, _GEN_107) @[RegisterFile.scala 39:14]
    node _GEN_109 = mux(eq(UInt<4>("hd"), io_rs2Addr), regs_13, _GEN_108) @[RegisterFile.scala 39:14]
    node _GEN_110 = mux(eq(UInt<4>("he"), io_rs2Addr), regs_14, _GEN_109) @[RegisterFile.scala 39:14]
    node _GEN_111 = mux(eq(UInt<4>("hf"), io_rs2Addr), regs_15, _GEN_110) @[RegisterFile.scala 39:14]
    node _GEN_112 = mux(eq(UInt<5>("h10"), io_rs2Addr), regs_16, _GEN_111) @[RegisterFile.scala 39:14]
    node _GEN_113 = mux(eq(UInt<5>("h11"), io_rs2Addr), regs_17, _GEN_112) @[RegisterFile.scala 39:14]
    node _GEN_114 = mux(eq(UInt<5>("h12"), io_rs2Addr), regs_18, _GEN_113) @[RegisterFile.scala 39:14]
    node _GEN_115 = mux(eq(UInt<5>("h13"), io_rs2Addr), regs_19, _GEN_114) @[RegisterFile.scala 39:14]
    node _GEN_116 = mux(eq(UInt<5>("h14"), io_rs2Addr), regs_20, _GEN_115) @[RegisterFile.scala 39:14]
    node _GEN_117 = mux(eq(UInt<5>("h15"), io_rs2Addr), regs_21, _GEN_116) @[RegisterFile.scala 39:14]
    node _GEN_118 = mux(eq(UInt<5>("h16"), io_rs2Addr), regs_22, _GEN_117) @[RegisterFile.scala 39:14]
    node _GEN_119 = mux(eq(UInt<5>("h17"), io_rs2Addr), regs_23, _GEN_118) @[RegisterFile.scala 39:14]
    node _GEN_120 = mux(eq(UInt<5>("h18"), io_rs2Addr), regs_24, _GEN_119) @[RegisterFile.scala 39:14]
    node _GEN_121 = mux(eq(UInt<5>("h19"), io_rs2Addr), regs_25, _GEN_120) @[RegisterFile.scala 39:14]
    node _GEN_122 = mux(eq(UInt<5>("h1a"), io_rs2Addr), regs_26, _GEN_121) @[RegisterFile.scala 39:14]
    node _GEN_123 = mux(eq(UInt<5>("h1b"), io_rs2Addr), regs_27, _GEN_122) @[RegisterFile.scala 39:14]
    node _GEN_124 = mux(eq(UInt<5>("h1c"), io_rs2Addr), regs_28, _GEN_123) @[RegisterFile.scala 39:14]
    node _GEN_125 = mux(eq(UInt<5>("h1d"), io_rs2Addr), regs_29, _GEN_124) @[RegisterFile.scala 39:14]
    node _GEN_126 = mux(eq(UInt<5>("h1e"), io_rs2Addr), regs_30, _GEN_125) @[RegisterFile.scala 39:14]
    node _GEN_127 = mux(eq(UInt<5>("h1f"), io_rs2Addr), regs_31, _GEN_126) @[RegisterFile.scala 39:14]
    node _regs_io_rs1Addr = _GEN_95 @[RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14 RegisterFile.scala 38:14]
    node _regs_io_rs2Addr = _GEN_127 @[RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14 RegisterFile.scala 39:14]
    io_rs1Data <= _regs_io_rs1Addr @[RegisterFile.scala 38:14]
    io_rs2Data <= _regs_io_rs2Addr @[RegisterFile.scala 39:14]
    regs_0 <= _GEN_32 @[RegisterFile.scala 33:24]
    regs_1 <= _GEN_33 @[RegisterFile.scala 33:24]
    regs_2 <= _GEN_34 @[RegisterFile.scala 33:24]
    regs_3 <= _GEN_35 @[RegisterFile.scala 33:24]
    regs_4 <= _GEN_36 @[RegisterFile.scala 33:24]
    regs_5 <= _GEN_37 @[RegisterFile.scala 33:24]
    regs_6 <= _GEN_38 @[RegisterFile.scala 33:24]
    regs_7 <= _GEN_39 @[RegisterFile.scala 33:24]
    regs_8 <= _GEN_40 @[RegisterFile.scala 33:24]
    regs_9 <= _GEN_41 @[RegisterFile.scala 33:24]
    regs_10 <= _GEN_42 @[RegisterFile.scala 33:24]
    regs_11 <= _GEN_43 @[RegisterFile.scala 33:24]
    regs_12 <= _GEN_44 @[RegisterFile.scala 33:24]
    regs_13 <= _GEN_45 @[RegisterFile.scala 33:24]
    regs_14 <= _GEN_46 @[RegisterFile.scala 33:24]
    regs_15 <= _GEN_47 @[RegisterFile.scala 33:24]
    regs_16 <= _GEN_48 @[RegisterFile.scala 33:24]
    regs_17 <= _GEN_49 @[RegisterFile.scala 33:24]
    regs_18 <= _GEN_50 @[RegisterFile.scala 33:24]
    regs_19 <= _GEN_51 @[RegisterFile.scala 33:24]
    regs_20 <= _GEN_52 @[RegisterFile.scala 33:24]
    regs_21 <= _GEN_53 @[RegisterFile.scala 33:24]
    regs_22 <= _GEN_54 @[RegisterFile.scala 33:24]
    regs_23 <= _GEN_55 @[RegisterFile.scala 33:24]
    regs_24 <= _GEN_56 @[RegisterFile.scala 33:24]
    regs_25 <= _GEN_57 @[RegisterFile.scala 33:24]
    regs_26 <= _GEN_58 @[RegisterFile.scala 33:24]
    regs_27 <= _GEN_59 @[RegisterFile.scala 33:24]
    regs_28 <= _GEN_60 @[RegisterFile.scala 33:24]
    regs_29 <= _GEN_61 @[RegisterFile.scala 33:24]
    regs_30 <= _GEN_62 @[RegisterFile.scala 33:24]
    regs_31 <= _GEN_63 @[RegisterFile.scala 33:24]

  module ALU :
    input clock : Clock
    input reset : UInt<1>
    input io_input_inputA : UInt<32>
    input io_input_inputB : UInt<32>
    input io_input_controlSignal : UInt<4>
    output io_output_aluOutput : UInt<32>
    output io_output_branchTake : UInt<1>
  
    node _T = add(io_input_inputA, io_input_inputB) @[ALU.scala 40:31]
    node _T_1 = tail(_T, 1) @[ALU.scala 40:31]
    node _T_2 = sub(io_input_inputA, io_input_inputB) @[ALU.scala 41:31]
    node _T_3 = tail(_T_2, 1) @[ALU.scala 41:31]
    node _T_4 = and(io_input_inputA, io_input_inputB) @[ALU.scala 42:31]
    node _T_5 = eq(UInt<4>("h9"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_7 = validif(UInt<1>("h0"), UInt<32>("h0"))
    node _T_6 = mux(_T_5, _T_7, io_input_inputA) @[Mux.scala 68:16]
    node _T_8 = eq(UInt<4>("h8"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_10 = validif(UInt<1>("h0"), UInt<32>("h0"))
    node _T_9 = mux(_T_8, _T_10, _T_6) @[Mux.scala 68:16]
    node _T_11 = eq(UInt<4>("h7"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_13 = validif(UInt<1>("h0"), UInt<32>("h0"))
    node _T_12 = mux(_T_11, _T_13, _T_9) @[Mux.scala 68:16]
    node _T_14 = eq(UInt<4>("h6"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_16 = validif(UInt<1>("h0"), UInt<32>("h0"))
    node _T_15 = mux(_T_14, _T_16, _T_12) @[Mux.scala 68:16]
    node _T_17 = eq(UInt<4>("h5"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_19 = validif(UInt<1>("h0"), UInt<32>("h0"))
    node _T_18 = mux(_T_17, _T_19, _T_15) @[Mux.scala 68:16]
    node _T_20 = eq(UInt<4>("h4"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_22 = validif(UInt<1>("h0"), UInt<32>("h0"))
    node _T_21 = mux(_T_20, _T_22, _T_18) @[Mux.scala 68:16]
    node _T_23 = eq(UInt<4>("h3"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_24 = mux(_T_23, _T_4, _T_21) @[Mux.scala 68:16]
    node _T_25 = eq(UInt<4>("h2"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_26 = mux(_T_25, _T_3, _T_24) @[Mux.scala 68:16]
    node _T_27 = eq(UInt<4>("h1"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_28 = mux(_T_27, _T_1, _T_26) @[Mux.scala 68:16]
    node _T_29 = eq(io_input_inputA, io_input_inputB) @[ALU.scala 52:41]
    node _T_30 = eq(io_input_inputA, io_input_inputB) @[ALU.scala 53:43]
    node _T_31 = eq(_T_30, UInt<1>("h0")) @[ALU.scala 53:25]
    node _T_32 = gt(io_input_inputA, UInt<1>("h0")) @[ALU.scala 54:47]
    node _T_33 = geq(io_input_inputA, UInt<1>("h0")) @[ALU.scala 55:52]
    node _T_34 = geq(io_input_inputA, UInt<1>("h0")) @[ALU.scala 56:45]
    node _T_35 = eq(_T_34, UInt<1>("h0")) @[ALU.scala 56:27]
    node _T_36 = gt(io_input_inputA, UInt<1>("h0")) @[ALU.scala 57:50]
    node _T_37 = eq(_T_36, UInt<1>("h0")) @[ALU.scala 57:32]
    node _T_38 = eq(UInt<4>("h9"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_39 = mux(_T_38, _T_37, UInt<1>("h0")) @[Mux.scala 68:16]
    node _T_40 = eq(UInt<4>("h8"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_41 = mux(_T_40, _T_35, _T_39) @[Mux.scala 68:16]
    node _T_42 = eq(UInt<4>("h7"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_43 = mux(_T_42, _T_33, _T_41) @[Mux.scala 68:16]
    node _T_44 = eq(UInt<4>("h6"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_45 = mux(_T_44, _T_32, _T_43) @[Mux.scala 68:16]
    node _T_46 = eq(UInt<4>("h5"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_47 = mux(_T_46, _T_31, _T_45) @[Mux.scala 68:16]
    node _T_48 = eq(UInt<4>("h4"), io_input_controlSignal) @[Mux.scala 68:19]
    node _T_49 = mux(_T_48, _T_29, _T_47) @[Mux.scala 68:16]
    io_output_aluOutput <= _T_28 @[ALU.scala 39:25]
    io_output_branchTake <= _T_49 @[ALU.scala 51:26]

  module SingleCycleCPU :
    input clock : Clock
    input reset : UInt<1>
    output io_imem_address : UInt<32>
    output io_imem_valid : UInt<1>
    input io_imem_good : UInt<1>
    input io_imem_instruction : UInt<32>
    output io_dmem_address : UInt<32>
    output io_dmem_valid : UInt<1>
    input io_dmem_good : UInt<1>
    output io_dmem_writedata : UInt<32>
    output io_dmem_memread : UInt<1>
    output io_dmem_memwrite : UInt<1>
    input io_dmem_readdata : UInt<32>
  
    inst controller of Controller @[SingleCycleCPU.scala 13:26]
    inst regFile of RegisterFile @[SingleCycleCPU.scala 14:23]
    inst alu of ALU @[SingleCycleCPU.scala 15:19]
    reg reg_pc : UInt<32>, clock with :
      reset => (UInt<1>("h0"), reg_pc) @[SingleCycleCPU.scala 12:23]
    node rs_address = bits(io_imem_instruction, 25, 21) @[SingleCycleCPU.scala 29:31]
    node rt_address = bits(io_imem_instruction, 20, 16) @[SingleCycleCPU.scala 30:31]
    node rd_address = bits(io_imem_instruction, 15, 11) @[SingleCycleCPU.scala 31:31]
    node immediate = bits(io_imem_instruction, 15, 0) @[SingleCycleCPU.scala 32:30]
    node address = bits(io_imem_instruction, 25, 0) @[SingleCycleCPU.scala 33:28]
    node _T = bits(immediate, 15, 15) @[SingleCycleCPU.scala 34:48]
    node _T_1 = bits(_T, 0, 0) @[Bitwise.scala 71:15]
    node _T_2 = mux(_T_1, UInt<16>("hffff"), UInt<16>("h0")) @[Bitwise.scala 71:12]
    node extendedImmediate = cat(_T_2, immediate) @[Cat.scala 29:58]
    node _T_3 = add(reg_pc, UInt<3>("h4")) @[SingleCycleCPU.scala 44:26]
    node _T_4 = tail(_T_3, 1) @[SingleCycleCPU.scala 44:26]
    node _T_5 = bits(reg_pc, 31, 28) @[SingleCycleCPU.scala 47:25]
    node _T_6 = mux(UInt<1>("h0"), UInt<2>("h3"), UInt<2>("h0")) @[Bitwise.scala 71:12]
    node _T_7 = cat(_T_5, address) @[Cat.scala 29:58]
    node _T_8 = cat(_T_7, _T_6) @[Cat.scala 29:58]
    node pc_plus_four = _T_4 @[SingleCycleCPU.scala 40:26 SingleCycleCPU.scala 44:16]
    node _T_9 = add(extendedImmediate, pc_plus_four) @[SingleCycleCPU.scala 48:34]
    node _T_10 = tail(_T_9, 1) @[SingleCycleCPU.scala 48:34]
    node j_target = _T_8 @[SingleCycleCPU.scala 43:22 SingleCycleCPU.scala 47:12]
    node _T_11 = mux(controller.io_output_PC_isJump, j_target, pc_plus_four) @[Mux.scala 87:16]
    node br_target = _T_10 @[SingleCycleCPU.scala 42:23 SingleCycleCPU.scala 48:13]
    node _T_12 = mux(controller.io_output_PC_isBranch, br_target, _T_11) @[Mux.scala 87:16]
    node _T_13 = mux(controller.io_output_DstRegSelect, rt_address, rd_address) @[SingleCycleCPU.scala 57:30]
    node valRT = regFile.io_rs2Data @[SingleCycleCPU.scala 62:19 SingleCycleCPU.scala 64:9]
    node _T_14 = mux(controller.io_output_OpBSelect, valRT, extendedImmediate) @[SingleCycleCPU.scala 69:29]
    node aluOutput = alu.io_output_aluOutput @[SingleCycleCPU.scala 72:23 SingleCycleCPU.scala 73:13]
    node readData = io_dmem_readdata @[SingleCycleCPU.scala 85:22 SingleCycleCPU.scala 86:12]
    node _T_15 = mux(controller.io_output_WBSelect, aluOutput, readData) @[SingleCycleCPU.scala 87:17]
    node pc_next = _T_12 @[SingleCycleCPU.scala 41:21 SingleCycleCPU.scala 49:11]
    node wb_data = _T_15 @[SingleCycleCPU.scala 54:21 SingleCycleCPU.scala 87:11]
    node valRS = regFile.io_rs1Data @[SingleCycleCPU.scala 61:19 SingleCycleCPU.scala 63:9]
    io_imem_address <= reg_pc @[SingleCycleCPU.scala 19:19]
    io_imem_valid <= UInt<1>("h1") @[SingleCycleCPU.scala 20:17]
    io_dmem_address <= aluOutput @[SingleCycleCPU.scala 77:19]
    io_dmem_valid <= UInt<1>("h1") @[SingleCycleCPU.scala 82:17]
    io_dmem_writedata <= valRT @[SingleCycleCPU.scala 78:21]
    io_dmem_memread <= controller.io_output_MemReadEnable @[SingleCycleCPU.scala 79:19]
    io_dmem_memwrite <= controller.io_output_MemWriteEnable @[SingleCycleCPU.scala 80:20]
    reg_pc <= mux(reset, UInt<32>("h0"), pc_next) @[SingleCycleCPU.scala 89:10]
    controller.clock <= clock
    controller.reset <= reset
    controller.io_input_instr <= io_imem_instruction @[SingleCycleCPU.scala 38:29]
    controller.io_input_alu_branch_take <= alu.io_output_branchTake @[SingleCycleCPU.scala 74:39]
    regFile.clock <= clock
    regFile.reset <= reset
    regFile.io_rs1Addr <= rs_address @[SingleCycleCPU.scala 55:22]
    regFile.io_rs2Addr <= rt_address @[SingleCycleCPU.scala 56:22]
    regFile.io_writeAddr <= _T_13 @[SingleCycleCPU.scala 57:24]
    regFile.io_writeData <= wb_data @[SingleCycleCPU.scala 58:24]
    regFile.io_writeEnable <= controller.io_output_WBEnable @[SingleCycleCPU.scala 59:26]
    alu.clock <= clock
    alu.reset <= reset
    alu.io_input_inputA <= valRS @[SingleCycleCPU.scala 67:23]
    alu.io_input_inputB <= _T_14 @[SingleCycleCPU.scala 69:23]
    alu.io_input_controlSignal <= controller.io_output_AluOp @[SingleCycleCPU.scala 70:30]

  module DualPortedCombinMemory :
    input clock : Clock
    input reset : UInt<1>
    output io_imem_request_ready : UInt<1>
    input io_imem_request_valid : UInt<1>
    input io_imem_request_bits_address : UInt<32>
    input io_imem_request_bits_writedata : UInt<32>
    input io_imem_request_bits_operation : UInt<2>
    output io_imem_response_valid : UInt<1>
    output io_imem_response_bits_data : UInt<32>
    output io_dmem_request_ready : UInt<1>
    input io_dmem_request_valid : UInt<1>
    input io_dmem_request_bits_address : UInt<32>
    input io_dmem_request_bits_writedata : UInt<32>
    input io_dmem_request_bits_operation : UInt<2>
    output io_dmem_response_valid : UInt<1>
    output io_dmem_response_bits_data : UInt<32>
  
    mem memory : @[BaseMemComponents.scala 41:19]
      data-type => UInt<32>
      depth => 16384
      read-latency => 0
      write-latency => 1
      reader => _T_9
      reader => _T_20
      writer => _T_24
      read-under-write => undefined
    node _T_2 = eq(io_imem_request_bits_operation, UInt<1>("h0")) @[Memory.scala 28:30]
    node _T_3 = asUInt(reset) @[Memory.scala 28:11]
    node _T_4 = or(_T_2, _T_3) @[Memory.scala 28:11]
    node _T_5 = eq(_T_4, UInt<1>("h0")) @[Memory.scala 28:11]
    node _T_6 = lt(io_imem_request_bits_address, UInt<17>("h10000")) @[Memory.scala 33:27]
    node _T_7 = shr(io_imem_request_bits_address, 2) @[Memory.scala 35:60]
    node _T_8 = bits(_T_7, 13, 0) @[Memory.scala 35:43]
    node _GEN_0 = mux(_T_6, UInt<1>("h1"), UInt<1>("h0")) @[Memory.scala 33:37]
    node _GEN_1 = validif(_T_6, _T_8) @[Memory.scala 33:37]
    node _GEN_2 = validif(_T_6, clock) @[Memory.scala 33:37]
    node _T_bits_data = UInt<32>("h0") @[BaseMemComponents.scala 38:35 BaseMemComponents.scala 38:35]
    node _GEN_3 = mux(_T_6, memory._T_9.data, _T_bits_data) @[Memory.scala 33:37]
    node _GEN_4 = mux(io_imem_request_valid, _GEN_0, UInt<1>("h0")) @[Memory.scala 23:32]
    node _GEN_5 = validif(io_imem_request_valid, _GEN_1) @[Memory.scala 23:32]
    node _GEN_6 = validif(io_imem_request_valid, _GEN_2) @[Memory.scala 23:32]
    node _GEN_7 = mux(io_imem_request_valid, _GEN_3, _T_bits_data) @[Memory.scala 23:32]
    node _T_10 = neq(io_dmem_request_bits_operation, UInt<1>("h1")) @[Memory.scala 54:31]
    node _T_11 = asUInt(reset) @[Memory.scala 54:12]
    node _T_12 = or(_T_10, _T_11) @[Memory.scala 54:12]
    node _T_13 = eq(_T_12, UInt<1>("h0")) @[Memory.scala 54:12]
    node _T_14 = lt(io_dmem_request_bits_address, UInt<17>("h10000")) @[Memory.scala 56:29]
    node _T_15 = asUInt(reset) @[Memory.scala 56:12]
    node _T_16 = or(_T_14, _T_15) @[Memory.scala 56:12]
    node _T_17 = eq(_T_16, UInt<1>("h0")) @[Memory.scala 56:12]
    node _T_18 = shr(io_dmem_request_bits_address, 2) @[Memory.scala 59:58]
    node _T_19 = bits(_T_18, 13, 0) @[Memory.scala 59:46]
    node _T_21 = eq(io_dmem_request_bits_operation, UInt<2>("h2")) @[Memory.scala 63:29]
    node _T_22 = shr(io_dmem_request_bits_address, 2) @[Memory.scala 64:25]
    node _T_23 = bits(_T_22, 13, 0) @[Memory.scala 64:13]
    node _GEN_8 = validif(_T_21, _T_23) @[Memory.scala 63:44]
    node _GEN_9 = validif(_T_21, clock) @[Memory.scala 63:44]
    node _GEN_10 = mux(_T_21, UInt<1>("h1"), UInt<1>("h0")) @[Memory.scala 63:44]
    node _GEN_11 = validif(_T_21, UInt<1>("h1")) @[Memory.scala 63:44]
    node _GEN_12 = validif(_T_21, io_dmem_request_bits_writedata) @[Memory.scala 63:44]
    node _GEN_13 = validif(io_dmem_request_valid, _T_19) @[Memory.scala 50:32]
    node _GEN_14 = validif(io_dmem_request_valid, clock) @[Memory.scala 50:32]
    node _GEN_15 = mux(io_dmem_request_valid, UInt<1>("h1"), UInt<1>("h0")) @[Memory.scala 50:32]
    node _T_1_bits_data = UInt<32>("h0") @[BaseMemComponents.scala 39:35 BaseMemComponents.scala 39:35]
    node _GEN_16 = mux(io_dmem_request_valid, memory._T_20.data, _T_1_bits_data) @[Memory.scala 50:32]
    node _GEN_17 = validif(io_dmem_request_valid, _GEN_8) @[Memory.scala 50:32]
    node _GEN_18 = validif(io_dmem_request_valid, _GEN_9) @[Memory.scala 50:32]
    node _GEN_19 = mux(io_dmem_request_valid, _GEN_10, UInt<1>("h0")) @[Memory.scala 50:32]
    node _GEN_20 = validif(io_dmem_request_valid, _GEN_11) @[Memory.scala 50:32]
    node _GEN_21 = validif(io_dmem_request_valid, _GEN_12) @[Memory.scala 50:32]
    node _T_valid = UInt<1>("h0") @[BaseMemComponents.scala 38:35 BaseMemComponents.scala 38:35]
    node _T_1_valid = UInt<1>("h0") @[BaseMemComponents.scala 39:35 BaseMemComponents.scala 39:35]
    io_imem_request_ready <= UInt<1>("h1") @[Memory.scala 17:26]
    io_imem_response_valid <= _GEN_4 @[BaseMemComponents.scala 38:20 Memory.scala 15:27 Memory.scala 34:30 Memory.scala 37:30 Memory.scala 40:28]
    io_imem_response_bits_data <= _GEN_7 @[BaseMemComponents.scala 38:20 Memory.scala 35:34]
    io_dmem_request_ready <= UInt<1>("h1") @[Memory.scala 17:26]
    io_dmem_response_valid <= _GEN_15 @[BaseMemComponents.scala 39:20 Memory.scala 15:27 Memory.scala 60:28 Memory.scala 67:28]
    io_dmem_response_bits_data <= _GEN_16 @[BaseMemComponents.scala 39:20 Memory.scala 59:32]
    memory._T_9.addr <= _GEN_5 @[Memory.scala 35:43]
    memory._T_9.en <= _GEN_4 @[BaseMemComponents.scala 41:19 Memory.scala 35:43]
    memory._T_9.clk <= _GEN_6 @[Memory.scala 35:43]
    memory._T_20.addr <= _GEN_13 @[Memory.scala 59:46]
    memory._T_20.en <= _GEN_15 @[BaseMemComponents.scala 41:19 Memory.scala 59:46]
    memory._T_20.clk <= _GEN_14 @[Memory.scala 59:46]
    memory._T_24.addr <= _GEN_17 @[Memory.scala 64:13]
    memory._T_24.en <= _GEN_19 @[BaseMemComponents.scala 41:19 Memory.scala 64:13]
    memory._T_24.clk <= _GEN_18 @[Memory.scala 64:13]
    memory._T_24.data <= _GEN_21 @[Memory.scala 64:31]
    memory._T_24.mask <= _GEN_20 @[Memory.scala 64:13 Memory.scala 64:31]
    printf(clock, and(and(and(UInt<1>("h1"), io_imem_request_valid), _T_5), UInt<1>("h1")), "Assertion failed\n    at Memory.scala:28 assert(request.operation === Read)\n") @[Memory.scala 28:11]
    stop(clock, and(and(and(UInt<1>("h1"), io_imem_request_valid), _T_5), UInt<1>("h1")), 1) @[Memory.scala 28:11]
    printf(clock, and(and(and(UInt<1>("h1"), io_dmem_request_valid), _T_13), UInt<1>("h1")), "Assertion failed\n    at Memory.scala:54 assert (request.operation =/= Write)\n") @[Memory.scala 54:12]
    stop(clock, and(and(and(UInt<1>("h1"), io_dmem_request_valid), _T_13), UInt<1>("h1")), 1) @[Memory.scala 54:12]
    printf(clock, and(and(and(UInt<1>("h1"), io_dmem_request_valid), _T_17), UInt<1>("h1")), "Assertion failed\n    at Memory.scala:56 assert (request.address < size.U)\n") @[Memory.scala 56:12]
    stop(clock, and(and(and(UInt<1>("h1"), io_dmem_request_valid), _T_17), UInt<1>("h1")), 1) @[Memory.scala 56:12]

  module ICombinMemPort :
    input clock : Clock
    input reset : UInt<1>
    input io_pipeline_address : UInt<32>
    input io_pipeline_valid : UInt<1>
    output io_pipeline_good : UInt<1>
    output io_pipeline_instruction : UInt<32>
    input io_bus_request_ready : UInt<1>
    output io_bus_request_valid : UInt<1>
    output io_bus_request_bits_address : UInt<32>
    output io_bus_request_bits_writedata : UInt<32>
    output io_bus_request_bits_operation : UInt<2>
    input io_bus_response_valid : UInt<1>
    input io_bus_response_bits_data : UInt<32>
  
    node _T_1_operation = pad(UInt<1>("h0"), 2) @[MemCombinPort.scala 18:23 MemCombinPort.scala 20:23]
    node _GEN_0 = validif(io_pipeline_valid, _T_1_operation) @[MemCombinPort.scala 17:28]
    node _T_1_writedata = pad(UInt<1>("h0"), 32) @[MemCombinPort.scala 18:23 MemCombinPort.scala 21:23]
    node _GEN_1 = validif(io_pipeline_valid, _T_1_writedata) @[MemCombinPort.scala 17:28]
    node _T_1_address = io_pipeline_address @[MemCombinPort.scala 18:23 MemCombinPort.scala 19:23]
    node _GEN_2 = validif(io_pipeline_valid, _T_1_address) @[MemCombinPort.scala 17:28]
    node _GEN_3 = mux(io_pipeline_valid, UInt<1>("h1"), UInt<1>("h0")) @[MemCombinPort.scala 17:28]
    node _T_address = io_pipeline_address @[BaseMemComponents.scala 54:31 BaseMemComponents.scala 54:31 BaseMemComponents.scala 54:15]
    node _T_valid = io_pipeline_valid @[BaseMemComponents.scala 54:31 BaseMemComponents.scala 54:31 BaseMemComponents.scala 54:15]
    node _T_good = UInt<1>("h0") @[BaseMemComponents.scala 54:31 BaseMemComponents.scala 54:31]
    node _T_instruction = UInt<32>("h0") @[BaseMemComponents.scala 54:31 BaseMemComponents.scala 54:31]
    io_pipeline_good <= UInt<1>("h1") @[BaseMemComponents.scala 54:15 MemCombinPort.scala 30:20]
    io_pipeline_instruction <= io_bus_response_bits_data @[BaseMemComponents.scala 54:15 MemCombinPort.scala 31:27]
    io_bus_request_valid <= _GEN_3 @[MemCombinPort.scala 24:26 MemCombinPort.scala 26:26]
    io_bus_request_bits_address <= _GEN_2 @[MemCombinPort.scala 23:26]
    io_bus_request_bits_writedata <= _GEN_1 @[MemCombinPort.scala 23:26]
    io_bus_request_bits_operation <= _GEN_0 @[MemCombinPort.scala 23:26]

  module DCombinMemPort :
    input clock : Clock
    input reset : UInt<1>
    input io_pipeline_address : UInt<32>
    input io_pipeline_valid : UInt<1>
    output io_pipeline_good : UInt<1>
    input io_pipeline_writedata : UInt<32>
    input io_pipeline_memread : UInt<1>
    input io_pipeline_memwrite : UInt<1>
    output io_pipeline_readdata : UInt<32>
    input io_bus_request_ready : UInt<1>
    output io_bus_request_valid : UInt<1>
    output io_bus_request_bits_address : UInt<32>
    output io_bus_request_bits_writedata : UInt<32>
    output io_bus_request_bits_operation : UInt<2>
    input io_bus_response_valid : UInt<1>
    input io_bus_response_bits_data : UInt<32>
  
    node _T_1 = or(io_pipeline_memread, io_pipeline_memwrite) @[MemCombinPort.scala 42:51]
    node _T_2 = and(io_pipeline_valid, _T_1) @[MemCombinPort.scala 42:27]
    node _T_3 = and(io_pipeline_memread, io_pipeline_memwrite) @[MemCombinPort.scala 44:34]
    node _T_4 = eq(_T_3, UInt<1>("h0")) @[MemCombinPort.scala 44:12]
    node _T_5 = asUInt(reset) @[MemCombinPort.scala 44:11]
    node _T_6 = or(_T_4, _T_5) @[MemCombinPort.scala 44:11]
    node _T_7 = eq(_T_6, UInt<1>("h0")) @[MemCombinPort.scala 44:11]
    node _GEN_0 = mux(io_pipeline_memwrite, UInt<2>("h2"), UInt<1>("h0")) @[MemCombinPort.scala 49:33]
    node _GEN_1 = validif(_T_2, io_pipeline_address) @[MemCombinPort.scala 42:77]
    node _GEN_2 = mux(_T_2, UInt<1>("h1"), UInt<1>("h0")) @[MemCombinPort.scala 42:77]
    node _GEN_3 = validif(_T_2, _GEN_0) @[MemCombinPort.scala 42:77]
    node _T_11 = bits(io_pipeline_address, 1, 0) @[MemCombinPort.scala 102:39]
    node _T_9 = io_bus_response_bits_data @[MemCombinPort.scala 99:36 MemCombinPort.scala 112:21]
    node _T_10 = _T_9 @[MemCombinPort.scala 100:36 MemCombinPort.scala 127:26]
    node _T_readdata = UInt<32>("h0") @[BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:31]
    node _GEN_4 = mux(io_pipeline_memread, _T_10, _T_readdata) @[MemCombinPort.scala 97:39]
    node _T_8 = io_pipeline_writedata @[MemCombinPort.scala 72:28 MemCombinPort.scala 95:17]
    node _GEN_5 = validif(io_pipeline_memwrite, _T_8) @[MemCombinPort.scala 70:33]
    node _GEN_6 = mux(io_pipeline_memwrite, _T_readdata, _GEN_4) @[MemCombinPort.scala 70:33]
    node _GEN_7 = validif(io_bus_response_valid, _GEN_5) @[MemCombinPort.scala 69:32]
    node _GEN_8 = mux(io_bus_response_valid, _GEN_6, _T_readdata) @[MemCombinPort.scala 69:32]
    node _T_address = io_pipeline_address @[BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:15]
    node _T_valid = io_pipeline_valid @[BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:15]
    node _T_good = UInt<1>("h0") @[BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:31]
    node _T_writedata = io_pipeline_writedata @[BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:15]
    node _T_memread = io_pipeline_memread @[BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:15]
    node _T_memwrite = io_pipeline_memwrite @[BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:31 BaseMemComponents.scala 71:15]
    io_pipeline_good <= UInt<1>("h1") @[BaseMemComponents.scala 71:15 BaseMemComponents.scala 78:20 MemCombinPort.scala 40:20]
    io_pipeline_readdata <= _GEN_8 @[BaseMemComponents.scala 71:15 MemCombinPort.scala 128:28]
    io_bus_request_valid <= _GEN_2 @[MemCombinPort.scala 47:26 MemCombinPort.scala 65:26]
    io_bus_request_bits_address <= _GEN_1 @[MemCombinPort.scala 46:33]
    io_bus_request_bits_writedata <= _GEN_7 @[MemCombinPort.scala 96:37]
    io_bus_request_bits_operation <= _GEN_3 @[MemCombinPort.scala 58:37 MemCombinPort.scala 61:37]
    printf(clock, and(and(and(UInt<1>("h1"), _T_2), _T_7), UInt<1>("h1")), "Assertion failed\n    at MemCombinPort.scala:44 assert(!(io.pipeline.memread && io.pipeline.memwrite))\n") @[MemCombinPort.scala 44:11]
    stop(clock, and(and(and(UInt<1>("h1"), _T_2), _T_7), UInt<1>("h1")), 1) @[MemCombinPort.scala 44:11]

  module Top :
    input clock : Clock
    input reset : UInt<1>
    output io_success : UInt<1>
  
    inst cpu of SingleCycleCPU @[Top.scala 10:19]
    inst mem of DualPortedCombinMemory @[Top.scala 11:19]
    inst imem of ICombinMemPort @[Top.scala 12:20]
    inst dmem of DCombinMemPort @[Top.scala 13:20]
    io_success is invalid
    cpu.clock <= clock
    cpu.reset <= reset
    cpu.io_imem_good <= imem.io_pipeline_good @[Top.scala 15:15]
    cpu.io_imem_instruction <= imem.io_pipeline_instruction @[Top.scala 15:15]
    cpu.io_dmem_good <= dmem.io_pipeline_good @[Top.scala 16:15]
    cpu.io_dmem_readdata <= dmem.io_pipeline_readdata @[Top.scala 16:15]
    mem.clock <= clock
    mem.reset <= reset
    mem.io_imem_request_valid <= imem.io_bus_request_valid @[BaseMemComponents.scala 22:26]
    mem.io_imem_request_bits_address <= imem.io_bus_request_bits_address @[BaseMemComponents.scala 22:26]
    mem.io_imem_request_bits_writedata <= imem.io_bus_request_bits_writedata @[BaseMemComponents.scala 22:26]
    mem.io_imem_request_bits_operation <= imem.io_bus_request_bits_operation @[BaseMemComponents.scala 22:26]
    mem.io_dmem_request_valid <= dmem.io_bus_request_valid @[BaseMemComponents.scala 24:26]
    mem.io_dmem_request_bits_address <= dmem.io_bus_request_bits_address @[BaseMemComponents.scala 24:26]
    mem.io_dmem_request_bits_writedata <= dmem.io_bus_request_bits_writedata @[BaseMemComponents.scala 24:26]
    mem.io_dmem_request_bits_operation <= dmem.io_bus_request_bits_operation @[BaseMemComponents.scala 24:26]
    imem.clock <= clock
    imem.reset <= reset
    imem.io_pipeline_address <= cpu.io_imem_address @[Top.scala 15:15]
    imem.io_pipeline_valid <= cpu.io_imem_valid @[Top.scala 15:15]
    imem.io_bus_request_ready <= mem.io_imem_request_ready @[BaseMemComponents.scala 22:26]
    imem.io_bus_response_valid <= mem.io_imem_response_valid @[BaseMemComponents.scala 23:26]
    imem.io_bus_response_bits_data <= mem.io_imem_response_bits_data @[BaseMemComponents.scala 23:26]
    dmem.clock <= clock
    dmem.reset <= reset
    dmem.io_pipeline_address <= cpu.io_dmem_address @[Top.scala 16:15]
    dmem.io_pipeline_valid <= cpu.io_dmem_valid @[Top.scala 16:15]
    dmem.io_pipeline_writedata <= cpu.io_dmem_writedata @[Top.scala 16:15]
    dmem.io_pipeline_memread <= cpu.io_dmem_memread @[Top.scala 16:15]
    dmem.io_pipeline_memwrite <= cpu.io_dmem_memwrite @[Top.scala 16:15]
    dmem.io_bus_request_ready <= mem.io_dmem_request_ready @[BaseMemComponents.scala 24:26]
    dmem.io_bus_response_valid <= mem.io_dmem_response_valid @[BaseMemComponents.scala 25:26]
    dmem.io_bus_response_bits_data <= mem.io_dmem_response_bits_data @[BaseMemComponents.scala 25:26]
