`include "defines.v"

module regfile(

	input	wire		clk,
	input wire			rst,
	
	//写端口
	input wire			we,
	input wire[4:0]    	waddr,
	input wire[31:0]	wdata,
	
	//读端口1
	input wire			re1,
	input wire[4:0]    	raddr1,
	output reg[31:0]    rdata1,
	
	//读端口2
	input wire			re2,
	input wire[4:0]		raddr2,
	output reg[31:0]    rdata2
	
);

	reg[31:0]  regs[31:0];

	always @ (posedge clk) begin
		if (rst == 1'b1) begin
			regs[0] <=32'd0;
			regs[1] <=32'd0;
			regs[2] <=32'd0;
			regs[3] <=32'd0;
			regs[4] <=32'd0;
			regs[5] <=32'd0;
			regs[6] <=32'd0;
			regs[7] <=32'd0;
			regs[8] <=32'd0;
			regs[9] <=32'd0;
			regs[10]<=32'd0;
			regs[11]<=32'd0;
			regs[12]<=32'd0;
			regs[13]<=32'd0;
			regs[14]<=32'd0;
			regs[15]<=32'd0;
			regs[16]<=32'd0;
			regs[17]<=32'd0;
			regs[18]<=32'd0;
			regs[19]<=32'd0;
			regs[20]<=32'd0;
			regs[21]<=32'd0;
			regs[22]<=32'd0;
			regs[23]<=32'd0;
			regs[24]<=32'd0;
			regs[25]<=32'd0;
			regs[26]<=32'd0;
			regs[27]<=32'd0;
			regs[28]<=32'd0;
			regs[29]<=32'd0;
			regs[30]<=32'd0;
			regs[31]<=32'd0;
		end else begin
			if((we == 1'b1) && (waddr != 5'h0)) begin
				regs[waddr] <= wdata;
			end
		end
	end
	
	always @ (*) begin
		if(rst == 1'b1) begin
			rdata1 <= 32'd0;
		end else begin
			if(raddr1 == 5'd0) begin
				rdata1 <= 32'd0;
			end else if(re1 == 1'b1 && we == 1'b1 && raddr1 == waddr) begin	// 数据前推
				rdata1 <= wdata;
			end else if(re1 == 1'b1) begin
				rdata1 <= regs[raddr1];
			end else begin
				rdata1 <= 32'd0;
			end
		end
	end

	always @ (*) begin
		if(rst == 1'b1) begin
			rdata2 <= 32'd0;
		end else begin
			if(raddr2 == 5'd0) begin
				rdata2 <= 32'd0;
			end else if(re2 == 1'b1 && we == 1'b1 && raddr2 == waddr) begin
				rdata2 <= wdata;
			end else if(re2 == 1'b1) begin
				rdata2 <= regs[raddr2];
			end else begin
				rdata2 <= 32'd0;
			end
		end
	end

endmodule