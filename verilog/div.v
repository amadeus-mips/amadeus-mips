`define DIV_FREE        2'b00
`define DIV_ON          2'b01
`define DIV_END         2'b10
`define DIV_BY_ZERO     2'b11

`define ZERO_DOUBLE_WORD 64'h0000_0000_0000_0000
`define ZERO_WORD       32'h00000000

module div(
    input wire          rst,
    input wire          clk,
    
    input wire          signed_div_i,       
    input wire[31:0]    opdata1_i,    
    input wire[31:0]    opdata2_i,
    input wire          start_i,            // start divide
    input wire          annul_i,            // cancel divide
    
    output reg[63:0]    result_o,
    output reg          ready_o             // divide complete
);

    reg[1:0]    state;
    reg[5:0]    cnt;
    reg[64:0]   tmp_result;
    reg[31:0]   tmp_opdata2;
    wire[32:0]  minuend;

    assign minuend = {1'b0, tmp_result[63:32]} - {1'b0, tmp_opdata2};

    always @(posedge clk) begin
        if (rst == 1'b1) begin
            state <= `DIV_FREE;
            result_o <= `ZERO_DOUBLE_WORD;
            ready_o  <= 1'b0;
        end else begin
            case (state)
                `DIV_FREE: begin
                    if(start_i == 1'b1 && annul_i == 1'b0) begin
                        if(opdata2_i == `ZERO_WORD) begin
                            state <= `DIV_BY_ZERO;
                        end else begin
                            state <= `DIV_ON;
                            cnt   <= 6'b000000;
                            if(signed_div_i == 1'b1 && opdata1_i[31] == 1'b1) begin
                                tmp_result <= {`ZERO_WORD, ~opdata1_i + 1, 1'b0}; 
                            end else begin
                                tmp_result <= {`ZERO_WORD, opdata1_i, 1'b0}; 
                            end
                            if(signed_div_i == 1'b1 && opdata2_i[31] == 1'b1) begin
                                tmp_opdata2 = ~opdata2_i + 1;
                            end else begin
                                tmp_opdata2 = opdata2_i;
                            end                   
                        end
                    end else begin
                        state <= `DIV_FREE;
                        result_o <= `ZERO_WORD;
                        ready_o <= 1'b0;
                    end  // if opdata2_i
                end // `DIV_FREE
                `DIV_ON: begin
                    if(annul_i == 1'b1) begin
                        state <= `DIV_FREE;
                    end else begin
                        if(cnt != 6'b100000) begin
                            cnt <= cnt + 1;
                            if(minuend[32] == 1'b1) begin
                                tmp_result <= {tmp_result[63:0], 1'b0};
                            end else begin
                                tmp_result <= {minuend[31:0], tmp_result[31:0], 1'b1};
                            end
                        end else begin
                            if((signed_div_i == 1'b1) && ((opdata1_i[31] ^ opdata2_i[31]) == 1'b1)) begin
                                tmp_result[31:0] <= ~tmp_result[31:0] + 1;      // 若商为负数
                            end
                            if((signed_div_i == 1'b1) && ((opdata1_i[31] ^ tmp_result[64]) == 1'b1)) begin
                                tmp_result[64:33] <= ~tmp_result[64:33] + 1;    // 若余数为负数(当被除数为负数时)
                            end
                            state <= `DIV_END;
                        end
                    end
                end // `DIV_ON
                `DIV_END: begin
                    result_o <= {tmp_result[64:33], tmp_result[31:0]};
                    ready_o <= 1'b1; 
                    if(start_i == 1'b0) begin
                        state <= `DIV_FREE;
                        result_o <= `ZERO_DOUBLE_WORD;
                        ready_o <= 1'b0;
                    end
                end // `DIV_END
                `DIV_BY_ZERO: begin
                    state <= `DIV_END;
                    tmp_result <= {`ZERO_DOUBLE_WORD, 1'b0};
                end // `DIV_BY_ZERO
            endcase
        end
    end


endmodule // div