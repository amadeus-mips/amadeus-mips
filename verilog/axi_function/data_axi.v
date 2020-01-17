`include "../defines.v"

module data_axi(
    input wire          clk,
    input wire          resetn,
    input wire          flush,
    
    //from mem
    input  wire         mem_ren,
    input  wire         mem_wen,
    input  wire[3:0]    mem_wsel,
    input  wire[31:0]   mem_addr,
    input  wire[31:0]   mem_wdata,
    input  wire         cached_trans,
    input  wire[31:0]   ex_addr,
    //to mem
    output wire[31:0]   mem_rdata,
    output wire         mem_rvalid,
    output wire         mem_bvalid,

    //读
    //发送地址
    output wire[3:0]    d_arid,     //读数据发送的ID号 
    output wire[31:0]   d_araddr,   //读数据发送的地址
    output wire[3:0]    d_arlen,    //读数据发送的请求传输长度(数据传输拍数) 固定为0
    output wire[2:0]    d_arsize,   //读数据发送的请求传输大小(数据传输每拍的字节数)
    output wire[1:0]    d_arburst,  //读数据发送的请求传输类型  固定为2'b01
    output wire[1:0]    d_arlock,   //读数据发送的原子锁，固定为2'b01
    output wire[3:0]    d_arcache,  //读数据发送的CACHE属性 固定为4'b0000
    output wire[2:0]    d_arprot,   //读数据发送的保护属性 固定为3'b000
    output wire         d_arvalid,  //读数据发送地址握手信号，读数据发送地址有效
    //input wire          d_arready,  //读数据发送地址握手信号，slave端准备好接受地址传输

    //接收数据
    input wire[3:0]     d_rid,      // 读数据接收的slave端返回的id
    input wire[31:0]    d_rdata,    // 读数据接收的slave端返回的数据
    input wire[1:0]     d_rresp,    // 读数据接收的slave端返回的是否成功完成信号，可忽略
    input wire          d_rlast,    // 读数据接收的slave端返回的是否是最后一拍数据信号，可忽略（可能用于burst传输）
    input wire          d_rvalid,   // 读数据接收的slave端返回的数据是否有效信号
    //output reg          d_rready,   // 读数据发送的master端准备好接收数据传输的信号

    //写
    //发送地址
    output wire[3:0]    d_awid,     // 写数据发送的ID号
    output wire[31:0]   d_awaddr,   // 写数据发送的地址
    output wire[3:0]    d_awlen,    // 写数据发送的请求传输长度，固定为0
    output wire[2:0]    d_awsize,   // 写数据发送的请求传输大小，固定为3'b010(4Bytes)
    output wire[1:0]    d_awburst,  // 写数据发送的请求传输类型，固定为2'b01
    output wire[1:0]    d_awlock,   // 写数据发送的原子锁，固定为2'b00
    output wire[3:0]    d_awcache,  // 写数据发送的CACHE属性，固定为4'b0000
    output wire[2:0]    d_awprot,   // 写数据发送的保护属性，固定为3'b000
    output wire         d_awvalid,  // 写数据发送的地址握手信号
    //input  wire         d_awready,  // 写数据接收的slave端准备好接受地址的信号

    //发送数据
    output wire[3:0]    d_wid,      // 写数据发送的ID号
    output wire[31:0]   d_wdata,    // 写数据发送的数据
    output wire[3:0]    d_wstrb,    // 写数据发送的字节选通位
    output wire         d_wlast,    // 写数据发送的是否是最后一拍数据的信号，固定为1
    output wire         d_wvalid,   // 写数据发送的数据是否有效的信号
    input  wire         d_wready,   // 写数据接收的slave端准备好接收数据的信号

    //接收写响应
    input wire[3:0]     d_bid,      // 写数据的ID号
    input wire[1:0]     d_bresp,    // 写数据接收的写是否成功完成，可忽略
    input wire          d_bvalid  // 写数据接收的响应是否有效信号
    //output wire         d_bready,   // 写数据发送的master端准备号接收写响应的信号
);

    //wire    cached_trans = (mem_addr[31:29] == 3'b101) ?  1'b0 : 1'b1;   // 0xA0000000~0xBFFFFFFF 不可cache
    //wire    cached_trans = 1'b;
    wire[31:0]    cache_data_to_cpu;
    wire    hit;
    wire    miss;
    wire    cache_wresp;
    wire[31:0]    cache_waddr_to_axi;
    wire[31:0]    cache_wdata_to_axi;
    wire    cache_wvalid_to_axi;
    wire    cache_wlast_to_axi;
    wire    write_back;

    //读
    assign d_arid       = `AXI_DATA_Id;
    assign d_araddr     = (cached_trans == 1'b1)  ? {3'b0, mem_addr[28:5], 5'b00000} : {3'b0, mem_addr[28:0]};
    assign d_arlen      = (cached_trans == 1'b1)  ? 4'b0111 : 4'b0000;     // 8 transfers or 1 transfer
    assign d_arsize     = 3'b010;   // 4 Bytes
    assign d_arburst    = 2'b01;    // Incrementing-address burst
    assign d_arlock     = 2'b00;    
    assign d_arcache    = 4'b0000;
    assign d_arprot     = 3'b000;
    assign d_arvalid    = (cached_trans == 1'b1)  ? miss : mem_ren;

    assign mem_rdata    = (cached_trans == 1'b1)  ? cache_data_to_cpu : d_rdata;
    assign mem_rvalid   = (cached_trans == 1'b1)  ? hit : (d_rid == `AXI_DATA_Id) ? d_rvalid : 1'b0;

    //写
    assign d_awid       = `AXI_DATA_Id;
    assign d_awaddr     = (cached_trans == 1'b1)  ? {3'b0, cache_waddr_to_axi[28:0]} : {3'b0, mem_addr[28:0]};
    assign d_awlen      = (cached_trans == 1'b1)  ? 4'b0111 : 4'b0000;
    assign d_awsize     = 3'b010;
    assign d_awburst    = 2'b01;
    assign d_awlock     = 2'b00;
    assign d_awcache    = 4'b0000;
    assign d_awprot     = 3'b000;
    assign d_awvalid    = (cached_trans == 1'b1)  ? write_back : mem_wen;

    assign d_wid        = `AXI_DATA_Id;
    assign d_wdata      = (cached_trans == 1'b1)  ? cache_wdata_to_axi : mem_wdata;
    assign d_wstrb      = (cached_trans == 1'b1)  ? 4'b1111 : mem_wsel;
    assign d_wlast      = (cached_trans == 1'b1)  ? cache_wlast_to_axi :  1'b1;
    assign d_wvalid     = (cached_trans == 1'b1)  ? cache_wvalid_to_axi : mem_wen;

    assign mem_bvalid   = (cached_trans == 1'b1)  ? cache_wresp : (d_bid == `AXI_DATA_Id) ? d_bvalid : 1'b0;
    

    wire    cache_write_valid = (d_rid == `AXI_DATA_Id) & (d_rvalid) ;  // axi to cache
    wire    cache_ren = (cached_trans == 1'b1) ? mem_ren : 1'b0;
    wire    cache_wen = (cached_trans == 1'b1) ? mem_wen : 1'b0;      

    data_cache u_data_cache (
        .clk        (clk),
        .resetn     (resetn),
        .flush      (flush),

        .addr       (mem_addr),
        .addr_ren   (cache_ren),

        .addr_wdata (mem_wdata),
        .addr_wen   (cache_wen),
        .addr_wsel  (mem_wsel),
        .ex_addr    (ex_addr ),
        
        .data_o     (cache_data_to_cpu),
        .hit        (hit),
        .wresp      (cache_wresp),

        .axi_rdata  (d_rdata),
        .axi_rvalid (cache_write_valid),
        .axi_wready (d_wready),
        .axi_bvalid (d_bvalid),

        .miss       (miss),

        .write_back (write_back),
        .axi_waddr  (cache_waddr_to_axi),
        .axi_wdata  (cache_wdata_to_axi),
        .axi_wvalid (cache_wvalid_to_axi),
        .axi_wlast  (cache_wlast_to_axi)
    );


endmodule // data_axi