`define IDLE            3'd0
`define LOOK_UP         3'd1
`define MISS_HANDLE     3'd2
`define CPU_WRITE       3'd3
`define WRITE_MISS_HANDLE 3'd4
`define GET_WRITE_DATA  3'd5
`define FINISH          3'd6

module data_cache(
    input wire          clk,
    input wire          resetn,
    input wire          flush,

    // from cpu--read
    input wire[31:0]    addr,
    input wire          addr_ren,

    // from cpu--write
    input wire[31:0]    addr_wdata,
    input wire          addr_wen,
    input wire[3:0]     addr_wsel,
    input wire[31:0]    ex_addr,
    // to cpu--read result
    output wire[31:0]   data_o,
    output wire         hit,        // read--cache hit
    output wire         wresp,      // write--cache hit and write to cache succeed

    // from axi--read data     
    input wire[31:0]    axi_rdata,      
    input wire          axi_rvalid,
    input wire          axi_wready,
    input wire          axi_bvalid,

    // to axi--read data
    output wire          miss,       // cache not hit, need to access memory

    // to axi--write data
    output reg          write_back, // repalced item need to write back
    output reg[31:0]    axi_waddr,  // address of write back
    output reg[31:0]    axi_wdata,
    output reg          axi_wvalid,
    output reg          axi_wlast

);
//-----------------reg ram-----------------------
    // valid
    reg[127:0] way0_v;
    reg[127:0] way1_v;

    reg[127:0] LRU;         // last be used way

    // dirty
    reg[127:0] way0_d;
    reg[127:0] way1_d;

    //reg[31:0] replace_buffer0;    // unused
    reg[31:0] replace_buffer1, replace_buffer2, replace_buffer3,
              replace_buffer4, replace_buffer5, replace_buffer6, replace_buffer7;
//----------------------------------------------

    // use addr to generate tag,index,offset
    wire[19:0]  tag   = addr[31:12];
    wire[6:0]   index = addr[11:5];
    wire[2:0]   bankoffset = addr[4:2]; //addr[1:0]==2'b00


    //wire[7:0]   bank_en;

    //bank input
    wire[3:0]   way0_wen0, way0_wen1, way0_wen2, way0_wen3,
                way0_wen4, way0_wen5, way0_wen6, way0_wen7; // wen && wsel--way0
    wire[3:0]   way1_wen0, way1_wen1, way1_wen2, way1_wen3,
                way1_wen4, way1_wen5, way1_wen6, way1_wen7; // wen && wsel--way1
    //tag input
    wire way0_tag_wen;  //tag1 write enable
    wire way1_tag_wen;  //tag2 write enable

    // bank output
    wire[31:0]  way0_dout0, way0_dout1, way0_dout2, way0_dout3, 
                way0_dout4, way0_dout5, way0_dout6, way0_dout7;//data from way0 bank0-7
    wire[31:0]  way1_dout0, way1_dout1, way1_dout2, way1_dout3,
                way1_dout4, way1_dout5, way1_dout6, way1_dout7;//data from way1 bank0-7

    reg[31:0]  way0_dout;//selected data for way0 from bank0-7
    reg[31:0]  way1_dout;//selected data for way1 from bank0-7

    wire[19:0]  way0_tag_dout, way1_tag_dout;

    reg       ex_change_LRU;
    reg       ex_hit, ex_wresp;
    reg[31:0] ex_data;
   
    // state regs
    reg[2:0]    state;
    reg[8:0]    cnt;   
    reg[3:0]    write_cnt;  // record the number of data written to axi 

    wire[31:0]  bank_wdata = (state == `IDLE) ? addr_wdata : axi_rdata;
    
    wire[19:0]  ex_tag = (state == `IDLE) ? ex_addr[31:12] : tag;
    wire[6:0]   ex_index = (state == `IDLE) ?  ex_addr[11:5] : index;
    wire[2:0]   ex_bankoffset = (state == `IDLE) ? ex_addr[4:2] : bankoffset;

    wire[6:0]   w_index = index;
    wire[6:0]   r_index = (state == `IDLE) ? ex_addr[11:5] : index;   
    
