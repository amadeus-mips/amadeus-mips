`include "../defines.v"

module ex_cpzero(
    input wire rst,
    input wire[31:0] reg1_i,
    input wire[31:0] inst_i,
    input wire[7:0] alu_op_i,

    output reg cp0_reg_we_o,
    output reg[31:0] cp0_reg_data_o,
    output reg[4:0] cp0_reg_write_addr_o
);
    always @(*) begin
        if (rst == 1'b1) begin
            cp0_reg_write_addr_o <= 5'b00000;
            cp0_reg_we_o        <= 1'b0;
            cp0_reg_data_o      <= 32'h00000000;
        end else if(alu_op_i == `EXE_MTC0_OP) begin
            cp0_reg_write_addr_o <= inst_i[15:11];
            cp0_reg_we_o        <= 1'b1;
            cp0_reg_data_o      <= reg1_i;
        end else begin
            cp0_reg_write_addr_o <= 5'b00000;
            cp0_reg_we_o        <= 1'b0;
            cp0_reg_data_o      <= 32'h00000000;
        end
    end
endmodule