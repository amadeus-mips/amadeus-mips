`include "../defines.v"

module ex_move(
    input wire[31:0] hi,
    input wire[31:0] lo,
    input wire[7:0] alu_op_i,
    input wire[31:0] inst_i,
    input wire[31:0] cp0_reg_data_i,

    input wire rst,

    input wire mem_cp0_reg_we,
    input wire[4:0] mem_cp0_reg_write_addr,
    input wire[31:0] mem_cp0_reg_data,

    input wire wb_cp0_reg_we,
    input wire[4:0] wb_cp0_reg_write_addr,
    input wire[31:0] wb_cp0_reg_data,


    output reg[31:0] move_result,
    output reg[4:0]cp0_reg_read_addr_o
);
    always @(*) begin
        if (rst == 1'b1) begin
            move_result <= 32'h00000000;
            cp0_reg_read_addr_o <= 5'd0;
        end else begin
            move_result <= 32'd0;
            cp0_reg_read_addr_o <= 5'd0;
            case (alu_op_i)
                `EXE_MFHI_OP:begin//取高位到rd
                    move_result <= hi;
                end
                `EXE_MFLO_OP:begin//取低位到rd
                    move_result <= lo;
                end
                `EXE_MFC0_OP:begin//从cp0中读取到rt
                    cp0_reg_read_addr_o <= inst_i[15:11];
                    if(mem_cp0_reg_we == 1'b1 && mem_cp0_reg_write_addr == inst_i[15:11]) begin
                        move_result <= mem_cp0_reg_data;
                    end else if(wb_cp0_reg_we == 1'b1 && wb_cp0_reg_write_addr == inst_i[15:11]) begin
                        move_result <= wb_cp0_reg_data;
                    end else begin
                        move_result <= cp0_reg_data_i;
                    end
                end
                default: begin
                    move_result <= 32'h00000000;
                end
            endcase
        end
    end
endmodule