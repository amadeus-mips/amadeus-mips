`include "defines.h"

module axi_master_interface(
    //AXI时钟与复位信号
    input wire          aclk,//AXI时钟
    input wire          aresetn,//AXI复位，低电平有效

    //读请求地址通道，(以ar开头)
    output wire[3:0]    arid,//读请求的ID号  取指为0 取数为1
    output wire[31:0]   araddr,//读请求的地址
    output wire[3:0]    arlen,//读请求控制信号，请求传输的长度(数据传输拍数) 固定为0
    output wire[2:0]    arsize,//读请求控制信号，请求传输的大小(数据传输每拍的字节数)
    output wire[1:0]    arburst,//读请求控制信号，传输类型  固定为2’b01
    output wire[1:0]    arlock,//读请求控制信号，原子锁  固定为0
    output wire[3:0]    arcache,//读请求控制信号，CACHE属性 固定为0
    output wire[2:0]    arprot,//读请求控制信号，保护属性 固定为0
    output wire          arvalid,//读请求地址握手信号，读请求地址有效
    input wire          arready,//读请求地址握手信号，slave端准备好接受地址传输

    //读请求数据通道，(以r开头)
    input wire[3:0]     rid,//读请求的ID号，同一请求的rid应和arid一致  指令回来为0数据 回来为1
    input wire[31:0]    rdata,//读请求的读回数据
    input wire[1:0]     rresp,//读请求控制信号，本次读请求是否成功完成 可忽略
    input wire          rlast,//读请求控制信号，本次读请求的最后一拍数据的指示信号 可忽略
    input wire          rvalid,//读请求数据握手信号，读请求数据有效
    output reg          rready,//读请求数据握手信号，master端准备好接受数据传输
    
    //写请求地址通道，(以aw开头)
    output wire[3:0]    awid,//写请求id 固定为1
    output wire[31:0]   awaddr,//写请求地址
    output wire[3:0]    awlen,//写请求控制信号，请求传输的长度(数据传输拍数) 固定为0
    output wire[2:0]    awsize,//写请求控制信号，请求传输的大小(数据传输每拍的字节数)
    output wire[1:0]    awburst,//写请求控制信号，传输类型 固定为2’b01
    output wire[1:0]    awlock,//写请求控制信号，原子锁 固定为0
    output wire[3:0]    awcache,//写请求控制信号，CACHE属性 固定为0
    output wire[2:0]    awprot,//写请求控制信号，保护属性 固定为0
    output wire         awvalid,//写请求地址握手信号，写请求地址有效
    input wire          awready,//写请求地址握手信号，slave端准备好接受地址传输
    
    //写请求数据通道，(以w开头)
    output wire[3:0]    wid, //写请求的ID号 固定为1
    output wire[31:0]   wdata, //写请求的写数据
    output wire[3:0]    wstrb, //写请求控制信号，字节选通位
    output wire         wlast, //写请求控制信号，本次写请求的最后一拍数据的指示信号 固定为1
    output wire         wvalid, //写请求数据握手信号，写请求数据有效
    input  wire    wready, //写请求数据握手信号，slave端准备好接受数据传输
    
    //写请求响应通道，(以b开头)
    input wire[3:0]     bid,//写请求的ID号，同一请求的bid、wid和awid应一致  可忽略
    input wire[1:0]     bresp,//写请求控制信号，本次写请求是否成功完成  可忽略
    input wire          bvalid,//写请求响应握手信号，写请求响应有效
    output reg         bready,//写请求响应握手信号，master端准备好接受写响应

    //连接mycpu
    input wire          flush,
    //inst
    input wire[31:0]    pc_i,
    input wire          pc_en_i,
    output wire[31:0]   inst_o,
    output wire         inst_valid_o,   

    //data
    input wire          data_ren,
    input wire          data_wen,
    input wire[3:0]     data_wsel,
    input wire[31:0]    data_addr,
    input wire[31:0]    data_wdata,
    input wire          cached_trans,
    input wire[31:0]    ex_data_addr,
    output wire[31:0]   data_rdata,
    output wire         data_rvalid,
    output wire         data_bvalid
);

//读（指令或数据）
//reg[1:0]        axi_wait;   // [1] inst
                            // [0] data
//reg[1:0]        axi_ar_finish;   // 同上
//reg[1:0]        axi_r_finish;   // 同上

reg[2:0]        r_state;   // see defines.v


wire[3:0]       inst_arid;
wire[31:0]      inst_araddr;
wire[3:0]       inst_arlen;
wire[2:0]       inst_arsize;
wire[1:0]       inst_arburst;
wire[1:0]       inst_arlock;
wire[3:0]       inst_arcache;
wire[2:0]       inst_arprot;
wire            inst_arvalid;

wire[3:0]       data_arid;
wire[31:0]      data_araddr;
wire[3:0]       data_arlen;
wire[2:0]       data_arsize;
wire[1:0]       data_arburst;
wire[1:0]       data_arlock;
wire[3:0]       data_arcache;
wire[2:0]       data_arprot;
wire            data_arvalid;

reg         arvalid_tmp;    // 用于状态机
reg[1:0]    cnt;
reg         cnt_en;
wire        rvalid_s = (flush != 1'b1 && r_state == `R_WAIT && rvalid == 1'b1 && rready == 1'b1 && cnt_en == 1'b1 && cnt != 2'b01) ? 1'b0 : (rvalid & rready);

