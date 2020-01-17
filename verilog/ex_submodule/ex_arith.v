`include "../defines.v"

module ex_arith(
    input wire rst,
    input wire[31:0] reg1_i,
    input wire[31:0] reg2_i,
    input wire[7:0] alu_op_i,
    output wire add_over_flag,
    output wire sub_over_flag,
    output reg[31:0]arith_result
);
    wire [31:0] reg2_i_mux;//操作数2的补码
    wire [31:0] add_result;//加法结果
    wire [31:0] sub_result;//减法结果
    wire slt_result;//有符号比较结果
    wire sltu_result;//无符号比较结果

    assign add_result = reg1_i + reg2_i;
    assign sub_result = reg1_i + reg2_i_mux;
    assign reg2_i_mux = (~reg2_i)+1;
    assign add_over_flag = ((reg1_i[31]&&reg2_i[31])&&(!add_result[31]))||((!reg1_i[31]&&!reg2_i[31])&&add_result[31]);
    assign sub_over_flag = ((reg1_i[31]&&reg2_i_mux[31])&&(!sub_result[31]))||((!reg1_i[31]&&!reg2_i_mux[31])&&sub_result[31]);
    assign slt_result = ((reg1_i[31]&&!reg2_i[31])||(reg1_i[31]&&reg2_i[31]&&sub_result[31])||(!reg1_i[31]&&!reg2_i[31]&&sub_result[31]));
    assign sltu_result = reg1_i < reg2_i;

    always @(*)begin
        if(rst==1'b1)begin
            arith_result <= 32'h00000000;
        end else begin
            case (alu_op_i)
                `EXE_ADD_OP:begin
                    arith_result <= add_result;
                end
                `EXE_SUB_OP:begin
                    arith_result <= sub_result;
                end
                `EXE_ADDU_OP:begin
                    arith_result <= add_result;
                end
                `EXE_SUBU_OP:begin
                    arith_result <= sub_result;
                end 
                `EXE_SLT_OP:begin
                    arith_result <= slt_result;
                end
                `EXE_SLTU_OP:begin
                    arith_result <= sltu_result;
                end
                default: begin
                    arith_result <= 32'h00000000;
                end
            endcase
        end
    end
endmodule