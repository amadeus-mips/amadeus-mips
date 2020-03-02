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
        .io_rInst_addr              (pc),
        .io_rInst_enable            (pc_en),
        .io_rInst_data              (inst),
        .io_rInst_valid             (inst_valid),
        .io_rData_addr              (data_axi_addr),
        .io_rData_valid             (data_axi_rvalid),
        .io_rData_enable            (data_axi_ren),
        .io_rData_data              (data_axi_rdata),
        .io_wData_addr              (),
        .io_wData_enable            (data_axi_wen),
        .io_wData_sel               (data_axi_wsel),
        .io_wData_data              (data_axi_wdata),
        .io_wData_valid             (data_axi_bvalid),
        .io_ls_ex_addr              (ex_data_addr),
        .io_ls_flush                (axi_flush),
        .io_ls_debug_wbPC           (debug_wb_pc),
        .io_ls_debug_wbRegFileWEn   (debug_wb_rf_wen),
        .io_ls_debug_wbRegFileWNum  (debug_wb_rf_wnum),
        .io_ls_debug_wbRegFileWData (debug_wb_rf_wdata)
    );


endmodule