//-------------bank write enable signal--include select bytes----------------------------------------------------------------------------
    // way0
    assign way0_wen0 = (state == `MISS_HANDLE && (cnt[0] & axi_rvalid & ~LRU[index])) ? 4'b1111 : 
                        (state == `IDLE && (ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd0) ? addr_wsel : 4'd0;
    assign way0_wen1 = (state == `MISS_HANDLE && (cnt[1] & axi_rvalid & ~LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd1) ? addr_wsel : 4'd0;
    assign way0_wen2 = (state == `MISS_HANDLE && (cnt[2] & axi_rvalid & ~LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd2) ? addr_wsel : 4'd0;
    assign way0_wen3 = (state == `MISS_HANDLE && (cnt[3] & axi_rvalid & ~LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd3) ? addr_wsel : 4'd0;
    assign way0_wen4 = (state == `MISS_HANDLE && (cnt[4] & axi_rvalid & ~LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd4) ? addr_wsel : 4'd0;
    assign way0_wen5 = (state == `MISS_HANDLE && (cnt[5] & axi_rvalid & ~LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd5) ? addr_wsel : 4'd0;
    assign way0_wen6 = (state == `MISS_HANDLE && (cnt[6] & axi_rvalid & ~LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd6) ? addr_wsel : 4'd0;
    assign way0_wen7 = (state == `MISS_HANDLE && (cnt[7] & axi_rvalid & ~LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd7) ? addr_wsel : 4'd0;
    
    // way1
    assign way1_wen0 = (state == `MISS_HANDLE && (cnt[0] & axi_rvalid & LRU[index])) ? 4'b1111 : 
                        (state == `IDLE && (~ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd0) ? addr_wsel : 4'd0;
    assign way1_wen1 = (state == `MISS_HANDLE && (cnt[1] & axi_rvalid & LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (~ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd1) ? addr_wsel : 4'd0;
    assign way1_wen2 = (state == `MISS_HANDLE && (cnt[2] & axi_rvalid & LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (~ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd2) ? addr_wsel : 4'd0;
    assign way1_wen3 = (state == `MISS_HANDLE && (cnt[3] & axi_rvalid & LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (~ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd3) ? addr_wsel : 4'd0;
    assign way1_wen4 = (state == `MISS_HANDLE && (cnt[4] & axi_rvalid & LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (~ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd4) ? addr_wsel : 4'd0;
    assign way1_wen5 = (state == `MISS_HANDLE && (cnt[5] & axi_rvalid & LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (~ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd5) ? addr_wsel : 4'd0;
    assign way1_wen6 = (state == `MISS_HANDLE && (cnt[6] & axi_rvalid & LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (~ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd6) ? addr_wsel : 4'd0;
    assign way1_wen7 = (state == `MISS_HANDLE && (cnt[7] & axi_rvalid & LRU[index])) ? 4'b1111 :
                        (state == `IDLE && (~ex_change_LRU & addr_wen & ex_wresp) && bankoffset == 3'd7) ? addr_wsel : 4'd0;
//------------------------------------------------------------------------------------------------------------------------------------


    assign way0_tag_wen = (state == `MISS_HANDLE && (cnt[0] & axi_rvalid & ~LRU[index]));
    assign way1_tag_wen = (state == `MISS_HANDLE && (cnt[0] & axi_rvalid & LRU[index]));

    assign hit = (state == `IDLE && (addr_ren & ex_hit));
    assign data_o = ex_data;

    assign wresp = (state == `IDLE && (addr_wen & ex_wresp));

    assign miss =((state == `IDLE && 
                    (addr_ren & ~ex_hit & ((~LRU[index] & ~way0_d[index]) | (LRU[index] & ~way1_d[index]))) ||
                    (addr_wen & ~ex_wresp & ((~LRU[index] & ~way0_d[index]) | (LRU[index] & ~way1_d[index])))) ||
                  (state == `WRITE_MISS_HANDLE && write_cnt == 4'd8 && axi_bvalid) || 
                  (state == `MISS_HANDLE));                    
                    

//--------------------------------ex get tag in advance-------------------------------------------
    wire[6:0] tag_index = (state == `IDLE) ? ex_addr[11:5] : index;
    reg[19:0] ex_way0_tag, ex_way1_tag;
    //reg       ex_way0_v, ex_way1_v;
    //reg       ex_way0_d, ex_way1_d;
    //reg       ex_LRU;

    //reg       ex_miss, ex_write_back;
    wire hit_way0 = way0_v[tag_index] && way0_tag_dout == ex_tag;
    wire hit_way1 = way1_v[tag_index] && way1_tag_dout == ex_tag;

    always @(posedge clk) begin
        if(resetn == 1'b0) begin
            ex_way0_tag <= 20'd0;
            ex_way1_tag <= 20'd0;
            //ex_way0_v   <= 1'b0;
            //ex_way1_v   <= 1'b0;
            //ex_way0_d   <= 1'b0;
            //ex_way1_d   <= 1'b0;
            //ex_LRU      <= 1'b0;
            ex_hit      <= 1'b0;
            ex_wresp    <= 1'b0;
            ex_change_LRU <= 1'b0;
        end else begin
            ex_way0_tag <= way0_tag_dout;
            ex_way1_tag <= way1_tag_dout;
            //ex_way0_v   <= way0_v[tag_index];
            //ex_way1_v   <= way1_v[tag_index];                
            //ex_way0_d   <= way0_d[tag_index];
            //ex_way1_d   <= way1_d[tag_index];
            //ex_LRU      <= LRU[tag_index];
            ex_hit      <= hit_way0 | hit_way1;
            ex_wresp    <= hit_way0 | hit_way1;
            ex_change_LRU <= hit_way0;
            ex_data     <= hit_way0 ? way0_dout : way1_dout;
        end
    end
//-----------------------------------------------------------------------------------------------

    //drive lru and select way based on tag
    always @(posedge clk) begin
        if(resetn == 1'b0) begin
            LRU         <= 128'd0;
            cnt         <= 9'd0;
            state       <= 3'd0;

            way0_v      <= 128'd0;
            way1_v      <= 128'd0;
            way0_d      <= 128'd0;
            way1_d      <= 128'd0;   

            write_cnt   <= 4'd0;

            replace_buffer1 <= 32'd0;
            replace_buffer2 <= 32'd0;
            replace_buffer3 <= 32'd0;
            replace_buffer4 <= 32'd0;
            replace_buffer5 <= 32'd0;
            replace_buffer6 <= 32'd0;
            replace_buffer7 <= 32'd0;
            
            axi_waddr   <= 32'd0;
            axi_wdata   <= 32'd0;
            axi_wvalid  <= 1'b0;
            axi_wlast   <= 1'b0;
            write_back  <= 1'b0;

        end else begin
            case(state)
                `IDLE: begin
                    if(addr_ren == 1'b1) begin
                        if(ex_hit) begin
                            LRU[index]  <= ex_change_LRU;
                            state       <= `IDLE;
                        end else begin
                            if((LRU[index] == 1'b0 && way0_d[index] == 1'b1) || (LRU[index] == 1'b1 &&  way1_d[index] == 1'b1)) begin
                                state       <= `GET_WRITE_DATA;
                            end else begin
                                state   <= `MISS_HANDLE;
                                cnt     <= 9'd1;
                                write_back <= 1'b0;
                            end                
                        end
                    end else if(addr_wen == 1'b1) begin
                        if(ex_wresp) begin
                                LRU[index]  <= ex_change_LRU;
                                state       <= `IDLE;
                                if(ex_change_LRU == 1'b1) begin // write way0
                                    way0_d[index] <= 1'b1;
                                end else begin
                                    way1_d[index] <= 1'b1;
                                end                                
                            end else begin                                
                                if((LRU[index] == 1'b0 && way0_d[index] == 1'b1) || (LRU[index] == 1'b1 && way1_d[index] == 1'b1)) begin                                   
                                    state       <= `GET_WRITE_DATA;
                                end else begin
                                    state   <= `MISS_HANDLE;
                                    cnt     <= 9'd1;
                                    write_back  <= 1'b0;
                                end
                            end           
                    end else begin
                        state   <= `IDLE;
                    end
                    //hit <= 1'b0;
                end
                `GET_WRITE_DATA: begin
                    if(LRU[index] == 1'b0 && way0_d[index] == 1'b1) begin                                   
                        state       <= `WRITE_MISS_HANDLE;
                        cnt         <= 9'd0;
                        write_cnt   <= 4'd0;

                        replace_buffer1 <= way0_dout1;                                                                    
                        replace_buffer2 <= way0_dout2;
                        replace_buffer3 <= way0_dout3;
                        replace_buffer4 <= way0_dout4;
                        replace_buffer5 <= way0_dout5;
                        replace_buffer6 <= way0_dout6;
                        replace_buffer7 <= way0_dout7;

                        axi_waddr   <= {way0_tag_dout, index, 5'd0};
                        axi_wdata   <= way0_dout0;
                        axi_wvalid  <= 1'b1;
                        axi_wlast   <= 1'b0;
                        write_back  <= 1'b1;
                    end else if(LRU[index] == 1'b1 && way1_d[index] == 1'b1) begin                            
                        state       <= `WRITE_MISS_HANDLE;
                        cnt         <= 9'd0;
                        write_cnt   <= 4'd0;
                                
                        replace_buffer1 <= way1_dout1;
                        replace_buffer2 <= way1_dout2;
                        replace_buffer3 <= way1_dout3;
                        replace_buffer4 <= way1_dout4;
                        replace_buffer5 <= way1_dout5;
                        replace_buffer6 <= way1_dout6;
                        replace_buffer7 <= way1_dout7;

                        axi_waddr   <= {way1_tag_dout, index, 5'd0};
                        axi_wdata   <= way1_dout0;
                        axi_wvalid  <= 1'b1;
                        axi_wlast   <= 1'b0;
                        write_back  <= 1'b1;
                    end else begin
                        state <= `IDLE;
                    end
                end
                `WRITE_MISS_HANDLE: begin
                    if(write_cnt != 4'd8 && axi_wready == 1'b1) begin
                        write_cnt     <= write_cnt + 1;
                        state   <= state;
                        case (write_cnt)
                            4'd0: begin
                                axi_wdata <= replace_buffer1;                                                                        
                            end
                            4'd1: begin
                                axi_wdata <= replace_buffer2;
                            end
                            4'd2: begin
                                axi_wdata <= replace_buffer3;
                            end                
                            4'd3: begin
                                axi_wdata <= replace_buffer4;
                            end
                            4'd4: begin
                                axi_wdata <= replace_buffer5;
                            end
                            4'd5: begin
                                axi_wdata <= replace_buffer6;
                            end
                            4'd6: begin
                                axi_wdata <= replace_buffer7;
                                axi_wlast <= 1'b1;
                            end
                            4'd7: begin
                                axi_wvalid <= 1'b0;
                                write_back <= 1'b0;                                    
                            end
                            default: begin
                                write_cnt <= 4'd0;
                                state <= `IDLE;
                            end
                        endcase
                    end else if(write_cnt == 4'd8 && axi_bvalid == 1'b1) begin
                        state   <= `MISS_HANDLE;
                        cnt     <= 9'd1;
                        write_cnt <= 4'd0;
                        write_back <= 1'b0;
                    end
                end
                `MISS_HANDLE: begin
                    if(cnt < 9'b100000000 && axi_rvalid == 1'b1) begin
                        
                        //update state and cnt
                        cnt <= cnt << 1'b1;
                        state <= `MISS_HANDLE;
                        if(cnt == 9'b001000000) begin
                            //set valid & dirty
                            if(LRU[index] == 1'b0)begin//way 0 tag is target
                                way0_d[index] <= 1'b0;
                                way0_v[index] <= 1'b1;                                
                            end else begin//way 1 tag is target
                                way1_d[index] <= 1'b0;
                                way1_v[index] <= 1'b1;
                            end
                        end
                    end else if (cnt == 9'b100000000) begin 
                            //clear cnt and state
                        cnt <= 9'b000000000;
                        state <= `IDLE;
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

    //select result from way0 & way1 banks
    always @(*)begin
        case(ex_bankoffset)
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
        .clk    (clk),        
        .we     (way0_tag_wen),
        .a      (tag_index),
        .d      (tag),
        .spo    (way0_tag_dout)
    );

    inst_tag_dist_ram way1_tag (
        .clk    (clk),        
        .we     (way1_tag_wen),
        .a      (tag_index),
        .d      (tag),
        .spo    (way1_tag_dout)
    );
    
    // bank way0
    sealed_data_dist_ram way0_bank0(
        .clka   (clk),
        .wea    (way0_wen0),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way0_dout0)
    );
    sealed_data_dist_ram way0_bank1(
        .clka   (clk),
        .wea    (way0_wen1),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way0_dout1)
    );

    sealed_data_dist_ram way0_bank2(
        .clka   (clk),
        .wea    (way0_wen2),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way0_dout2)
    );

    sealed_data_dist_ram way0_bank3(
        .clka   (clk),
        .wea    (way0_wen3),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way0_dout3)
    );

    sealed_data_dist_ram way0_bank4(
        .clka   (clk),
        .wea    (way0_wen4),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way0_dout4)
    );

    sealed_data_dist_ram way0_bank5(
        .clka   (clk),
        .wea    (way0_wen5),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way0_dout5)
    );

    sealed_data_dist_ram way0_bank6(
        .clka   (clk),
        .wea    (way0_wen6),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way0_dout6)
    );
    sealed_data_dist_ram way0_bank7(
        .clka   (clk),
        .wea    (way0_wen7),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way0_dout7)
    );


    sealed_data_dist_ram way1_bank0(
        .clka   (clk),
        .wea    (way1_wen0),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way1_dout0)
    );

    // bank way1
    sealed_data_dist_ram way1_bank1(
        .clka   (clk),
        .wea    (way1_wen1),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way1_dout1)
    );
    sealed_data_dist_ram way1_bank2(
        .clka   (clk),
        .wea    (way1_wen2),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way1_dout2)
    );
    sealed_data_dist_ram way1_bank3(
        .clka   (clk),
        .wea    (way1_wen3),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way1_dout3)
    );
    sealed_data_dist_ram way1_bank4(
        .clka   (clk),
        .wea    (way1_wen4),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way1_dout4)
    );
    sealed_data_dist_ram way1_bank5(
        .clka   (clk),
        .wea    (way1_wen5),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way1_dout5)
    );
    sealed_data_dist_ram way1_bank6(
        .clka   (clk),
        .wea    (way1_wen6),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way1_dout6)
    );
    sealed_data_dist_ram way1_bank7(
        .clka   (clk),
        .wea    (way1_wen7),
        .waddr  (w_index),
        .raddr  (r_index),
        .dina   (bank_wdata),
        .douta  (way1_dout7)
    );

endmodule // inst_cache