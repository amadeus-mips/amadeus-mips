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
    output wire          write_back, // repalced item need to write back
    output wire[31:0]    axi_waddr,  // address of write back
    output wire[31:0]    axi_wdata,
    output wire          axi_wvalid,
    output wire          axi_wlast
);

    DCache dcache(
        .clock              (clk),
        .reset              (~resetn),
        .io_bus_rData_valid (axi_rvalid),
        .io_bus_rData_bits  (axi_rdata),
        .io_bus_wReady      (axi_wready),
        .io_bus_bValid      (axi_bvalid),
        .io_flush           (flush),
        .io_cpu_addr        (addr),
        .io_cpu_ren         (addr_ren),
        .io_cpu_wen         (addr_wen),
        .io_cpu_wData       (addr_wdata),
        .io_cpu_wSel        (addr_wsel),
        .io_cpu_exeAddr     (ex_addr),
        .io_cpu_data        (data_o),
        .io_hit             (hit),
        .io_wResp           (wresp),
        .io_miss            (miss),
        .io_bus_writeBack   (write_back),
        .io_bus_wAddr       (axi_waddr),
        .io_bus_wData       (axi_wdata),
        .io_bus_wValid      (axi_wvalid),
        .io_bus_wLast       (axi_wlast)
    );
endmodule