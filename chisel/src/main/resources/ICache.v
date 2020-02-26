module inst_cache(
    input wire          clk,
    input wire          resetn,
    input wire          flush,

    input wire[31:0]    addr,
    input wire          addr_en,

    output wire[31:0]    inst_o,
    output wire          hit,
    output wire          miss,

    //axi read data port     
    input wire[31:0]    axi_rdata,      
    input wire          axi_rvalid  
);

    ICache icache(
        .clock              (clk),
        .reset              (~resetn),
        .io_busData_valid   (axi_rvalid),
        .io_busData_bits    (axi_rdata),
        .io_flush           (flush),
        .io_addr_valid      (addr_en),
        .io_addr_bits       (addr),
        .io_inst            (inst_o),
        .io_hit             (hit),
        .io_miss            (miss)
    );

endmodule