//仲裁（总是先处理读指令）
wire     i_d_arvalid_tmp = (inst_arvalid == 1'b1) ? inst_arvalid : data_arvalid;

assign arid     = (inst_arvalid == 1'b1) ? inst_arid    : data_arid;
assign araddr   = (inst_arvalid == 1'b1) ? inst_araddr  : data_araddr;
assign arlen    = (inst_arvalid == 1'b1) ? inst_arlen   : data_arlen;
assign arsize   = (inst_arvalid == 1'b1) ? inst_arsize  : data_arsize;
assign arburst  = (inst_arvalid == 1'b1) ? inst_arburst : data_arburst;
assign arlock   = (inst_arvalid == 1'b1) ? inst_arlock  : data_arlock;
assign arcache  = (inst_arvalid == 1'b1) ? inst_arcache : data_arcache;
assign arprot   = (inst_arvalid == 1'b1) ? inst_arprot  : data_arprot;
assign arvalid  = (flush == 1'b1) ? 1'b0 : 
                    (r_state == `R_IDLE) ? i_d_arvalid_tmp : arvalid_tmp;



always @(posedge aclk) begin
    if(aresetn == 1'b0) begin        
        r_state <= `R_IDLE;
        arvalid_tmp <= 1'b0;
        rready <= 1'b0;
        cnt     <= 2'b00;
        cnt_en  <= 1'b0;
    end else begin
        if(flush == 1'b1) begin            
            r_state <= `R_IDLE;
            arvalid_tmp <= 1'b0;
            rready <= 1'b0;
            cnt_en  <= 1'b1;
        end else begin
            case (r_state)
                `R_IDLE: begin                   
                    if(arvalid == 1'b1 && arready == 1'b0) begin
                        r_state <= `AR_WAIT;
                        arvalid_tmp <= 1'b1;
                    end else if(arvalid == 1'b1 && arready == 1'b1) begin
                        r_state <= `AR_FINISH;
                        cnt     <= cnt + 1;
                        arvalid_tmp <= 1'b0;
                    end else begin
                        r_state <= r_state;
                    end
                end
                `AR_WAIT: begin                    
                    if(arvalid_tmp == 1'b1 && arready == 1'b1) begin
                        r_state <= `AR_FINISH;
                        cnt     <= cnt + 1;
                        arvalid_tmp <= 1'b0;
                    end else begin
                        r_state <= r_state;             
                    end
                end
                `AR_FINISH: begin                    
                    r_state <= `R_WAIT;
                    rready <= 1'b1;
                end
                `R_WAIT: begin
                    if(rvalid == 1'b1 && rready == 1'b1) begin
                        if(cnt_en == 1'b1 && cnt != 2'b01) begin
                            r_state <= r_state;
                            rready <= 1'b1;
                            if(rlast == 1'b1) begin                                                        
                                cnt    <= cnt - 1;  
                            end
                        end else if(rlast != 1'b1) begin
                            r_state <= r_state;
                            rready <= 1'b1;                            
                        end else begin                                    
                            r_state <= `R_FINISH;
                            cnt     <= 2'b00;
                            rready <= 1'b0;
                        end
                    end else begin
                        r_state <= r_state;                        
                    end
                end
                `R_FINISH: begin
                    r_state <= `R_IDLE;
                    cnt_en <= 1'b0;
                end
                default: begin                    
                    r_state <= r_state;
                end
            endcase
        end
    end
end

//写数据
reg[2:0]        w_state;

wire[3:0]       data_awid;
wire[31:0]      data_awaddr;
wire[3:0]       data_awlen;
wire[2:0]       data_awsize;
wire[1:0]       data_awburst;
wire[1:0]       data_awlock;
wire[3:0]       data_awcache;
wire[2:0]       data_awprot;
wire            data_awvalid;

wire[3:0]       data_wid;
wire[31:0]      data_wdata1;
wire[3:0]       data_wstrb;
wire            data_wlast;
wire            data_wvalid;

// 用于状态机
reg     awvalid_tmp;
reg     wvalid_tmp;

assign awid     = data_awid;
assign awaddr   = data_awaddr;
assign awlen    = data_awlen;
assign awsize   = data_awsize;
assign awburst  = data_awburst;
assign awlock   = data_awlock;
assign awcache  = data_awcache;
assign awprot   = data_awprot;
assign awvalid  = (w_state == `W_IDLE) ? data_awvalid : awvalid_tmp;

assign wid      = data_wid;
assign wdata    = data_wdata1;
assign wstrb    = data_wstrb;
assign wlast    = data_wlast;
assign wvalid   = wvalid_tmp & data_wvalid;

//先传地址再传数据，可改进
always @(posedge aclk) begin
    if(aresetn == 1'b0) begin
        w_state <= `W_IDLE;
        awvalid_tmp <= 1'b0;
        wvalid_tmp  <= 1'b0;
        bready <= 1'b0;
    end else begin
        case (w_state)
            `W_IDLE: begin
                if(data_awvalid == 1'b1 && awready == 1'b0) begin
                    w_state     <= `AW_WAIT;
                    awvalid_tmp <= 1'b1;
                    wvalid_tmp  <= 1'b0;
                end else if(data_awvalid == 1'b1 && awready == 1'b1) begin
                    w_state     <= `AW_FINISH;
                    awvalid_tmp <= 1'b0;
                    wvalid_tmp  <= 1'b0;
                end else begin
                    w_state     <= w_state;
                    awvalid_tmp <= 1'b0;
                    wvalid_tmp  <= 1'b0;                    
                end
            end 
            `AW_WAIT: begin
                if(awvalid_tmp == 1'b1 && awready == 1'b1) begin
                    w_state     <= `AW_FINISH;
                    awvalid_tmp <= 1'b0;
                end else begin
                    w_state     <= w_state;
                end
            end
            `AW_FINISH: begin
                w_state <= `W_WAIT;
                wvalid_tmp  <= 1'b1;
            end
            `W_WAIT: begin
                if(wvalid_tmp == 1'b1 && wready == 1'b1) begin
                    if(data_wlast == 1'b1) begin
                        w_state <= `W_FINISH;
                        wvalid_tmp  <= 1'b0;
                    end else begin
                        w_state <= w_state;
                        wvalid_tmp  <= 1'b1;
                    end
                end else begin
                    w_state <= w_state;
                end
            end
            `W_FINISH: begin
                w_state <= `B_WAIT;
                bready  <= 1'b1;
            end
            `B_WAIT: begin
                if(bready == 1'b1 && bvalid == 1'b1) begin
                    w_state <= `W_IDLE;
                    bready  <= 1'b0;
                end else begin
                    w_state <= w_state;
                end
            end
            default: begin
                w_state <= w_state;
            end
        endcase
    end
end


inst_axi u_inst_axi(
    .clk           (aclk),
	.resetn        (aresetn           ),
    .flush         (flush),
    .pc            (pc_i            ),
    .pc_en         (pc_en_i        ),
    .inst          (inst_o          ),
    .inst_valid    (inst_valid_o  ),

    .i_arid        (inst_arid        ),
    .i_araddr      (inst_araddr      ),
    .i_arlen       (inst_arlen       ),
    .i_arsize      (inst_arsize      ),
    .i_arburst     (inst_arburst     ),
    .i_arlock      (inst_arlock      ),
    .i_arcache     (inst_arcache     ),
    .i_arprot      (inst_arprot      ),
    .i_arvalid     (inst_arvalid     ),

    .i_rid         (rid         ),
    .i_rdata       (rdata       ),
    .i_rresp       (rresp       ),
    .i_rlast       (rlast       ),
    .i_rvalid      (rvalid_s      )
);

data_axi u_data_axi(
    .clk        (aclk),
	.resetn     (aresetn     ),
    .flush      (flush),
    // connect to cpu
    .mem_ren    (data_ren    ),
    .mem_wen    (data_wen    ),
    .mem_wsel   (data_wsel   ),
    .mem_addr   (data_addr   ),
    .mem_wdata  (data_wdata  ),
    .cached_trans (cached_trans),
    .ex_addr   (ex_data_addr),
    .mem_rdata  (data_rdata  ),
    .mem_rvalid (data_rvalid ),
    .mem_bvalid (data_bvalid ),

    .d_arid     (data_arid     ),
    .d_araddr   (data_araddr   ),
    .d_arlen    (data_arlen    ),
    .d_arsize   (data_arsize   ),
    .d_arburst  (data_arburst  ),
    .d_arlock   (data_arlock   ),
    .d_arcache  (data_arcache  ),
    .d_arprot   (data_arprot   ),
    .d_arvalid  (data_arvalid  ),

    .d_rid      (rid      ),
    .d_rdata    (rdata    ),
    .d_rresp    (rresp    ),
    .d_rlast    (rlast    ),
    .d_rvalid   (rvalid & rready   ),

    .d_awid     (data_awid     ),
    .d_awaddr   (data_awaddr   ),
    .d_awlen    (data_awlen    ),
    .d_awsize   (data_awsize   ),
    .d_awburst  (data_awburst  ),
    .d_awlock   (data_awlock   ),
    .d_awcache  (data_awcache  ),
    .d_awprot   (data_awprot   ),
    .d_awvalid  (data_awvalid  ),

    .d_wid      (data_wid      ),
    .d_wdata    (data_wdata1    ),
    .d_wstrb    (data_wstrb    ),
    .d_wlast    (data_wlast    ),
    .d_wvalid   (data_wvalid   ),
    .d_wready   (wready & wvalid),

    .d_bid      (bid      ),
    .d_bresp    (bresp    ),
    .d_bvalid   (bvalid   )
);



endmodule