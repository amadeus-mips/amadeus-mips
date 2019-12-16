module mult(
    input wire          rst,
    input wire          clk,

    input wire[31:0]    mcand,          // ±»³ËÊı
    input wire[31:0]    mplier,         // ³ËÊı
    input wire          start_i,        // start mult
    input wire          flush,          // cancel divide

    output wire[63:0]    result_o,
    output reg          ready_o       // divide complete
);
    
    reg[2:0]    cnt;
    
    always @(posedge clk) begin
        if(rst == 1'b1)  begin
            cnt <= 3'd0;
            ready_o <= 1'b0;
        end else begin
            if(flush == 1'b1) begin
                cnt <= 3'd0;
                ready_o <= 1'b0;
            end else begin
                case (cnt)
                    3'b000: begin
                        if(start_i == 1'b1) begin
                            cnt <= 3'b001;
                            ready_o <= 1'b0;
                        end else begin
                            cnt <= cnt;
                            ready_o <= 1'b0;
                        end
                    end 
                    3'b001: begin
                        cnt <= 3'b010;
                        ready_o <= 1'b0;
                    end
                    3'b010: begin
                        cnt <= 3'b011;
                        ready_o <= 1'b0;
                    end
                    3'b011: begin
                        cnt <= 3'b100;
                        ready_o <= 1'b0;
                    end
                    3'b100: begin
                        cnt <= 3'b000;
                        ready_o <= 1'b1;
                    end
                    default: begin
                        cnt <= 3'b000;
                        ready_o <= 1'b0;
                    end
                endcase
            end
        end
    end
    
    mult_gen_0 u_mult_gen_0(
        .CLK    (clk),
        .A      (mcand),
        .B      (mplier),
        .CE     (~rst),
        .SCLR   (flush),
        .P      (result_o)
    );

endmodule // mult