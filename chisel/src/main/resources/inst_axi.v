`include "defines.h"

module inst_axi(
    input wire          clk,
    input wire          resetn,
    input wire          flush,
    
    //from pc
    input wire[31:0]    pc,
    input wire          pc_en,

    //to pc
    output wire[31:0]    inst,
    output wire          inst_valid,

    //发送地址
    output wire[3:0]    i_arid,     // 读指令发送的id
    output wire[31:0]   i_araddr,   // 读指令发送的地址
    output wire[3:0]    i_arlen,    // 读指令发送的请求传输长度（数据传输拍数），固定为0
    output wire[2:0]    i_arsize,   // 读指令发送的请求传输大小（每拍传输的字节数）
    output wire[1:0]    i_arburst,  // 读指令发送的请求传输类型，固定为2'b01
    output wire[1:0]    i_arlock,   // 读指令发送的原子锁，固定为2'b00
    output wire[3:0]    i_arcache,  // 读指令发送的cache属性，固定为4'b0000
    output wire[2:0]    i_arprot,   // 读指令发送的保护属性，固定为3'b000
    output wire         i_arvalid,  // 读指令发送的请求地址有效信号
    //input  wire         i_arready,  // 读指令接收的slave端准备好接受地址的信号

    //接收指令
    input wire[3:0]     i_rid,      // 读指令接收的slave端返回的id
    input wire[31:0]    i_rdata,    // 读指令接收的slave端返回的指令
    input wire[1:0]     i_rresp,    // 读指令接收的slave端返回的是否成功完成信号，可忽略
    input wire          i_rlast,     // 读指令接收的slave端返回的是否是最后一拍数据信号，可忽略（可能用于burst传输）
    input wire          i_rvalid  // 读指令接收的slave端返回的指令是否有效信号
    //output reg          i_rready,   // 读指令发送的master端准备好接收指令传输的信号

);

    wire hit;
    wire miss;
    wire cached_trans   = 1'b1;

    assign i_arid       = `AXI_INST_Id;
    assign i_araddr     = cached_trans ? {3'b000, pc[28:5], 5'b00000} : {3'b000, pc[28:0]};
    assign i_arlen      = cached_trans ? 4'b0111 : 4'b0000;      // 8 transfers or 1 transfer
    assign i_arsize     = 3'b010;       // 4Bytes
    assign i_arburst    = 2'b01;        // Incrementing-address burst
    assign i_arlock     = 2'b00;        // Normal access
    assign i_arcache    = 4'b0000;      // Noncacheable and nonbufferable
    assign i_arprot     = 3'b000;       // 与awprot有关，目前暂定3'b000
    assign i_arvalid    = flush ? 1'b0 : cached_trans ? miss : pc_en;

    assign inst_valid = cached_trans ? hit : 1'b0;

    wire cache_write_valid = (i_rid == `AXI_INST_Id) & (i_rvalid == 1'b1);
    inst_cache u_inst_cache (
        .clk        (clk),
        .resetn     (resetn),
        .flush      (flush),
        // in
        .addr       (pc),
        .addr_en    (pc_en),
        //out
        .inst_o     (inst),
        .hit        (hit),
        .miss       (miss),

        .axi_rdata  (i_rdata),
        .axi_rvalid (cache_write_valid)
    );

endmodule // inst_axi