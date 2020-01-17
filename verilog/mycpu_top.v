`include "defines.v"

module mycpu_top(
    input wire[5:0]     int,
    //AXI时钟与复位信�?
    input wire          aclk,//AXI时钟
    input wire          aresetn,//AXI复位，低电平有效

    //读请求地�?通道�?(以ar�?�?)
    output wire[3:0]    arid,//读请求的ID�?  取指�?0 取数�?1
    output wire[31:0]   araddr,//读请求的地址
    output wire[3:0]    arlen,//读请求控制信号，请求传输的长�?(数据传输拍数) 固定�?0
    output wire[2:0]    arsize,//读请求控制信号，请求传输的大�?(数据传输每拍的字节数)
    output wire[1:0]    arburst,//读请求控制信号，传输类型  固定�?2’b01
    output wire[1:0]    arlock,//读请求控制信号，原子�?  固定�?0
    output wire[3:0]    arcache,//读请求控制信号，CACHE属�?? 固定�?0
    output wire[2:0]    arprot,//读请求控制信号，保护属�?? 固定�?0
    output wire         arvalid,//读请求地�?握手信号，读请求地址有效
    input wire          arready,//读请求地�?握手信号，slave端准备好接受地址传输

    //读请求数据�?�道�?(以r�?�?)
    input wire[3:0]     rid,//读请求的ID号，同一请求的rid应和arid�?�?  指令回来�?0数据 回来�?1
    input wire[31:0]    rdata,//读请求的读回数据
    input wire[1:0]     rresp,//读请求控制信号，本次读请求是否成功完�? 可忽�?
    input wire          rlast,//读请求控制信号，本次读请求的�?后一拍数据的指示信号 可忽�?
    input wire          rvalid,//读请求数据握手信号，读请求数据有�?
    output wire         rready,//读请求数据握手信号，master端准备好接受数据传输
    
    //写请求地�?通道�?(以aw�?�?)
    output wire[3:0]    awid,//写请求id 固定�?1
    output wire[31:0]   awaddr,//写请求地�?
    output wire[3:0]    awlen,//写请求控制信号，请求传输的长�?(数据传输拍数) 固定�?0
    output wire[2:0]    awsize,//写请求控制信号，请求传输的大�?(数据传输每拍的字节数)
    output wire[1:0]    awburst,//写请求控制信号，传输类型 固定�?2’b01
    output wire[1:0]    awlock,//写请求控制信号，原子�? 固定�?0
    output wire[3:0]    awcache,//写请求控制信号，CACHE属�?? 固定�?0
    output wire[2:0]    awprot,//写请求控制信号，保护属�?? 固定�?0
    output wire         awvalid,//写请求地�?握手信号，写请求地址有效
    input wire          awready,//写请求地�?握手信号，slave端准备好接受地址传输
    
    //写请求数据�?�道�?(以w�?�?)
    output wire[3:0]    wid, //写请求的ID�? 固定�?1
    output wire[31:0]   wdata, //写请求的写数�?
    output wire[3:0]    wstrb, //写请求控制信号，字节选�?�位
    output wire         wlast, //写请求控制信号，本次写请求的�?后一拍数据的指示信号 固定�?1
    output wire         wvalid, //写请求数据握手信号，写请求数据有�?
    input  wire         wready, //写请求数据握手信号，slave端准备好接受数据传输
    
    //写请求响应�?�道�?(以b�?�?)
    input wire[3:0]     bid,//写请求的ID号，同一请求的bid、wid和awid应一�?  可忽�?
    input wire[1:0]     bresp,//写请求控制信号，本次写请求是否成功完�?  可忽�?
    input wire          bvalid,//写请求响应握手信号，写请求响应有�?
    output wire         bready,//写请求响应握手信号，master端准备好接受写响�?

    //debug 
    output wire[31:0]   debug_wb_pc,
    output wire[3:0]    debug_wb_rf_wen,
    output wire[4:0]    debug_wb_rf_wnum,
    output wire[31:0]   debug_wb_rf_wdata
);

wire        flush;
//inst
wire[31:0]      pc_from_cpu;
wire            pc_en_from_cpu;
wire[31:0]      inst_from_axi_interface;
wire            inst_valid_from_axi_interface;

//data
wire        data_ren;
wire        data_wen;
wire[3:0]   data_wsel;
wire[31:0]  data_addr;
wire[31:0]  data_wdata;

wire[31:0]  data_rdata;
wire        data_rvalid;
wire        data_bvalid;
wire        cached_trans;
wire[31:0]  ex_data_addr;

axi_master_interface u_axi_master_interface(
	.aclk             (aclk             ),
    .aresetn          (aresetn          ),

    .arid             (arid             ),
    .araddr           (araddr           ),
    .arlen            (arlen            ),
    .arsize           (arsize           ),
    .arburst          (arburst          ),
    .arlock           (arlock           ),
    .arcache          (arcache          ),
    .arprot           (arprot           ),
    .arvalid          (arvalid          ),
    .arready          (arready          ),

    .rid              (rid              ),
    .rdata            (rdata            ),
    .rresp            (rresp            ),
    .rlast            (rlast            ),
    .rvalid           (rvalid           ),
    .rready           (rready           ),

    .awid             (awid             ),
    .awaddr           (awaddr           ),
    .awlen            (awlen            ),
    .awsize           (awsize           ),
    .awburst          (awburst          ),
    .awlock           (awlock           ),
    .awcache          (awcache          ),
    .awprot           (awprot           ),
    .awvalid          (awvalid          ),
    .awready          (awready          ),

    .wid              (wid              ),
    .wdata            (wdata            ),
    .wstrb            (wstrb            ),
    .wlast            (wlast            ),
    .wvalid           (wvalid           ),
    .wready           (wready           ),

    .bid              (bid              ),
    .bresp            (bresp            ),
    .bvalid           (bvalid           ),
    .bready           (bready           ),

    .flush            (flush),
    .pc_i             (pc_from_cpu             ),
    .pc_en_i          (pc_en_from_cpu            ),
    .inst_o           (inst_from_axi_interface             ),
    .inst_valid_o     (inst_valid_from_axi_interface       ),

    .data_ren         (data_ren         ),
    .data_wen         (data_wen         ),
    .data_wsel        (data_wsel        ),
    .data_addr        (data_addr        ),
    .data_wdata       (data_wdata       ),
    .cached_trans     (cached_trans),
    .ex_data_addr     (ex_data_addr),
    .data_rdata       (data_rdata       ),
    .data_rvalid      (data_rvalid      ),
    .data_bvalid      (data_bvalid      )
);



mycpu u_mycpu(
	.clk               (aclk               ),
    .resetn            (aresetn            ),
    .intr              (int               ),
    
    .pc_o              (pc_from_cpu              ),
    .pc_en             (pc_en_from_cpu             ),
    .inst              (inst_from_axi_interface              ),
    .inst_valid        (inst_valid_from_axi_interface        ),

    .data_axi_ren      (data_ren      ),
    .data_axi_wen      (data_wen     ),
    .data_axi_wsel     (data_wsel),
    .data_axi_addr     (data_addr    ),
    .data_axi_wdata    (data_wdata   ),
    .cached_trans      (cached_trans),
    .ex_data_addr      (ex_data_addr),

    .data_axi_rdata    (data_rdata   ),
    .data_axi_rvalid   (data_rvalid),
    .data_axi_bvalid   (data_bvalid),
    
    .axi_flush         (flush),

    .debug_wb_pc       (debug_wb_pc       ),
    .debug_wb_rf_wen   (debug_wb_rf_wen   ),
    .debug_wb_rf_wnum  (debug_wb_rf_wnum  ),
    .debug_wb_rf_wdata (debug_wb_rf_wdata )
);



endmodule