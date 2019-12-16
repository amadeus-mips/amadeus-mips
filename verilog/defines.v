/**********全局***********/
`define RstEnable 		1'b1
`define RstDisable 		1'b0
`define ZeroWord 		32'h00000000 //字置为0
`define WriteEnable 	1'b1
`define WriteDisable 	1'b0
`define ReadEnable 		1'b1
`define ReadDisable 	1'b0
`define AluOpBus 		7:0  		//自定义8位AluOp
`define AluSelBus 		2:0   		//自定义3位选择类别
`define InstValid 		1'b0  		//指令存有效
`define InstInvalid		1'b1        //指令存无效
`define Stop 			1'b1
`define NoStop 			1'b0  
`define InDelaySlot 	1'b1 		//延迟漕指令
`define NotInDelaySlot 	1'b0        //非延迟漕指令
`define Branch 			1'b1  		//分支跳转
`define NotBranch 		1'b0 
`define InterruptAssert 		1'b1 //中断可达
`define InterruptNotAssert 		1'b0 //中断不可达
`define TrapAssert 				1'b1 //自陷可达
`define TrapNotAssert 			1'b0 //自陷不可达
`define True_v 					1'b1 
`define False_v 				1'b0
`define ChipEnable 				1'b1
`define ChipDisable 			1'b0

//**********指令存储器inst_rom***********
`define InstAddrBus 31:0
`define InstBus 31:0
`define InstMemNum 131071
`define InstMemNumLog2 17

//**********数据存储器data_ram***********
`define DataAddrBus 31:0
`define DataBus 31:0
`define DataMemNum 131071
`define DataMemNumLog2 17
`define ByteWidth 7:0

//************EX模块*********
//逻辑运算op
`define EXE_OR_OP  8'd0
`define EXE_XOR_OP 8'd1
`define EXE_AND_OP 8'd2
`define EXE_NOR_OP 8'd3
//逻辑运算op
`define EXE_SLL_OP 8'd4
`define EXE_SRL_OP 8'd5
`define EXE_SRA_OP 8'd6
//数据移动op
`define EXE_MFHI_OP 8'd7
`define EXE_MFLO_OP 8'd9
`define EXE_MTHI_OP 8'd10
`define EXE_MTLO_OP 8'd11

`define EXE_LB_OP  8'd24
`define EXE_LBU_OP  8'd25
`define EXE_LH_OP  8'd26
`define EXE_LHU_OP  8'd27
`define EXE_LW_OP   8'd28
`define EXE_SB_OP   8'd29
`define EXE_SH_OP   8'd30
`define EXE_SW_OP   8'd31
//算数运算op
`define EXE_ADDU_OP 8'd12
`define EXE_SUB_OP 8'd13
`define EXE_ADD_OP 8'd14
`define EXE_SUBU_OP 8'd15
`define EXE_DIV_OP 8'd16
`define EXE_DIVU_OP 8'd17
`define EXE_MULT_OP 8'd18
`define EXE_MULTU_OP 8'd19
`define EXE_SLT_OP 8'd20
`define EXE_SLTU_OP 8'd21
//特权指令op
`define EXE_MFC0_OP 8'd22
`define EXE_MTC0_OP 8'd23
//访存指令op
`define EXE_LB_OP  8'd24
`define EXE_LBU_OP  8'd25
`define EXE_LH_OP  8'd26
`define EXE_LHU_OP  8'd27
`define EXE_LW_OP   8'd28
`define EXE_SB_OP   8'd29
`define EXE_SH_OP   8'd30
`define EXE_SW_OP   8'd31

///**************temp****************
//跳转指令op
`define EXE_JR_OP       8'd100
`define EXE_JALR_OP     8'd103
`define EXE_J_OP        8'd104
`define EXE_JAL_OP      8'd105
`define EXE_BEQ_OP      8'd106
`define EXE_BGTZ_OP     8'd107
`define EXE_BLEZ_OP     8'd108
`define EXE_BGEZ_OP     8'd109
`define EXE_BGEZAL_OP   8'd110

`define EXE_SYSCALL_OP  8'd101
`define EXE_BREAK_OP    8'd102
//空指令
`define EXE_NOP_OP    8'b00000000
//自陷指令
`define EXE_ERET_OP   8'd200

