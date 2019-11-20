

module ctrl(
	
	input wire				rst,

	input wire[31:0]        excepttype_i,
	input wire[31:0]        cp0_epc_i,

	input wire				stallreq_from_pc,
	input wire              stallreq_from_id,

  //stall requirement from ex module
	input wire              stallreq_from_ex,
	input wire 				stallreq_from_mem,

	output wire[31:0]        new_pc,
	output wire              flush,	
	output wire[5:0]         stall       
	
);


	assign stall = 	(|excepttype_i != 1'b0) ? 6'd0 :
					(stallreq_from_mem) ? 6'b011111 :
					(stallreq_from_ex) 	? 6'b001111 :
					(stallreq_from_id)  ? 6'b000111 :
					(stallreq_from_pc)  ? 6'b000111 : 6'd0;
	
	assign new_pc = (|excepttype_i != 1'b0) ? ((excepttype_i[12] == 1'b1) ? cp0_epc_i : 32'hbfc00380) : 32'd0;

	assign flush = |excepttype_i;

						 

	//always @ (*) begin
	//	if(rst == 1'b1) begin
	//		stall <= 6'b000000;
	//		flush <= 1'b0;
	//		new_pc <= 32'h00000000;
	//	end else if(excepttype_i != 32'h00000000) begin
	//	  	flush <= 1'b1;
	//	  	stall <= 6'b000000;
	//		if(excepttype_i[12] == 1'b1) begin
	//			new_pc <= cp0_epc_i;
	//		end else begin
	//			new_pc <=32'hbfc00380;
	//		end
	//	end else if(stallreq_from_mem == 1'b1) begin
	//		stall <= 6'b011111;
	//		flush <= 1'b0;		
	//	end else if(stallreq_from_ex == 1'b1) begin
	//		stall <= 6'b001111;
	//		flush <= 1'b0;		
	//	end else if(stallreq_from_id == 1'b1) begin
	//		stall <= 6'b000111;	
	//		flush <= 1'b0;		
	//	end else if(stallreq_from_pc == 1'b1) begin
	//		stall <= 6'b000111;
	//		flush <= 1'b0;
	//	end else begin
	//		stall <= 6'b000000;
	//		flush <= 1'b0;
	//		new_pc <= 32'h00000000;		
	//	end    //if
	//end      //always
			

endmodule