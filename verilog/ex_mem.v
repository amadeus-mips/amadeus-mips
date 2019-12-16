
module ex_mem(

	input	wire			   clk,
	input wire				   rst,

	//information from ctrl module
	input wire[5:0]			   stall,	
	input wire                 flush,
	
	//information from ex module
	input wire[4:0]            ex_wd,
	input wire                 ex_wreg,
	input wire[31:0]		   ex_wdata, 	
	input wire[31:0]           ex_hi,
	input wire[31:0]           ex_lo,
	input wire                 ex_whilo, 	

  //to achieve load,memory access
    input wire[7:0]            ex_aluop,
	input wire[31:0]           ex_mem_addr,
	input wire[31:0]           ex_mem_wdata,
	input wire				   ex_l_op,
	input wire 				   ex_s_op,
	input wire 				   ex_cached_trans,

	input wire                 ex_cp0_reg_we,
	input wire[4:0]            ex_cp0_reg_write_addr,
	input wire[31:0]           ex_cp0_reg_data,	

    input wire[31:0]           ex_excepttype,
	input wire                 ex_is_in_delayslot,
	input wire[31:0]           ex_current_inst_address,
	
	//information to mem module
	output reg[4:0]            mem_wd,
	output reg                 mem_wreg,
	output reg[31:0]		   mem_wdata,
	output reg[31:0]           mem_hi,
	output reg[31:0]           mem_lo,
	output reg                 mem_whilo,

  //to achieve load,memory access
    output reg[7:0]            mem_aluop,
	output reg[31:0]           mem_mem_addr,
	output reg[31:0]           mem_mem_wdata,
	output reg				   mem_l_op,
	output reg 				   mem_s_op,
	output reg 				   mem_cached_trans,
	
	output reg                 mem_cp0_reg_we,
	output reg[4:0]            mem_cp0_reg_write_addr,
	output reg[31:0]           mem_cp0_reg_data,
	
	output reg[31:0]           mem_excepttype,
  output reg                   mem_is_in_delayslot,
	output reg[31:0]           mem_current_inst_address
		
	
);


	always @ (posedge clk) begin
		if(rst | flush | (stall[3] & (~stall[4]))== 1'b1) begin
			mem_wd <= 5'b00000;
			mem_wreg <= 1'b0;
		    mem_wdata <= 32'h00000000;	
		    mem_hi <= 32'h00000000;
		    mem_lo <= 32'h00000000;
		    mem_whilo <= 1'b0;
  		    mem_aluop <= 8'b00000000;
			mem_mem_addr <= 32'h00000000;
			mem_mem_wdata <= 32'h00000000;	
			mem_cp0_reg_we <= 1'b0;
			mem_cp0_reg_write_addr <= 5'b00000;
			mem_cp0_reg_data <= 32'h00000000;	
			mem_excepttype <= 32'h00000000;
			mem_is_in_delayslot <= 1'b0;
	        mem_current_inst_address <= 32'h00000000;	
			mem_l_op <= 1'b0;
			mem_s_op <= 1'b0;			  
			mem_cached_trans <= 1'b0;				    
		end else if(stall[3] == 1'b0) begin
			mem_wd <= ex_wd;
			mem_wreg <= ex_wreg;
			mem_wdata <= ex_wdata;	
			mem_hi <= ex_hi;
			mem_lo <= ex_lo;
			mem_whilo <= ex_whilo;	
  		    mem_aluop <= ex_aluop;
			mem_mem_addr <= ex_mem_addr;
			mem_mem_wdata <= ex_mem_wdata;
			mem_cp0_reg_we <= ex_cp0_reg_we;
			mem_cp0_reg_write_addr <= ex_cp0_reg_write_addr;
			mem_cp0_reg_data <= ex_cp0_reg_data;	
			mem_excepttype <= ex_excepttype;
			mem_is_in_delayslot <= ex_is_in_delayslot;
	        mem_current_inst_address <= ex_current_inst_address;
			mem_l_op <= ex_l_op;
			mem_s_op <= ex_s_op;	
			mem_cached_trans <= ex_cached_trans;					
		end else begin
		end    //if
	end      //always
			

endmodule