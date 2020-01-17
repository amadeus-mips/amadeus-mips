module inst_fetch(

	input wire					clk,
	input wire					rst,

	// information from ctrl module
	input wire[5:0]             stall,
	input wire                  flush,
	input wire[31:0]            new_pc,

	// information from id module
	input wire                  branch_flag_i,
	input wire[31:0]            branch_target_address_i,
	
	output reg					if_excepttype_o,
	
	// to ram and if_id
	output reg[31:0]			pc,
	// to ram 
	output reg  				pc_en,
	// from ram
	input wire[31:0] 			inst_i,
	input wire 					inst_valid,

	output wire[31:0]			inst_o,

	output reg 					stallreq
);

	reg 	ce;
	reg[31:0] 	inst_buffer;
	reg			use_ibuffer;

	always @ (posedge clk) begin
		if (ce == 1'b0) begin
			pc <= 32'hbfbffffc;
			if_excepttype_o <= 1'b0;
			pc_en <= 1'b0;
			inst_buffer <= 32'd0;
			use_ibuffer <= 1'b0;
		end else begin
			if(flush == 1'b1) begin			// 发生例外清空流水线，修改pc为指定地�?
				pc <= new_pc;
				if(new_pc[1:0] != 2'b00) begin
					if_excepttype_o <= 1'b1;
					pc_en <= 1'b0;					
				end else begin
					if_excepttype_o <= 1'b0;
					pc_en <= 1'b1;					
				end			
				use_ibuffer <= 1'b0;
			end else if(stall[0] == 1'b0) begin
				if(branch_flag_i == 1'b1) begin
					pc <= branch_target_address_i;
					if(branch_target_address_i[1:0] != 2'b00) begin
						if_excepttype_o <= 1'b1;
						pc_en <= 1'b0;						
					end else begin
					    if_excepttype_o <= 1'b0;
						pc_en <= 1'b1;
					end					
				end else begin
		  			pc <= pc + 4'h4;
					if_excepttype_o <= 1'b0;
					pc_en <= 1'b1;
		  		end 
				use_ibuffer <= 1'b0;
		  	end else begin
				pc <= pc;
				if_excepttype_o <= 1'b0;	
				if(stallreq == 1'b0) begin
					pc_en <= 1'b0;
					inst_buffer <= use_ibuffer ? inst_buffer : inst_i;
					use_ibuffer <= 1'b1;
				end else begin
					pc_en <= 1'b1;
					use_ibuffer <= 1'b0;
				end
			end
		end
	end


	always @ (posedge clk) begin
		if (rst == 1'b1) begin
			ce <= 1'b0;
		end else begin
			ce <= 1'b1;
		end
	end

	always @(*) begin
		if(rst == 1'b1) begin
			stallreq <= 1'b0;
		end else begin
			if(pc_en == 1'b1 && inst_valid == 1'b0) begin
				stallreq <= 1'b1;
			end else if(pc_en == 1'b1 && inst_valid == 1'b1) begin
				stallreq <= 1'b0;
			end else begin
				stallreq <= 1'b0;
			end
		end
	end

	assign inst_o = (use_ibuffer == 1'b1) ? inst_buffer : inst_i;

endmodule