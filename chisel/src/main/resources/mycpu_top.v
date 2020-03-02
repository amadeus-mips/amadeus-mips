module mycpu_top(
    input wire[5:0]     int, //硬件中断信号
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
    output wire         arvalid,//读请求地址握手信号，读请求地址有效
    input wire          arready,//读请求地址握手信号，slave端准备好接受地址传输

    //读请求数据通道，(以r开头)
    input wire[3:0]     rid,//读请求的ID号，同一请求的rid应和arid一致  指令回来为0数据 回来为1
    input wire[31:0]    rdata,//读请求的读回数据
    input wire[1:0]     rresp,//读请求控制信号，本次读请求是否成功完成 可忽略
    input wire          rlast,//读请求控制信号，本次读请求的最后一拍数据的指示信号 可忽略
    input wire          rvalid,//读请求数据握手信号，读请求数据有效
    output wire         rready,//读请求数据握手信号，master端准备好接受数据传输
    
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
    input  wire         wready, //写请求数据握手信号，slave端准备好接受数据传输
    
    //写请求响应通道，(以b开头)
    input wire[3:0]     bid,//写请求的ID号，同一请求的bid、wid和awid应一致  可忽略
    input wire[1:0]     bresp,//写请求控制信号，本次写请求是否成功完成  可忽略
    input wire          bvalid,//写请求响应握手信号，写请求响应有效
    output wire         bready,//写请求响应握手信号，master端准备好接受写响应

    //debug 
    output wire[31:0]   debug_wb_pc,
    output wire[3:0]    debug_wb_rf_wen,
    output wire[4:0]    debug_wb_rf_wnum,
    output wire[31:0]   debug_wb_rf_wdata
);

    CPUTop cpuTop(
        .clock    (aclk),
        .reset    (~aresetn),
        .io_intr  (int),
        .io_bus_axi_ar_id         (arid),
        .io_bus_axi_ar_addr       (araddr),
        .io_bus_axi_ar_len        (arlen),
        .io_bus_axi_ar_size       (arsize),
        .io_bus_axi_ar_burst      (arburst),
        .io_bus_axi_ar_lock       (arlock),
        .io_bus_axi_ar_cache      (arcache),
        .io_bus_axi_ar_prot       (arprot),
        .io_bus_axi_ar_valid      (arvalid),
        .io_bus_axi_ar_ready      (arready),
        .io_bus_axi_r_id          (rid),
        .io_bus_axi_r_data        (rdata),
        .io_bus_axi_r_resp        (rresp),
        .io_bus_axi_r_last        (rlast),
        .io_bus_axi_r_valid       (rvalid),
        .io_bus_axi_r_ready       (rready),
        .io_bus_axi_aw_id         (awid),
        .io_bus_axi_aw_addr       (awaddr),
        .io_bus_axi_aw_len        (awlen),
        .io_bus_axi_aw_size       (awsize),
        .io_bus_axi_aw_burst      (awburst),
        .io_bus_axi_aw_lock       (awlock),
        .io_bus_axi_aw_cache      (awcache),
        .io_bus_axi_aw_prot       (awprot),
        .io_bus_axi_aw_valid      (awvalid),
        .io_bus_axi_aw_ready      (awready),
        .io_bus_axi_w_id          (wid),
        .io_bus_axi_w_data        (wdata),
        .io_bus_axi_w_strb        (wstrb),
        .io_bus_axi_w_last        (wlast),
        .io_bus_axi_w_valid       (wvalid),
        .io_bus_axi_w_ready       (wready),
        .io_bus_axi_b_id          (bid),
        .io_bus_axi_b_resp        (bresp),
        .io_bus_axi_b_valid       (bvalid),
        .io_bus_axi_b_ready       (bready),
        .io_debug_wbPC            (debug_wb_pc),
        .io_debug_wbRegFileWEn    (debug_wb_rf_wen),
        .io_debug_wbRegFileWNum   (debug_wb_rf_wnum),
        .io_debug_wbRegFileWData  (debug_wb_rf_wdata)
    );


endmodule