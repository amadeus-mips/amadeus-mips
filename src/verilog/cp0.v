
module cp0(

	input wire			       clk,
	input wire				   rst,
	
	// wb½×¶ÎĞ´cp0
	input wire                 we_i,
	input wire[4:0]            waddr_i,
	input wire[31:0]           data_i,
	
	// ex½×¶Î¶Ácp0
	input wire[4:0]            raddr_i,

	input wire[31:0]           excepttype_i,
	input wire[5:0]            int_i,			// Íâ²¿ÖĞ¶Ï	

	input wire[31:0]           current_inst_addr_i,	
	input wire                 is_in_delayslot_i,
	input wire[31:0]           bad_addr_i,

	// Ë²Ê±Êä³ö
	output reg[31:0]           data_o,

	output reg[31:0]           count_o,
	output reg[31:0]           compare_o,
	output reg[31:0]           status_o,
	output reg[31:0]           cause_o,
	output reg[31:0]           epc_o,
	output reg[31:0]   		   badvaddr_o
	
);
	reg flag;
	/*----------------- Write operations to cp0 register-----------------*/
	always @ (posedge clk) begin
		if(rst == 1'b1) begin
			flag <= 1'b0;
			// initial Count as 0
			count_o <= 32'h00000000; 
			// initial Compare as 0
			compare_o <= 32'h00000000; 
			// initial Status CU:4'b0001 shows that cp0 is subsistent
			status_o <= 32'b00010000001000000000000000000000;
			// initial Cause as 0
			cause_o <= 32'h00000000;
			// initial Epc as 0
			epc_o <= 32'h00000000;
			// initial badvaddr_o as 0
			badvaddr_o <= 32'h00000000;
		end else begin

			// for every cycle Count+1
			if(flag == 1'b1) begin
				count_o <= count_o + 1;				
			end
			flag <= ~flag;
			// cause 10~15bit saves external interrupt statement
			cause_o[15:10] <= int_i;

			if(we_i == 1'b1) begin 
				case (waddr_i)
					5'b01001:begin  // write Count register
						count_o <=data_i;
					end
					5'b01011:begin	// write Compare register
						compare_o <= data_i;	
					end
					5'b01100:begin	// wirter Status rgeister
						status_o <=data_i;	
					end
					5'b01110:begin	// write Epc register
						epc_o <= data_i;
					end
					5'b01101:begin	// write Cause register
						cause_o <= data_i;				
					end
					default: begin
                    end
				endcase
			end

			if(excepttype_i[7:0]!=8'b00000000) begin	// interrupt
				if(is_in_delayslot_i == 1'b1) begin
					epc_o <= current_inst_addr_i - 4;
					cause_o[31] <= 1'b1;
				end else begin
					epc_o <= current_inst_addr_i;
					cause_o[31] <= 1'b0;
				end
				status_o[1] <= 1'b1;
				cause_o[6:2] <= 5'b00000;
			end else if(excepttype_i[13] == 1'b1) begin	// pc bad addr
				if(is_in_delayslot_i == 1'b1) begin
					epc_o <= current_inst_addr_i - 4;
					cause_o[31] <= 1'b1;
				end else begin
					epc_o <= current_inst_addr_i;
					cause_o[31] <= 1'b0;
				end
				status_o[1] <= 1'b1;
				cause_o[6:2] <= 5'b00100;
				badvaddr_o <= bad_addr_i;
			end else if(excepttype_i[10] == 1'b1) begin // instvalid
				if(is_in_delayslot_i == 1'b1) begin
					epc_o <= current_inst_addr_i - 4;
					cause_o[31] <= 1'b1;
				end else begin
					epc_o <= current_inst_addr_i;
					cause_o[31] <= 1'b0;
				end
				status_o[1] <= 1'b1;
				cause_o[6:2] <= 5'b01010;
			end else if(excepttype_i[16] == 1'b1) begin	// ov
				if(is_in_delayslot_i == 1'b1) begin
					epc_o <= current_inst_addr_i - 4;
					cause_o[31] <= 1'b1;
				end else begin
					epc_o <= current_inst_addr_i;
					cause_o[31] <= 1'b0;
				end
				status_o[1] <= 1'b1;
				cause_o[6:2] <= 5'b01100;
			end else if(excepttype_i[9] == 1'b1) begin	// break
				if(is_in_delayslot_i == 1'b1) begin
						epc_o <= current_inst_addr_i - 4;
						cause_o[31] <= 1'b1;
					end else begin
						epc_o <= current_inst_addr_i;
						cause_o[31] <= 1'b0;
					end
				status_o[1] <= 1'b1;
				cause_o[6:2] <= 5'b01001;
			end else if(excepttype_i[8] == 1'b1) begin	// syscall
				if(is_in_delayslot_i == 1'b1) begin
					epc_o <= current_inst_addr_i - 4;
					cause_o[31] <= 1'b1;
				end else begin
					epc_o <= current_inst_addr_i;
					cause_o[31] <= 1'b0;
				end
				status_o[1] <= 1'b1;
				cause_o[6:2] <= 5'b01000;
			end else if(excepttype_i[14] == 1'b1) begin	// mem read addr error
				if(is_in_delayslot_i == 1'b1) begin
						epc_o <= current_inst_addr_i - 4;
						cause_o[31] <= 1'b1;
					end else begin
						epc_o <= current_inst_addr_i;
						cause_o[31] <= 1'b0;
					end
				status_o[1] <= 1'b1;
				cause_o[6:2] <= 5'b00100;
				badvaddr_o <= bad_addr_i;
			end else if(excepttype_i[15] == 1'b1) begin	// mem write addr error
				if(is_in_delayslot_i == 1'b1) begin
						/* code */
						epc_o <= current_inst_addr_i - 4;
						cause_o[31] <= 1'b1;
					end else begin
						epc_o <= current_inst_addr_i;
						cause_o[31] <= 1'b0;
					end
				status_o[1] <= 1'b1;
				cause_o[6:2] <= 5'b00101;
				badvaddr_o <= bad_addr_i;
			end else if(excepttype_i[12] == 1'b1) begin	// eret
				status_o[1] <= 1'b0;
			end
		end
	end

	

	/*----------------- Read operations to cp0 register-----------------*/
	always @ (*) begin
		if(rst == 1'b1) begin
			data_o <=32'h00000000;
		end else begin
			case (raddr_i)
				5'b01001:begin	// read Count register
					data_o <= count_o;
				end
				5'b01011:begin	// read Compare register
					data_o <= compare_o;
				end
				5'b01100:begin	// read Status register
					data_o <= status_o;
				end
				5'b01101:begin	// read Cause register
					data_o <= cause_o;
				end
				5'b01110:begin // read EPC register
					data_o <= epc_o;
				end
				5'b01000:begin	// read badvaddr_o register
					data_o <= badvaddr_o; 
				end
				default:begin
				    data_o <= 32'd0;
				end
			endcase
		end
	end

endmodule
