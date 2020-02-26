module mycpu(
    input wire clk,
    input wire resetn,
    input wire[5:0] int,

    output wire[31:0]   pc_o,
    output wire         pc_en,
    input  wire[31:0]   inst,
    input  wire         inst_valid,

    output wire         data_axi_ren,
    output wire         data_axi_wen,
    output wire[3:0]    data_axi_wsel,
    output wire[31:0]   data_axi_addr,
    output wire[31:0]   data_axi_wdata,
    output wire         cached_trans,
    output wire[31:0]   ex_data_addr,

    input wire[31:0]    data_axi_rdata,
    input wire          data_axi_rvalid,
    input wire          data_axi_bvalid,
    
    output wire         axi_flush,
    //debug signals
    output wire [31:0] debug_wb_pc,
    output wire [3 :0] debug_wb_rf_wen,
    output wire [4 :0] debug_wb_rf_wnum,
    output wire [31:0] debug_wb_rf_wdata
);
    wire [31:0] pc;
    assign pc_o = {3'b0, pc[28:0]};

    Core_ls core_ls (
        .clock                      (clk),
        .reset                      (~resetn),
        .io_intr                    (int),
        .io_inst_valid              (inst_valid),
        .io_inst_bits               (inst),
        .io_pc_valid                (pc_en),
        .io_pc_bits                 (pc),
        .io_load_rValid             (data_axi_rvalid),
        .io_load_data               (data_axi_rdata),
        .io_load_enable             (data_axi_ren),
        .io_store_bValid            (data_axi_bvalid),
        .io_store_enable            (data_axi_wen),
        .io_store_sel               (data_axi_wsel),
        .io_store_data              (data_axi_wdata),
        .io_addr                    (data_axi_addr),
        .io_ls_ex_addr              (ex_data_addr),
        .io_ls_cached_trans         (cached_trans),
        .io_ls_flush                (axi_flush),
        .io_ls_debug_wbPC           (debug_wb_pc),
        .io_ls_debug_wbRegFileWEn   (debug_wb_rf_wen),
        .io_ls_debug_wbRegFileWNum  (debug_wb_rf_wnum),
        .io_ls_debug_wbRegFileWData (debug_wb_rf_wdata)
    );


endmodule