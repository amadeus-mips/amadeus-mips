`include "../defines.v"

module ex_shift(
    input wire[31:0] reg1_i,
    input wire[31:0] reg2_i,
    input wire[7:0] alu_op_i,
    input wire rst,
    output reg[31:0] shift_result
);
    wire [31:0] high_value;
    assign high_value = {32{reg2_i[31]}} << (6'd32 - {1'b0,reg1_i[4:0]});//�������Ƽ�����ߵĸ�λ
    always @(*)begin
        if(rst==1'b1) begin//���rstʹ�ܣ����ZeroWord
            shift_result <= 32'h00000000;
        end else begin
            case (alu_op_i)
                `EXE_SLL_OP:begin//�߼�����
                    shift_result <= reg2_i << reg1_i[4:0];
                end
                `EXE_SRL_OP:begin//�߼�����
                    shift_result <= reg2_i >> reg1_i[4:0];
                end
                `EXE_SRA_OP:begin//��������
                    shift_result <= reg2_i >> reg1_i[4:0] | high_value;
                end
                default: begin
                    shift_result <= 32'h00000000;
                end
            endcase
        end
    end
endmodule