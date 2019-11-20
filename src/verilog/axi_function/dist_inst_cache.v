`define IDLE            3'd0
`define LOOK_UP         3'd1
`define MISS_HANDLE     3'd2
`define REPLACE         3'd3
`define SELECT          3'd4
`define REFILL          3'd5
`define FINISH            3'd6

module inst_cache(
    input wire          clk,
    input wire          resetn,
    input wire          flush,

    input wire[31:0]    addr,
    input wire          addr_en,

    output wire[31:0]    inst_o,
    output wire          hit,
    output reg          miss,

    //axi read data port     
    input wire[31:0]    axi_rdata,      
    input wire          axi_rvalid  
);
    reg[127:0] way0_v;
    reg[127:0] way1_v;
    reg[127:0] LRU;         // last be used way

    // use addr to generate tag,index,offset
    wire[19:0]  tag   = addr[31:12];
    wire[6:0]   index = addr[11:5];
    //wire[4:0]   offset = addr[4:0];
    wire[2:0]   bankoffset = addr[4:2];//addr[1:0]==2'b00
    //wire[7:0]   bank_en;//

    //bank input
    wire[7:0]   way0_wen;//way0 bank0-7 write enable
    wire[7:0]   way1_wen;//way1 bank0-7 write enable
    
    //tag input
    wire way0_tag_wen;//tag1 write enable
    wire way1_tag_wen;//tag2 write enable
    
    //wire   tag_en;

    // bank output
    wire[31:0]  way0_dout0, way0_dout1, way0_dout2, way0_dout3, 
                way0_dout4, way0_dout5, way0_dout6, way0_dout7;//data from way0 bank0-7
    wire[31:0]  way1_dout0, way1_dout1, way1_dout2, way1_dout3,
                way1_dout4, way1_dout5, way1_dout6, way1_dout7;//data from way1 bank0-7

    reg[31:0]  way0_dout;//selected data for way0 from bank0-7
    reg[31:0]  way1_dout;//selected data for way1 from bank0-7

    wire[19:0]  way0_tag_dout, way1_tag_dout;
   
    // state regs
    reg[2:0]    state;
    reg[8:0]    cnt;   

    //assign bank_en[0] = (((state == `IDLE) && addr_en == 1'b1 && bankoffset == 3'd0)||( way0_wen[0] | way1_wen[0]==1'd1)) ? 1'b1 :1'b0;
    //assign bank_en[1] = (((state == `IDLE) && addr_en == 1'b1 && bankoffset == 3'd1)||( way0_wen[1] | way1_wen[1]==1'd1)) ? 1'b1 :1'b0;
    //assign bank_en[2] = (((state == `IDLE) && addr_en == 1'b1 && bankoffset == 3'd2)||( way0_wen[2] | way1_wen[2]==1'd1)) ? 1'b1 :1'b0;
    //assign bank_en[3] = (((state == `IDLE) && addr_en == 1'b1 && bankoffset == 3'd3)||( way0_wen[3] | way1_wen[3]==1'd1)) ? 1'b1 :1'b0;
    //assign bank_en[4] = (((state == `IDLE) && addr_en == 1'b1 && bankoffset == 3'd4)||( way0_wen[4] | way1_wen[4]==1'd1)) ? 1'b1 :1'b0;
    //assign bank_en[5] = (((state == `IDLE) && addr_en == 1'b1 && bankoffset == 3'd5)||( way0_wen[5] | way1_wen[5]==1'd1)) ? 1'b1 :1'b0;
    //assign bank_en[6] = (((state == `IDLE) && addr_en == 1'b1 && bankoffset == 3'd6)||( way0_wen[6] | way1_wen[6]==1'd1)) ? 1'b1 :1'b0;
    //assign bank_en[7] = (((state == `IDLE) && addr_en == 1'b1 && bankoffset == 3'd7)||( way0_wen[7] | way1_wen[7]==1'd1)) ? 1'b1 :1'b0;

    assign way0_wen = (state == `MISS_HANDLE && cnt < 9'b100000000 && axi_rvalid == 1'b1 && LRU[index] == 1'b0) ? cnt[7:0] : 8'd0;
    assign way1_wen = (state == `MISS_HANDLE && cnt < 9'b100000000 && axi_rvalid == 1'b1 && LRU[index] == 1'b1) ? cnt[7:0] : 8'd0;

    assign way0_tag_wen = (state == `MISS_HANDLE && cnt == 9'b000000001 && axi_rvalid == 1'b1 && LRU[index] == 1'b0) ? 1'b1 : 1'b0;
    assign way1_tag_wen = (state == `MISS_HANDLE && cnt == 9'b000000001 && axi_rvalid == 1'b1 && LRU[index] == 1'b1) ? 1'b1 : 1'b0;

    //assign tag_en = (((state == `IDLE) && addr_en == 1'b1)||(way0_tag_wen|way1_tag_wen == 1'b1)) ? 1'b1 : 1'b0;  
    //assign tag_en = 1'b1;
  
    assign hit    = (resetn == 1'b1 && flush == 1'b0 && state == `IDLE && addr_en == 1'b1 &&
                        ((way0_v[index] == 1'b1 && way0_tag_dout == tag) || 
                         (way1_v[index] == 1'b1 && way1_tag_dout == tag))) ? 1'b1 : 1'b0;
    assign inst_o = (hit == 1'b1) ? ((way0_v[index] == 1'b1 && way0_tag_dout == tag) ? way0_dout : way1_dout) : 32'd0;                        

    //drive lru and select way based on tag
    always @(posedge clk) begin
        if(resetn == 1'b0) begin
            miss        <= 1'b0;
            LRU         <= 128'd0;
            cnt         <= 9'd0;
            state       <= 3'd0;   
            way0_v      <= 128'd0;
            way1_v      <= 128'd0;         
        end else begin
            if(flush == 1'b1) begin
                state <= `IDLE;
                miss  <= 1'b0;
                cnt   <= 9'd0;
            end else begin
                case(state)
                    `IDLE: begin                        
                        if(addr_en == 1'b1) begin
                            if(way0_v[index] == 1'b1 && way0_tag_dout == tag) begin
                                miss        <= 1'b0;
                                LRU[index]  <= 1'b1;
                                state       <= `IDLE;
                            end else if(way1_v[index] == 1'b1 && way1_tag_dout == tag) begin
                                miss        <= 1'b0;
                                LRU[index]  <= 1'b0;
                                state       <= `IDLE;
                            end else begin
                                miss        <= 1'b1; 
                                state       <= `MISS_HANDLE;     
                                cnt         <= 9'd1;                      
                            end
                        end else begin
                            state   <= `IDLE;
                        end
                    end
                    `MISS_HANDLE:begin
                        if(cnt < 9'b100000000 && axi_rvalid == 1'b1) begin
                            
                            //update state and cnt
                            cnt <= cnt << 1'b1;
                            state <= `MISS_HANDLE;

                        end else if (cnt == 9'b100000000) begin 
                            
                            //set valid
                            if(LRU[index] == 1'b0)begin//way 0 tag is target
                                way0_v[index] <= 1'b1;
                            end else begin//way 1 tag is target
                                way1_v[index] <= 1'b1;
                            end

                            //clear cnt and state
                            cnt <= 9'b000000000;
                            state <= `IDLE;
                            miss <= 1'b0;
                        end else begin
                            
                            //keep state
                            cnt <= cnt;
                            state <= `MISS_HANDLE;
                        end
                    end
                    default:begin
                        state <= `IDLE;
                    end
                endcase
            end
        end
    end

    //select result from way0 & way1 banks
    always @(*)begin
        case(bankoffset)
            3'd0:begin
                way0_dout <= way0_dout0;
                way1_dout <= way1_dout0;
            end
            3'd1:begin
                way0_dout <= way0_dout1;
                way1_dout <= way1_dout1;
            end
            3'd2:begin
                way0_dout <= way0_dout2;
                way1_dout <= way1_dout2;
            end
            3'd3:begin
                way0_dout <= way0_dout3;
                way1_dout <= way1_dout3;
            end
            3'd4:begin
                way0_dout <= way0_dout4;
                way1_dout <= way1_dout4;
            end
            3'd5:begin
                way0_dout <= way0_dout5;
                way1_dout <= way1_dout5;
            end
            3'd6:begin
                way0_dout <= way0_dout6;
                way1_dout <= way1_dout6;
            end
            3'd7:begin
                way0_dout <= way0_dout7;
                way1_dout <= way1_dout7;
            end
        endcase
    end
    
    // tag
    inst_tag_dist_ram way0_tag (
        .clk   (clk),
        .we    (way0_tag_wen),
        .a  (index),
        .d   (tag),                 //输入
        .spo  (way0_tag_dout)       // 输出
    );

    inst_tag_dist_ram way1_tag (
        .clk   (clk),
        .we    (way1_tag_wen),
        .a  (index),
        .d   (tag),                 //输入
        .spo  (way1_tag_dout)       // 输出
    );
    
    // bank way0
    inst_bank_dist_ram way0_bank0(
        .clk   (clk),        
        .we    (way0_wen[0]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way0_dout0)          // 输出
    );
    inst_bank_dist_ram way0_bank1(
        .clk   (clk),        
        .we    (way0_wen[1]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way0_dout1)          // 输出
    );

    inst_bank_dist_ram way0_bank2(
        .clk   (clk),        
        .we    (way0_wen[2]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way0_dout2)          // 输出
    );

    inst_bank_dist_ram way0_bank3(
        .clk   (clk),        
        .we    (way0_wen[3]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way0_dout3)          // 输出
    );

    inst_bank_dist_ram way0_bank4(
        .clk   (clk),        
        .we    (way0_wen[4]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way0_dout4)          // 输出
    );

    inst_bank_dist_ram way0_bank5(
        .clk   (clk),        
        .we    (way0_wen[5]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way0_dout5)          // 输出
    );

    inst_bank_dist_ram way0_bank6(
        .clk   (clk),        
        .we    (way0_wen[6]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way0_dout6)          // 输出
    );
    inst_bank_dist_ram way0_bank7(
        .clk   (clk),        
        .we    (way0_wen[7]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way0_dout7)          // 输出
    );


    inst_bank_dist_ram way1_bank0(
        .clk   (clk),        
        .we    (way1_wen[0]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way1_dout0)          // 输出
    );

    // bank way1
    inst_bank_dist_ram way1_bank1(
        .clk   (clk),        
        .we    (way1_wen[1]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way1_dout1)          // 输出
    );
    inst_bank_dist_ram way1_bank2(
        .clk   (clk),        
        .we    (way1_wen[2]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way1_dout2)          // 输出
    );
    inst_bank_dist_ram way1_bank3(
        .clk   (clk),        
        .we    (way1_wen[3]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way1_dout3)          // 输出
    );
    inst_bank_dist_ram way1_bank4(
        .clk   (clk),        
        .we    (way1_wen[4]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way1_dout4)          // 输出
    );
    inst_bank_dist_ram way1_bank5(
        .clk   (clk),        
        .we    (way1_wen[5]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way1_dout5)          // 输出
    );
    inst_bank_dist_ram way1_bank6(
        .clk   (clk),        
        .we    (way1_wen[6]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way1_dout6)          // 输出
    );
    inst_bank_dist_ram way1_bank7(
        .clk   (clk),        
        .we    (way1_wen[7]),
        .a  (index),
        .d   (axi_rdata),           //输入
        .spo  (way1_dout7)          // 输出
    );

endmodule // inst_cache