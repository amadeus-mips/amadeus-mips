`include "../defines.v"

module ex_logic(
    input wire[31:0] reg1_i,
    input wire[31:0] reg2_i,
    input wire[7:0] alu_op_i,
    input wire rst,
    output reg[31:0] logic_result
);
    always @(*)begin
        if(rst==1'b1) begin//���rstʹ�ܣ����ZeroWord
            logic_result <= 32'h00000000;
        end else begin
            case (alu_op_i)
                `EXE_OR_OP:begin//�߼���
                    logic_result <= reg1_i | reg2_i;
                end
                `EXE_AND_OP:begin//�߼���
                    logic_result <= reg1_i & reg2_i;
                end
                `EXE_NOR_OP:begin//�߼����
                    logic_result <= ~(reg1_i | reg2_i); 
                end
                `EXE_XOR_OP:begin//�߼����
                    logic_result <= reg1_i ^ reg2_i;
                end
                default: begin
                    logic_result<=32'h00000000;
                end
            endcase
        end
    end
endmodule