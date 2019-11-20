`include "../defines.v"

module ex_hilo_register(//���hilo������ϵ
    input wire rst,
    input wire[31:0] mem_hi_i,
    input wire[31:0] mem_lo_i,
    input wire mem_write_hilo_i,

    input wire[31:0] wb_hi_i,
    input wire[31:0] wb_lo_i,
    input wire wb_write_hilo_i,

    input wire[31:0] hi_i,
    input wire[31:0] lo_i,

    output reg[31:0] hi,
    output reg[31:0] lo
);
    always @ (*) begin
        if (rst == 1'b1) begin
            hi <= 32'h00000000;
            lo <= 32'h00000000;
        end else if (mem_write_hilo_i == 1'b1) begin//�����mem�׶ε���������
            hi <= mem_hi_i;
            lo <= mem_lo_i;
        end else if (wb_write_hilo_i == 1'b1)begin//����ͻ�д�׶ε���������
            hi <= wb_hi_i;
            lo <= wb_lo_i;
        end else begin
            hi <= hi_i;
            lo <= lo_i;
        end
    end
endmodule