//结果选择信号
`define EXE_RES_LOGIC 3'd0
`define EXE_RES_SHIFT 3'd1
`define EXE_RES_ARITH 3'd2
`define EXE_RES_MOVE  3'd3
`define EXE_RES_JUMP_BRANCH 3'd4
`define EXE_RES_LOAD_STORE 3'b111	

`define EXE_RES_NOP 3'b000

//**********通用寄存器regfile**********
`define RegAddrBus 		4:0       //寄存器地址编号
`define RegBus 			31:0      //寄存器位宽
`define RegWidth 		32	      //寄存器位宽个数
`define DoubleRegWidth 	64		  
`define DoubleRegBus 	63:0	  //64位地址
`define RegNum 			32		  //Regfile中寄存器的个数
`define RegNumLog2 		5
`define NOPRegAddr 		5'b00000  //RST时，写入寄存器的地址编号

//**********ID模块指令**********
//逻辑运算指令
`define EXE_AND  	6'b100100
`define EXE_OR   	6'b100101
`define EXE_XOR 	6'b100110
`define EXE_NOR 	6'b100111
`define EXE_ANDI 	6'b001100
`define EXE_ORI  	6'b001101
`define EXE_XORI 	6'b001110
`define EXE_LUI 	6'b001111
//位移运算
`define EXE_SLL  	6'b000000
`define EXE_SLLV  	6'b000100
`define EXE_SRL  	6'b000010
`define EXE_SRLV  	6'b000110
`define EXE_SRA  	6'b000011
`define EXE_SRAV  	6'b000111
//位移运算---HI,LO位移指令
`define EXE_MFHI  	6'b010000
`define EXE_MTHI  	6'b010001
`define EXE_MFLO  	6'b010010
`define EXE_MTLO  	6'b010011
//算术运算指令
//算术运算指令---加减
`define EXE_ADD  	6'b100000
`define EXE_ADDU  	6'b100001
`define EXE_ADDI  	6'b001000
`define EXE_ADDIU  	6'b001001
`define EXE_SLT  	6'b101010
`define EXE_SLTU  	6'b101011
`define EXE_SLTI  	6'b001010
`define EXE_SLTIU  	6'b001011   
`define EXE_SUB  	6'b100010
`define EXE_SUBU  	6'b100011
//算术运算指令---除法
`define EXE_DIV  	6'b011010
`define EXE_DIVU  	6'b011011
//算术运算指令---乘法
`define EXE_MULT  	6'b011000
`define EXE_MULTU  	6'b011001
//跳转指令
`define EXE_J  	  	6'b000010
`define EXE_JAL   	6'b000011
`define EXE_JALR  	6'b001001
`define EXE_JR    	6'b001000
`define EXE_BEQ   	6'b000100
`define EXE_BGEZ  	5'b00001
`define EXE_BGEZAL  5'b10001
`define EXE_BGTZ  	6'b000111
`define EXE_BLEZ  	6'b000110
`define EXE_BLTZ  	5'b00000
`define EXE_BLTZAL  5'b10000
`define EXE_BNE  	6'b000101
//访存指令 
`define EXE_LB  	6'b100000
`define EXE_LBU  	6'b100100
`define EXE_LH  	6'b100001
`define EXE_LHU 	6'b100101
`define EXE_LW  	6'b100011
`define EXE_SB  	6'b101000
`define EXE_SW  	6'b101011
`define EXE_SH  	6'b101001
//自陷指令 
`define EXE_BREAK    6'b001101
`define EXE_SYSCALL  6'b001100
//特权指令
`define EXE_ERET 	32'b01000010000000000000000000011000
//自定义空指令和空地址
`define EXE_NOP     6'b000000
`define SSNOP       32'b00000000000000000000000001000000
//自定义三种特别指令类别
`define EXE_SPECIAL_INST 6'b000000
`define EXE_REGIMM_INST 6'b000001
`define EXE_SPECIAL2_INST 6'b011100
`define EXE_PREF  6'b110011


// 用于axi总线
`define AXI_INST_Id     4'b0000
`define AXI_DATA_Id     4'b0001

`define R_IDLE          3'd0
`define AR_WAIT         3'd1
`define AR_FINISH       3'd2
`define R_WAIT          3'd3
`define R_FINISH        3'd4

`define W_IDLE          3'd0
`define AW_WAIT         3'd1
`define AW_FINISH       3'd2
`define W_WAIT          3'd3
`define W_FINISH        3'd4
`define B_WAIT          3'd5