module hilo(
    input wire clk,
    input wire rst,

    //写端口 
    input wire write_enable,
    input wire[31:0] hi_i,
    input wire[31:0] lo_i,

    //读端口
    output reg [31:0] hi_o,
    output reg [31:0] lo_o
);
    reg[31:0] hi;
    reg[31:0] lo;
    always @ (posedge clk)begin
        if (rst == 1'b1) begin
            hi <= 32'h00000000;
            lo <= 32'h00000000;
        end else if (write_enable) begin
            hi <= hi_i;
            lo <= lo_i;
        end
    end

    always @ (*)begin
        hi_o <= hi;
        lo_o <= lo;
    end
endmodule