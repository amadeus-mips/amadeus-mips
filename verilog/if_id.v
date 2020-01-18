module if_id(

	input wire          clk,        
	input wire          rst,        

	//information from ctrl module
	input wire[5:0]     stall,	        
	input wire          flush,
	
	input wire 		 	if_excepttype_i,
	input wire[31:0]    if_pc,      	//instruction address from if module
	input wire[31:0]    if_inst,        //instruction from if module
	output reg 			if_excepttype_o,
	output reg[31:0]    id_pc,      	//instruction address that id module needs
	output reg[31:0]    id_inst         //intruction that id module needs
	
);


	always @ (posedge clk) begin
		if (rst == 1'b1) begin
			id_pc <= 32'h00000000;
			id_inst <= 32'h00000000;
			if_excepttype_o <= 1'b0;
		end else if(flush == 1'b1 ) begin
			id_pc <= 32'h00000000;
			id_inst <= 32'h00000000;					
			if_excepttype_o <= 1'b0;
		end else if(stall[1] == 1'b1 && stall[2] == 1'b0) begin
			id_pc <= 32'h00000000;
			id_inst <= 32'h00000000;
			if_excepttype_o <= 1'b0;	
	  	end else if(stall[1] == 1'b0) begin
		    id_pc <= if_pc;
			if(if_excepttype_i == 1'b1) begin
				id_inst <= 32'h00000000;
			end else begin
				//id_inst <= if_inst;
				//此处可应该可以忽略，使用被注释版本也可
		    	if(if_inst === 32'hxxxxxxxx)begin
		      		id_inst <= 32'b00000000;
		    	end else begin
					id_inst <= if_inst;
				end
			end
			if_excepttype_o <= if_excepttype_i;
		end
	end

endmodule