`include "../defines.v"

module ex_hilo_write(
    input wire[31:0] reg1_i,
    input wire[31:0] hi,
    input wire[31:0] lo,
    input wire[63:0] div_result_i,
    input wire[63:0] mult_result,
    input wire[7:0] alu_op_i,
    input wire rst,

    output reg[31:0] hi_o,
    output reg[31:0] lo_o,
    output reg write_hilo_o
);
    always @(*) begin
        if(rst == 1'b1) begin
            write_hilo_o <= 1'b0;
            hi_o <= 32'h00000000;
            lo_o <= 32'h00000000;
        end else if(alu_op_i == `EXE_MTHI_OP) begin
            write_hilo_o <= 1'b1;
            hi_o <= reg1_i;
            lo_o <= lo;
        end else if(alu_op_i == `EXE_MTLO_OP) begin
            write_hilo_o <= 1'b1;
            hi_o <= hi;
            lo_o <= reg1_i;
        end else if ((alu_op_i==`EXE_DIV_OP)||(alu_op_i==`EXE_DIVU_OP)) begin
            write_hilo_o <= 1'b1;
            hi_o <= div_result_i[63:32];
            lo_o <= div_result_i[31:0];
        end else if ((alu_op_i==`EXE_MULTU_OP) || (alu_op_i==`EXE_MULT_OP)) begin
            write_hilo_o <= 1'b1;
            hi_o <= mult_result[63:32];
            lo_o <= mult_result[31:0];
        end else begin
            write_hilo_o <= 1'b0;
            hi_o <= 32'h00000000;
            lo_o <= 32'h00000000;
        end
    end
endmodule