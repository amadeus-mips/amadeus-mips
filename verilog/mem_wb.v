

module mem_wb(

	input wire					clk,
	input wire					rst,

  //information from ctrl module
	input wire[5:0]             stall,	
  input wire                    flush,	
	//information from mem module	
	input wire[4:0]             mem_wd,
	input wire                  mem_wreg,
	input wire[31:0]			mem_wdata,
	input wire[31:0]            mem_hi,
	input wire[31:0]            mem_lo,
	input wire                  mem_whilo,	
	
	input wire                  mem_cp0_reg_we,
	input wire[4:0]             mem_cp0_reg_write_addr,
	input wire[31:0]            mem_cp0_reg_data,			
	input wire[31:0]			current_pc_i,


	//information to wb module
	output reg[4:0]             wb_wd,
	output reg                  wb_wreg,
	output reg[31:0]			wb_wdata,
	output reg[31:0]            wb_hi,
	output reg[31:0]            wb_lo,
	output reg                  wb_whilo,

	output reg                  wb_cp0_reg_we,
	output reg[4:0]             wb_cp0_reg_write_addr,
	output reg[31:0]            wb_cp0_reg_data,							       
	output reg[31:0]			current_pc_o
);


	always @ (posedge clk) begin
		if(rst == 1'b1) begin
			wb_wd <= 5'b00000;
			wb_wreg <= 1'b0;
		    wb_wdata <= 32'h00000000;	
		    wb_hi <= 32'h00000000;
		    wb_lo <= 32'h00000000;
		    wb_whilo <= 1'b0;		
			wb_cp0_reg_we <= 1'b0;
			wb_cp0_reg_write_addr <= 5'b00000;
			wb_cp0_reg_data <= 32'h00000000;	
			current_pc_o <= 32'h00000000;		
		end else if(flush == 1'b1 ) begin
			wb_wd <= 5'b00000;
			wb_wreg <= 1'b0;
		    wb_wdata <= 32'h00000000;
		    wb_hi <= 32'h00000000;
		    wb_lo <= 32'h00000000;
		    wb_whilo <= 1'b0;
			wb_cp0_reg_we <= 1'b0;
			wb_cp0_reg_write_addr <= 5'b00000;
			wb_cp0_reg_data <= 32'h00000000;	
			current_pc_o	<= 32'h00000000;	  				  	  	
		end else if(stall[4] == 1'b1 && stall[5] == 1'b0) begin
			wb_wd <= 5'b00000;
			wb_wreg <= 1'b0;
		    wb_wdata <= 32'h00000000;
		    wb_hi <= 32'h00000000;
		    wb_lo <= 32'h00000000;
		    wb_whilo <= 1'b0;	
			wb_cp0_reg_we <= 1'b0;
			wb_cp0_reg_write_addr <= 5'b00000;
			wb_cp0_reg_data <= 32'h00000000;	
			current_pc_o	<= 32'h00000000;				  		  	  	  
		end else if(stall[4] == 1'b0) begin
			wb_wd <= mem_wd;
			wb_wreg <= mem_wreg;
			wb_wdata <= mem_wdata;
			wb_hi <= mem_hi;
			wb_lo <= mem_lo;
			wb_whilo <= mem_whilo;		
			wb_cp0_reg_we <= mem_cp0_reg_we;
			wb_cp0_reg_write_addr <= mem_cp0_reg_write_addr;
			wb_cp0_reg_data <= mem_cp0_reg_data;	
			current_pc_o <= current_pc_i;	  		
		end else begin
		end    //if
	end      //always
			

endmodule