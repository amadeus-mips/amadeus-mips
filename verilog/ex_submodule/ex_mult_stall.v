`include "../defines.v"

module ex_mult(
    input wire rst,
    input wire clk,
    input wire flush,

    input wire[7:0] alu_op_i,
    input wire[31:0] reg1_i,
    input wire[31:0] reg2_i,
    
    output reg[63:0] mult_result,

    output reg stallreq_for_mult
);
    wire[31:0] mult_op1, mult_op2;
    wire [63:0] mult_result_tmp;
    reg start_i;
    wire mult_ready;

    assign mult_op1 = ((alu_op_i == `EXE_MULT_OP) && (reg1_i[31] == 1'b1)) ? (~reg1_i + 1) : reg1_i;
    assign mult_op2 = ((alu_op_i == `EXE_MULT_OP) && (reg2_i[31] == 1'b1)) ? (~reg2_i + 1) : reg2_i;
    
    always @(*) begin
        if(rst == 1'b1) begin
            stallreq_for_mult <= 1'b0;
            start_i <= 1'b0;
        end else begin
            if(alu_op_i == `EXE_MULTU_OP || alu_op_i == `EXE_MULT_OP) begin
                if(mult_ready == 1'b0) begin
                    stallreq_for_mult <= 1'b1;
                    start_i     <= 1'b1;
                end else begin
                    stallreq_for_mult <= 1'b0;
                    start_i <= 1'b0;
                end
            end else begin
                stallreq_for_mult <= 1'b0;
                start_i <= 1'b0;
            end
        end
    end

    always @(*) begin
        if(rst == 1'b1) begin
            mult_result <= 64'h0000_0000_0000_0000;
        end else if((alu_op_i == `EXE_MULT_OP) && (reg1_i[31] ^ reg2_i[31] == 1'b1)) begin
            mult_result <= ~mult_result_tmp + 1;
        end else begin
            mult_result <= mult_result_tmp;
        end
    end

mult u_mult(
	.rst      (rst      ),
    .clk      (clk      ),
    .mcand    (mult_op1    ),
    .mplier   (mult_op2   ),
    .start_i  (start_i  ),
    .flush    (flush    ),
    .result_o (mult_result_tmp ),
    .ready_o  (mult_ready  )
);


endmodule