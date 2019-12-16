`include "../defines.v"

module ex_select_result(
    input wire[7:0] alu_op_i,
    input wire[4:0] write_addr_i,
    input wire write_reg_i,
    input wire add_over_flag,
    input wire sub_over_flag,
    input wire[2:0] alu_sel_i,

    input wire[31:0] logic_result,
    input wire[31:0] shift_result,
    input wire[31:0] move_result,
    input wire[31:0] arith_result,
    input wire[31:0] link_address_i,

    output reg[4:0] write_addr_o,
    output reg[31:0] write_data_o,
    output reg overexcept,
    output reg write_reg_o
);

    always @(*)begin
        write_addr_o <= write_addr_i;
        if ((alu_op_i==`EXE_ADD_OP&&add_over_flag)||(alu_op_i==`EXE_SUB_OP)&&sub_over_flag)begin
            write_reg_o <= 1'b0;//disable wrige signal   
            overexcept <= 1'b1;
        end else begin
            write_reg_o <= write_reg_i;
            overexcept <= 1'b0;            
        end
        case (alu_sel_i)
            `EXE_RES_LOGIC:begin
                write_data_o <= logic_result;
            end
            `EXE_RES_SHIFT:begin
                write_data_o <= shift_result;
            end
            `EXE_RES_MOVE: begin
                write_data_o <= move_result;
            end
            `EXE_RES_ARITH:begin
                write_data_o <= arith_result;
            end
            `EXE_RES_JUMP_BRANCH:begin
                write_data_o <= link_address_i;
            end
            default: begin
                write_data_o <= 32'h00000000;
            end
        endcase
    end
endmodule