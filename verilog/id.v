/* 
* @ModuleName: id
* @Description: "ID模块对指令进行译码，得到最终运算的类型、子类型、源操作数1、
* 源操作数2、要写入的目的寄存器地址等信息"
* @Author: "limingsong"
* @CreateDate: 2019-07-07
* @LastModifiedTime: 2019-07-16
*/

/*
  实现的功能：
  1. EX MEM 阶段的数据前推
  2. EX阶段数据前推,解决Load相关问题
  3. 流水线暂时
  4. ID阶段异常类型判断（Syscall ERET Break)
  5. 协处理器指令
 */
`include "defines.v"

module id(

	input wire					  rst,
	input wire[31:0]	          pc_i,  //来自if/id阶段的pc
	input wire[31:0]              inst_i,//来自if/id阶段的指令

  	//处于执行阶段的指令的一些信息，用于解决load相关
  	input wire[7:0]		    	  ex_aluop_i,

	//处于执行阶段的指令要写入的目的寄存器信息
	input wire					  ex_wreg_i,//数据前推，来自EX阶段，是否写寄存器
	input wire[31:0]			  ex_wdata_i,//数据前推，来自EX阶段，写入的数据
	input wire[4:0]       		  ex_wd_i,//数据前推，来自EX阶段，写入的寄存器的编号
	
	//处于访存阶段的指令要写入的目的寄存器信息
	input wire					  mem_wreg_i,//数据前推，来自MEM阶段，是否写寄存器
	input wire[31:0]			  mem_wdata_i,//数据前推，来自MEM阶段，写入的数据
	input wire[4:0]       		  mem_wd_i,//数据前推，来自MEM阶段，写入的寄存器的编号
	
	input wire[31:0]              reg1_data_i,//来自Regfile模块的端口1数据
	input wire[31:0]              reg2_data_i,//来自Regfile模块的端口2数据

	//如果上一条指令是转移指令，那么下一条指令在译码的时候is_in_delayslot为true
	input wire                    is_in_delayslot_i,

	input wire			  		  if_excepttype_i,//IF阶段的异常类型

	//送到regfile的信息
	output reg                    reg1_read_o,//输入到refile寄存器端口1的读使能信号
	output reg                    reg2_read_o,//输入到refile寄存器端口2的读使能信号     
	output reg[4:0]       		  reg1_addr_o,//输入到refile寄存器端口1的寄存器编号
	output reg[4:0]               reg2_addr_o,//输入到refile寄存器端口2的寄存器编号 	      
	
	//送到执行阶段的信息
	output reg[7:0]         	  aluop_o,//输出到EX阶段的指令子类型
	output reg[2:0]        		  alusel_o,//输出到EX阶段的指令类型
	output reg[31:0]              reg1_o,//输出到EX阶段的要进行运算的源操作数1
	output reg[31:0]              reg2_o,//输出到EX阶段的要进行运算的源操作数2
	output reg[4:0]       		  wd_o,//译码阶段的对应的指令要写入的目的寄存器的编号
	output reg                    wreg_o,//译码阶段的对应的指令要是否写目的寄存器
	output wire[31:0]          	  inst_o,//ID阶段输出的指令

	output reg                    next_inst_in_delayslot_o,//如果当前指令为跳转/分支指令，则置为1，否则为0

	//分支控制相关语句
	output reg                    branch_flag_o,//是否为分支指令
	output reg[31:0]              branch_target_address_o,//分支指令跳转地址       
	output reg[31:0]              link_addr_o,//跳转指令的返回地址，一直输入到MEM阶段进行处理
	output wire                   is_in_delayslot_o,//指示当前指令是否为延迟槽指令

	//流水线暂停信号
	output wire                   stallreq,//stall信号

	//异常处理信号
	output wire[31:0]			  excepttype_o,//指示当前ID阶段是否发生异常
	output wire[31:0]		  	  current_inst_address_o//当前指令的地址
);

  	wire[5:0] op = inst_i[31:26];
	wire[4:0] sa = inst_i[10:6];
	wire[5:0] func = inst_i[5:0];
	wire[4:0] rt = inst_i[20:16];
	reg[31:0]	imm;
	reg inst_invalid;
	wire[31:0] pc_plus_8;
	wire[31:0] pc_plus_4;
	wire[31:0] imm_sll2_signedext;  

	wire stallreq_for_reg1_load_use;
	wire stallreq_for_reg2_load_use;
	wire pre_inst_is_load;

	reg excepttype_is_syscall; //是否是系统调用异常syscall
	reg excepttype_is_eret;	 //是否是返回异常eret
	reg excepttype_is_break;   //是否是break
	
	assign pc_plus_8 = pc_i + 8;
	assign pc_plus_4 = pc_i +4;
	assign imm_sll2_signedext = {{14{inst_i[15]}}, inst_i[15:0], 2'b00 };  
	assign stallreq = stallreq_for_reg1_load_use | stallreq_for_reg2_load_use;

  	assign pre_inst_is_load = ((ex_aluop_i == `EXE_LB_OP) || 
							(ex_aluop_i == `EXE_LBU_OP)||
							(ex_aluop_i == `EXE_LH_OP) ||
							(ex_aluop_i == `EXE_LHU_OP)||
							(ex_aluop_i == `EXE_LW_OP) ) ? 1'b1 : 1'b0;

 	 assign inst_o = inst_i;
  
  //exceptiontype的低8bit留给外部中断，第9bit表示是否是syscall指令
  //第10bit表示是否是无效指令，第11bit表示是否是break指令
  //*****************beark********
  //need to  be discussed
  	assign excepttype_o = {18'b0,if_excepttype_i,excepttype_is_eret,1'b0, inst_invalid,
  							excepttype_is_break, excepttype_is_syscall,8'b0};
  //赋值当前pc											
  	assign current_inst_address_o = pc_i;

//跳转有关变量
	always @(*) begin
		link_addr_o <= `ZeroWord;
		branch_flag_o <= 1'b1;
		branch_target_address_o <= `ZeroWord;
		next_inst_in_delayslot_o <= 1'b1;
		case (op)	//inst[31:26]
			`EXE_SPECIAL_INST: begin
				if(sa == 5'b00000) begin // inst[10:6]
					case(func)
						`EXE_JR: begin
							link_addr_o <= `ZeroWord;
							branch_target_address_o <= reg1_o;
							branch_flag_o <= 1'b1;
							next_inst_in_delayslot_o <= 1'b1;
						end 
						`EXE_JALR: begin
							link_addr_o <= pc_plus_8;
							branch_target_address_o <= reg1_o;
							branch_flag_o <= 1'b1;
							next_inst_in_delayslot_o <= 1'b1;	
						end
						default: begin
							link_addr_o <= `ZeroWord;
							branch_target_address_o <= 32'd0;
							branch_flag_o <= 1'b0;
							next_inst_in_delayslot_o <= 1'b0;
						end
					endcase
				end else begin
					link_addr_o <= `ZeroWord;
					branch_target_address_o <= 32'd0;
					branch_flag_o <= 1'b0;
					next_inst_in_delayslot_o <= 1'b0;
				end
			end
			`EXE_J: begin
				link_addr_o <= `ZeroWord;
				branch_target_address_o <= {pc_plus_4[31:28], inst_i[25:0], 2'b00};
				branch_flag_o <= 1'b1;
				next_inst_in_delayslot_o <= 1'b1;
			end
			`EXE_JAL: begin
				link_addr_o <= pc_plus_8;
				branch_target_address_o <= {pc_plus_4[31:28], inst_i[25:0], 2'b00};
				branch_flag_o <= 1'b1;
				next_inst_in_delayslot_o <= 1'b1;
			end
			`EXE_BEQ: begin
				link_addr_o <= `ZeroWord;				
				branch_target_address_o <= pc_plus_4 + imm_sll2_signedext;
				branch_flag_o <= (reg1_o == reg2_o);
				next_inst_in_delayslot_o <= 1'b1;
			end
			`EXE_BGTZ: begin
				link_addr_o <= `ZeroWord;
				branch_target_address_o <= pc_plus_4 + imm_sll2_signedext;
				branch_flag_o <= (~reg1_o[31] & (|reg1_o));
				next_inst_in_delayslot_o <= 1'b1;
			end
			`EXE_BLEZ: begin
				link_addr_o <= `ZeroWord;
				branch_target_address_o <= pc_plus_4 + imm_sll2_signedext;
				branch_flag_o <= ~(~reg1_o[31] & (|reg1_o));
				next_inst_in_delayslot_o <= 1'b1;
			end
			`EXE_BNE: begin
				link_addr_o <= `ZeroWord;
				branch_target_address_o <= pc_plus_4 + imm_sll2_signedext;
				branch_flag_o <= ~(reg1_o == reg2_o);
				next_inst_in_delayslot_o <= 1'b1;
			end
			`EXE_REGIMM_INST: begin
				case(rt)   //  inst[20:16]
					`EXE_BGEZ: begin
						link_addr_o <= `ZeroWord;
						branch_target_address_o <= pc_plus_4 + imm_sll2_signedext;
						branch_flag_o <= ~reg1_o[31];
						next_inst_in_delayslot_o <= 1'b1;
					end
					`EXE_BGEZAL: begin
						link_addr_o <= pc_plus_8;
						branch_target_address_o <= pc_plus_4 + imm_sll2_signedext;
						branch_flag_o <= ~reg1_o[31];
						next_inst_in_delayslot_o <= 1'b1;
					end
					`EXE_BLTZ: begin
						link_addr_o <= `ZeroWord;
						branch_target_address_o <= pc_plus_4 + imm_sll2_signedext;
						branch_flag_o <= reg1_o[31];
						next_inst_in_delayslot_o <= 1'b1;			
					end
					`EXE_BLTZAL: begin
						link_addr_o <= pc_plus_8;
						branch_target_address_o <= pc_plus_4 + imm_sll2_signedext;
						branch_flag_o <= reg1_o[31];
						next_inst_in_delayslot_o <= 1'b1;
					end
				endcase
			end
			default: begin
				link_addr_o <= `ZeroWord;
				branch_target_address_o <= 32'd0;
				branch_flag_o <= 1'b0;
				next_inst_in_delayslot_o <= 1'b0;				
			end
		endcase
	end

	always @ (*) begin	
		if (rst == 1'b1) begin
			aluop_o <= `EXE_NOP_OP;
			alusel_o <= `EXE_RES_NOP;
			wd_o <= `NOPRegAddr;
			wreg_o <= 1'b0;
			inst_invalid <= 1'b0;
			reg1_read_o <= 1'b0;
			reg2_read_o <= 1'b0;
			reg1_addr_o <= `NOPRegAddr;
			reg2_addr_o <= `NOPRegAddr;
			imm <= 32'h0;	
			excepttype_is_syscall <= 1'b0; //无系统调用
			excepttype_is_eret <= 1'b0;  //无返回指令异常
			excepttype_is_break <= 1'b0;  //无break指令		
	  end else begin
			aluop_o <= `EXE_NOP_OP;
			alusel_o <= `EXE_RES_NOP;
			wd_o <= inst_i[15:11];
			wreg_o <= 1'b0;
			inst_invalid <= 1'b1;	   
			reg1_read_o <= 1'b0;
			reg2_read_o <= 1'b0;
			reg1_addr_o <= inst_i[25:21]; //默认取rs
			reg2_addr_o <= inst_i[20:16]; //默认取rt		
			imm <= `ZeroWord;
			excepttype_is_syscall <= 1'b0;	//默认无系统调用
			excepttype_is_eret <= 1'b0;	 //默认不是返回指令
			excepttype_is_break <= 1'b0;  //默认无break异常指令
			case (op)	// inst[31:26]
				`EXE_SPECIAL_INST: begin
					case (sa)	// inst[10:6]
						5'b00000: begin
							case (func)	// inst[5:0]
								`EXE_OR: begin
									aluop_o <= `EXE_OR_OP; 	alusel_o <= `EXE_RES_LOGIC; 
									reg1_read_o <= 1'b1;   	reg2_read_o <= 1'b1;
									wreg_o <= 1'b1; inst_invalid <= 1'b0;	
								end  
								`EXE_AND: begin
									aluop_o <= `EXE_AND_OP; alusel_o <= `EXE_RES_LOGIC;	
									reg1_read_o <= 1'b1;	reg2_read_o <= 1'b1;	
									wreg_o <= 1'b1; inst_invalid <= 1'b0;	
								end  	
								`EXE_XOR: begin
									aluop_o <= `EXE_XOR_OP; alusel_o <= `EXE_RES_LOGIC;		
									reg1_read_o <= 1'b1;	reg2_read_o <= 1'b1;	
									wreg_o <= 1'b1;	inst_invalid <= 1'b0;	
								end  				
								`EXE_NOR: begin
									aluop_o <= `EXE_NOR_OP; alusel_o <= `EXE_RES_LOGIC;
									reg1_read_o <= 1'b1;	reg2_read_o <= 1'b1;	
									wreg_o <= 1'b1; inst_invalid <= 1'b0;	
								end

								//位移运算指令						
								`EXE_SLLV: begin 
									aluop_o <= `EXE_SLL_OP; alusel_o <= `EXE_RES_SHIFT;
									reg1_read_o <= 1'b1;	reg2_read_o <= 1'b1;
									wreg_o <= 1'b1;	inst_invalid <= 1'b0;	
								end 
								`EXE_SRLV: begin
									aluop_o <= `EXE_SRL_OP; alusel_o <= `EXE_RES_SHIFT;	
									reg1_read_o <= 1'b1;	reg2_read_o <= 1'b1;
									wreg_o <= 1'b1;	inst_invalid <= 1'b0;	
								end 					
								`EXE_SRAV: begin
									aluop_o <= `EXE_SRA_OP; alusel_o <= `EXE_RES_SHIFT;		
									reg1_read_o <= 1'b1;	reg2_read_o <= 1'b1;
									wreg_o <= 1'b1; inst_invalid <= 1'b0;			
								end

								//HI、LO位移指令	  					
								`EXE_MFHI: begin 
									aluop_o <= `EXE_MFHI_OP; alusel_o <= `EXE_RES_MOVE;
									reg1_read_o <= 1'b0;	 reg2_read_o <= 1'b0;
									wreg_o <= 1'b1;  inst_invalid <= 1'b0;	
								end
								`EXE_MFLO: begin
									aluop_o <= `EXE_MFLO_OP; alusel_o <= `EXE_RES_MOVE;
									reg1_read_o <= 1'b0;	 reg2_read_o <= 1'b0;
									wreg_o <= 1'b1;  inst_invalid <= 1'b0;	
								end
								`EXE_MTHI: begin
									aluop_o <= `EXE_MTHI_OP;
									reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b0; 
									wreg_o <= 1'b0; inst_invalid <= 1'b0;	
								end
								`EXE_MTLO: begin
									aluop_o <= `EXE_MTLO_OP;
									reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b0; 
									wreg_o <= 1'b0; inst_invalid <= 1'b0;	
								end

								//算术运算								
								`EXE_SLT: begin 
									aluop_o <= `EXE_SLT_OP; alusel_o <= `EXE_RES_ARITH;				  						
									reg1_read_o <= 1'b1;	reg2_read_o <= 1'b1;
									wreg_o <= 1'b1; inst_invalid <= 1'b0;	
								end
								`EXE_SLTU: begin
									aluop_o <= `EXE_SLTU_OP; alusel_o <= `EXE_RES_ARITH;
									reg1_read_o <= 1'b1;	reg2_read_o <= 1'b1;
									wreg_o <= 1'b1; inst_invalid <= 1'b0;	
								end							
								`EXE_ADD: begin
									aluop_o <= `EXE_ADD_OP; alusel_o <= `EXE_RES_ARITH;
									reg1_read_o <= 1'b1;	reg2_read_o <= 1'b1;
									wreg_o <= 1'b1; inst_invalid <= 1'b0;	
								end
								`EXE_ADDU: begin
									aluop_o <= `EXE_ADDU_OP; alusel_o <= `EXE_RES_ARITH;
									reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b1;
									wreg_o <= 1'b1;  inst_invalid <= 1'b0;	
								end
								`EXE_SUB: begin
									aluop_o <= `EXE_SUB_OP; alusel_o <= `EXE_RES_ARITH;
									reg1_read_o <= 1'b1;	reg2_read_o <= 1'b1;
									wreg_o <= 1'b1; inst_invalid <= 1'b0;	
								end
								`EXE_SUBU: begin
									aluop_o <= `EXE_SUBU_OP; alusel_o <= `EXE_RES_ARITH;
									reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b1;
									wreg_o <= 1'b1;  inst_invalid <= 1'b0;	
								end							
								`EXE_MULT: begin 	//有符号乘法
									aluop_o <= `EXE_MULT_OP;
									reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b1; 
									wreg_o <= 1'b0; inst_invalid <= 1'b0;	
								end							
								`EXE_MULTU: begin 	//无符号乘法
									aluop_o <= `EXE_MULTU_OP;
									reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b1; 
									wreg_o <= 1'b0; inst_invalid <= 1'b0;	
								end
								`EXE_DIV: begin
									aluop_o <= `EXE_DIV_OP;
									reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b1; 
									wreg_o <= 1'b0; inst_invalid <= 1'b0;	
								end
								`EXE_DIVU: begin
									aluop_o <= `EXE_DIVU_OP;
									reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b1; 
									wreg_o <= 1'b0; inst_invalid <= 1'b0;	
								end			
								`EXE_JR: begin
									aluop_o <= `EXE_JR_OP;   alusel_o <= `EXE_RES_JUMP_BRANCH;  
									reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b0;
									wreg_o <= 1'b0; inst_invalid <= 1'b0;		            			
								end
								`EXE_JALR: begin
									aluop_o <= `EXE_JALR_OP; alusel_o <= `EXE_RES_JUMP_BRANCH;  
									reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b0;
									wreg_o <= 1'b1;	 inst_invalid <= 1'b0;
								end								
								`EXE_SYSCALL: begin //系统调用
									aluop_o <= `EXE_SYSCALL_OP; alusel_o <= `EXE_RES_NOP; 
									reg1_read_o <= 1'b0;		reg2_read_o <= 1'b0;
									wreg_o <= 1'b0; 	inst_invalid <= 1'b0; 	
									excepttype_is_syscall<= 1'b1;
								end			  					
								`EXE_BREAK: begin	//Break 指令									
									aluop_o <= `EXE_BREAK_OP; alusel_o <= `EXE_RES_NOP;
									reg1_read_o <= 1'b0;	  reg2_read_o <= 1'b0;
									wreg_o <= 1'b0;  inst_invalid <= 1'b0; 
									excepttype_is_break<= 1'b1;
								end													 											  											
								default: begin
								end
							endcase
						end
						default: begin
						end
					endcase	
				end									  
				`EXE_ORI: begin   //ORI指令
					aluop_o <= `EXE_OR_OP;  alusel_o <= `EXE_RES_LOGIC;
					reg1_read_o <= 1'b1;	reg2_read_o <= 1'b0;	
					wreg_o <= 1'b1; inst_invalid <= 1'b0;				    							
					wd_o <= inst_i[20:16];  imm <= {16'h0, inst_i[15:0]};					
				end
				`EXE_ANDI: begin
					aluop_o <= `EXE_AND_OP; alusel_o <= `EXE_RES_LOGIC;	
					reg1_read_o <= 1'b1;	reg2_read_o <= 1'b0;	  							  	
					wreg_o <= 1'b1; inst_invalid <= 1'b0;
					wd_o <= inst_i[20:16];	imm <= {16'h0, inst_i[15:0]};
				end	 	
				`EXE_XORI: begin
					aluop_o <= `EXE_XOR_OP; alusel_o <= `EXE_RES_LOGIC;	
					reg1_read_o <= 1'b1;	reg2_read_o <= 1'b0;	  	
					wreg_o <= 1'b1;	inst_invalid <= 1'b0;	
					wd_o <= inst_i[20:16];	imm <= {16'h0, inst_i[15:0]};			  					
				end	 		
				`EXE_LUI: begin
					aluop_o <= `EXE_OR_OP;  alusel_o <= `EXE_RES_LOGIC;
					reg1_read_o <= 1'b1;	reg2_read_o <= 1'b0;	  	
					wreg_o <= 1'b1;	inst_invalid <= 1'b0;	
					wd_o <= inst_i[20:16];	imm <= {inst_i[15:0], 16'h0};			  					
				end			
				`EXE_SLTI: begin
					aluop_o <= `EXE_SLT_OP; alusel_o <= `EXE_RES_ARITH;
					reg1_read_o <= 1'b1;	reg2_read_o <= 1'b0;	  	
					wreg_o <= 1'b1;	inst_invalid <= 1'b0;	
					wd_o <= inst_i[20:16];	imm <= {{16{inst_i[15]}}, inst_i[15:0]};			  					
				end
				`EXE_SLTIU: begin
					aluop_o <= `EXE_SLTU_OP; alusel_o <= `EXE_RES_ARITH;
					reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b0;	  	
					wreg_o <= 1'b1;	 inst_invalid <= 1'b0;	
					wd_o <= inst_i[20:16];	 imm <= {{16{inst_i[15]}}, inst_i[15:0]};			  					
				end					
				`EXE_ADDI: begin
					aluop_o <= `EXE_ADD_OP; alusel_o <= `EXE_RES_ARITH;
					reg1_read_o <= 1'b1;	reg2_read_o <= 1'b0;	  	
					wreg_o <= 1'b1;	inst_invalid <= 1'b0;	
					wd_o <= inst_i[20:16];	imm <= {{16{inst_i[15]}}, inst_i[15:0]};			  					
				end
				`EXE_ADDIU: begin
					aluop_o <= `EXE_ADDU_OP; alusel_o <= `EXE_RES_ARITH;
					reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b0;	  	
					wreg_o <= 1'b1;	 inst_invalid <= 1'b0;	
					wd_o <= inst_i[20:16];	 imm <= {{16{inst_i[15]}}, inst_i[15:0]};			  					
				end
				`EXE_J:	begin
					aluop_o <= `EXE_J_OP;  	 alusel_o <= `EXE_RES_JUMP_BRANCH;
					reg1_read_o <= 1'b0;	 reg2_read_o <= 1'b0;
					wreg_o <= 1'b0; inst_invalid <= 1'b0;								    
				end
				`EXE_JAL: begin
					aluop_o <= `EXE_JAL_OP; alusel_o <= `EXE_RES_JUMP_BRANCH;
					reg1_read_o <= 1'b0;	reg2_read_o <= 1'b0;
					wreg_o <= 1'b1; inst_invalid <= 1'b0;
					wd_o <= 5'b11111;				    	
				end
				`EXE_BEQ: begin
					aluop_o <= `EXE_BEQ_OP;  alusel_o <= `EXE_RES_JUMP_BRANCH;
					reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b1;
					wreg_o <= 1'b0; inst_invalid <= 1'b0;
				end
				`EXE_BGTZ: begin
					aluop_o <= `EXE_BGTZ_OP; alusel_o <= `EXE_RES_JUMP_BRANCH;
					reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b0;
					wreg_o <= 1'b0; inst_invalid <= 1'b0;	
				end
				`EXE_BLEZ: begin
					aluop_o <= `EXE_BLEZ_OP; alusel_o <= `EXE_RES_JUMP_BRANCH; 
					reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b0;
					wreg_o <= 1'b0; inst_invalid <= 1'b0;
				end
				`EXE_BNE: begin
					aluop_o <= `EXE_BLEZ_OP; alusel_o <= `EXE_RES_JUMP_BRANCH; 
					reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b1;
					wreg_o <= 1'b0; inst_invalid <= 1'b0;	
				end
				`EXE_LB: begin						
					aluop_o <= `EXE_LB_OP;  alusel_o <= `EXE_RES_LOAD_STORE;					
					reg1_read_o <= 1'b1;    reg2_read_o <= 1'b0;					
					wreg_o <= 1'b1; inst_invalid <= 1'b0;		  						
					wd_o <= inst_i[20:16]; 
				end
				`EXE_LBU: begin
					aluop_o <= `EXE_LBU_OP; alusel_o <= `EXE_RES_LOAD_STORE; 
					reg1_read_o <= 1'b1;	reg2_read_o <= 1'b0;	  	
					wreg_o <= 1'b1; inst_invalid <= 1'b0;	
					wd_o <= inst_i[20:16];
				end
				`EXE_LH: begin
					aluop_o <= `EXE_LH_OP;  alusel_o <= `EXE_RES_LOAD_STORE;
					reg1_read_o <= 1'b1;	reg2_read_o <= 1'b0;	  	
					wreg_o <= 1'b1; inst_invalid <= 1'b0;	
					wd_o <= inst_i[20:16]; 
				end
				`EXE_LHU: begin
					aluop_o <= `EXE_LHU_OP; alusel_o <= `EXE_RES_LOAD_STORE;
					reg1_read_o <= 1'b1;	reg2_read_o <= 1'b0;	  	
					wreg_o <= 1'b1; inst_invalid <= 1'b0;
					wd_o <= inst_i[20:16]; 	
				end
				`EXE_LW: begin
					aluop_o <= `EXE_LW_OP;  alusel_o <= `EXE_RES_LOAD_STORE;
					reg1_read_o <= 1'b1;	reg2_read_o <= 1'b0;	  	
					wreg_o <= 1'b1; inst_invalid <= 1'b0;	
					wd_o <= inst_i[20:16]; 
				end					
				`EXE_SB: begin
					aluop_o <= `EXE_SB_OP;   alusel_o <= `EXE_RES_LOAD_STORE;
					reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b1;
					wreg_o <= 1'b0; inst_invalid <= 1'b0;					 
				end
				`EXE_SH: begin
					aluop_o <= `EXE_SH_OP;   alusel_o <= `EXE_RES_LOAD_STORE; 
					reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b1; 
					wreg_o <= 1'b0; inst_invalid <= 1'b0;						
				end
				`EXE_SW: begin
					aluop_o <= `EXE_SW_OP;   alusel_o <= `EXE_RES_LOAD_STORE; 
					reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b1; 
					wreg_o <= 1'b0; inst_invalid <= 1'b0;						
				end
				//op选项――条件跳转			
				`EXE_REGIMM_INST: begin
					case (rt)	// inst[20:16]
						`EXE_BGEZ: begin							
							aluop_o <= `EXE_BGEZ_OP; alusel_o <= `EXE_RES_JUMP_BRANCH;
							reg1_read_o <= 1'b1;	 reg2_read_o <= 1'b0;
							wreg_o <= 1'b0; inst_invalid <= 1'b0;	
						end
						`EXE_BGEZAL: begin								
							aluop_o <= `EXE_BGEZAL_OP; alusel_o <= `EXE_RES_JUMP_BRANCH;							 
							reg1_read_o <= 1'b1;  	   reg2_read_o <= 1'b0;							
							wreg_o <= 1'b1;	   inst_invalid <= 1'b0;
							wd_o <= 5'b11111;  
						end
						`EXE_BLTZ: begin
							aluop_o <= `EXE_BGEZAL_OP; alusel_o <= `EXE_RES_JUMP_BRANCH;
							reg1_read_o <= 1'b1;	   reg2_read_o <= 1'b0;
							wreg_o <= 1'b0;   inst_invalid <= 1'b0;	
						end
						`EXE_BLTZAL: begin
							aluop_o <= `EXE_BGEZAL_OP; alusel_o <= `EXE_RES_JUMP_BRANCH;
							reg1_read_o <= 1'b1;	   reg2_read_o <= 1'b0;
							wreg_o <= 1'b1;    inst_invalid <= 1'b0;							
							wd_o <= 5'b11111;
						end
						default: begin
						end
					endcase
				end		//`EXE_REGIMM_INST
				default: begin
				end
			endcase //case op
					  	//移位指令
		  	if (inst_i[31:21] == 11'b00000000000) begin
		  		if (func == `EXE_SLL) begin
		  			aluop_o <= `EXE_SLL_OP;   alusel_o <= `EXE_RES_SHIFT;
		  			reg1_read_o <= 1'b0;	  reg2_read_o <= 1'b1;	  	
					wreg_o <= 1'b1;   inst_invalid <= 1'b0;	
					wd_o <= inst_i[15:11];    imm[4:0] <= inst_i[10:6]; 
				end else if ( func == `EXE_SRL ) begin
		  			aluop_o <= `EXE_SRL_OP;   alusel_o <= `EXE_RES_SHIFT;
		  			reg1_read_o <= 1'b0;	  reg2_read_o <= 1'b1;	  	
					wreg_o <= 1'b1;	  inst_invalid <= 1'b0;	
					wd_o <= inst_i[15:11];	  imm[4:0] <= inst_i[10:6]; 
				end else if ( func == `EXE_SRA ) begin
		  			aluop_o <= `EXE_SRA_OP;   alusel_o <= `EXE_RES_SHIFT;
		  			reg1_read_o <= 1'b0;     reg2_read_o <= 1'b1;	  	
					wreg_o <= 1'b1;   inst_invalid <= 1'b0;	
					wd_o <= inst_i[15:11];	  imm[4:0] <= inst_i[10:6]; 
				end
			end		  
		   //ERET指令和协处理器指令 
		  	if(inst_i == `EXE_ERET) begin					
				aluop_o <= `EXE_ERET_OP; alusel_o <= `EXE_RES_NOP;
		  		reg1_read_o <= 1'b0;	 reg2_read_o <= 1'b0;
		  		wreg_o <= 1'b0; inst_invalid <= 1'b0; 	
				excepttype_is_eret<= 1'b1;				
			end else if(inst_i[31:21] == 11'b01000000000 && 
										inst_i[10:0] == 11'b00000000000) begin
				aluop_o <= `EXE_MFC0_OP; alusel_o <= `EXE_RES_MOVE;
				reg1_read_o <= 1'b0;	 reg2_read_o <= 1'b0;				
				wreg_o <= 1'b1;  inst_invalid <= 1'b0;	
				wd_o <= inst_i[20:16];			
			end else if(inst_i[31:21] == 11'b01000000100 && 
										inst_i[10:0] == 11'b00000000000) begin
				aluop_o <= `EXE_MTC0_OP; alusel_o <= `EXE_RES_NOP;				
				reg1_read_o <= 1'b1; 	 reg2_read_o <= 1'b0;
				wreg_o <= 1'b0; inst_invalid <= 1'b0;					   
				reg1_addr_o <= inst_i[20:16];									
			end
		end       //if
	end         //always
	
	// load-use 相关
	assign stallreq_for_reg1_load_use = ((pre_inst_is_load & reg1_read_o == 1'b1) && ex_wd_i == reg1_addr_o);
	assign stallreq_for_reg2_load_use = ((pre_inst_is_load & reg2_read_o == 1'b1) && ex_wd_i == reg2_addr_o);
		
	//提前接收EX,MEM阶段的数据，数据前推								
	always @ (*) begin
		if(rst == 1'b1) begin
			reg1_o <= `ZeroWord;						
		end else if((reg1_read_o == 1'b1) && (ex_wreg_i == 1'b1) 
								&& (ex_wd_i == reg1_addr_o)) begin
			reg1_o <= ex_wdata_i; 
		end else if((reg1_read_o == 1'b1) && (mem_wreg_i == 1'b1) 
								&& (mem_wd_i == reg1_addr_o)) begin
			reg1_o <= mem_wdata_i; 			
	  	end else if(reg1_read_o == 1'b1) begin
	  		reg1_o <= reg1_data_i;
	  	end else if(reg1_read_o == 1'b0) begin
	  		reg1_o <= imm;
	  	end else begin
	    	reg1_o <= `ZeroWord;
	  	end
	end

	//todo konw function
	always @ (*) begin
		if(rst == 1'b1) begin
			reg2_o <= `ZeroWord;	
		end else if((reg2_read_o == 1'b1) && (ex_wreg_i == 1'b1) 
								&& (ex_wd_i == reg2_addr_o)) begin
			reg2_o <= ex_wdata_i; 
		end else if((reg2_read_o == 1'b1) && (mem_wreg_i == 1'b1) 
								&& (mem_wd_i == reg2_addr_o)) begin
			reg2_o <= mem_wdata_i;			
	 	end else if(reg2_read_o == 1'b1) begin
	  		reg2_o <= reg2_data_i;
	 	end else if(reg2_read_o == 1'b0) begin
	  		reg2_o <= imm;
	  	end else begin
	    	reg2_o <= `ZeroWord;
	  	end
	end

	//延迟槽信号赋值
	assign is_in_delayslot_o = is_in_delayslot_i;

endmodule