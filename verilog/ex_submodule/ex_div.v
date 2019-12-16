`include "../defines.v"

module ex_div(
    input wire rst,
    input wire[7:0] alu_op_i,
    input wire[31:0] reg1_i,
    input wire[31:0] reg2_i,
    input wire div_ready_i,

    output reg stallreq_for_div,
    output reg[31:0] div_op_data1_o,
    output reg[31:0] div_op_data2_o,
    output reg div_start_o,
    output reg signed_div_o
);
    always @(*)begin
        if(rst==1'b1)begin
            stallreq_for_div <= 1'b0;
            div_op_data1_o <= 32'h00000000;
            div_op_data2_o <= 32'h00000000;
            div_start_o <= 1'b0;
            signed_div_o <= 1'b0;
        end else begin
            case(alu_op_i)
                `EXE_DIV_OP:begin
                    if(div_ready_i == 1'b0)begin
                        div_op_data1_o <= reg1_i;
                        div_op_data2_o <= reg2_i;
                        div_start_o <= 1'b1;
                        signed_div_o <= 1'b1;
                        stallreq_for_div <= 1'b1;
                    end else if (div_ready_i == 1'b1) begin
                        div_op_data1_o <= reg1_i;
                        div_op_data2_o <= reg2_i;
                        div_start_o <= 1'b0;
                        signed_div_o <= 1'b1;
                        stallreq_for_div <= 1'b0;
                    end else begin
                        div_op_data1_o <= 32'h00000000;
                        div_op_data2_o <= 32'h00000000;
                        div_start_o <= 1'b0;
                        signed_div_o <= 1'b0;
                        stallreq_for_div <= 1'b0;
                    end
                end
                `EXE_DIVU_OP:begin
                    if(div_ready_i == 1'b0)begin
                        div_op_data1_o <= reg1_i;
                        div_op_data2_o <= reg2_i;
                        div_start_o <= 1'b1;
                        signed_div_o <= 1'b0;
                        stallreq_for_div <= 1'b1;
                    end else if (div_ready_i == 1'b1) begin
                        div_op_data1_o <= reg1_i;
                        div_op_data2_o <= reg2_i;
                        div_start_o <= 1'b0;
                        signed_div_o <= 1'b0;
                        stallreq_for_div <= 1'b0;
                    end else begin
                        div_op_data1_o <= 32'h00000000;
                        div_op_data2_o <= 32'h00000000;
                        div_start_o <= 1'b0;
                        signed_div_o <= 1'b0;
                        stallreq_for_div <= 1'b0;
                    end
                end
                default:begin
                    div_op_data1_o <= 32'h00000000;
                    div_op_data2_o <= 32'h00000000;
                    div_start_o <= 1'b0;
                    signed_div_o <= 1'b0;
                    stallreq_for_div <= 1'b0;
                end
            endcase
        end
    end
endmodule