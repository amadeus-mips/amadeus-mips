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
wire rst = ~resetn;
wire[5:0]   stall;
wire        flush;

assign axi_flush = flush;

wire[31:0]  new_pc;
//branch control
wire        branch_flag_from_id;
wire[31:0]  branch_target_address_from_id;     
//pc
wire[31:0] pc_if;
assign pc_o = pc_if;
//except
wire if_excepttype_from_pc;
//inst from pc
wire[31:0] inst_from_pc;
wire stallreq_from_pc;
inst_fetch u_inst_fetch (
    .clk               (clk),
    .rst               (rst),

    //information from ctrl module
	.stall                      (stall),
    .flush                      (flush),
    .new_pc                     (new_pc),

    //information from id module
	.branch_flag_i              (branch_flag_from_id),
    .branch_target_address_i    (branch_target_address_from_id),

    .if_excepttype_o            (if_excepttype_from_pc),
    .pc                         (pc_if),
    .pc_en                      (pc_en),
    .inst_i                     (inst),
    .inst_valid                 (inst_valid),
    .inst_o                     (inst_from_pc),
    .stallreq                   (stallreq_from_pc)
);


//if_id
wire[31:0] id_pc_ifid;
wire[31:0] id_inst_ifid;
wire       if_excepttype_from_if_id;
if_id u_if_id(
	.clk     (clk     ),
    .rst     (rst     ),
    .stall   (stall   ),
    .flush   (flush   ),
    .if_excepttype_i (if_excepttype_from_pc),
    .if_pc   (pc_if   ),
    .if_inst (inst_from_pc ),
    .if_excepttype_o (if_excepttype_from_if_id),
    .id_pc   (id_pc_ifid   ),
    .id_inst (id_inst_ifid )
);


//id
//处于执行阶段的指令的一些信息，用于解决load相关
wire[7:0]       alu_op_from_ex;

//处于执行阶段的指令要写入的目的寄存器信息
wire[4:0]       write_addr_from_ex;
wire            write_reg_from_ex;
wire[31:0]      write_data_from_ex;
//处于访存阶段的指令要写入的目的寄存器信息
wire            write_reg_from_mem;
wire[31:0]      write_data_from_mem;
wire[4:0]       write_address_from_mem;

//read from reg file
wire[31:0]      reg1_data_id;
wire[31:0]      reg2_data_id;

wire            reg1_read_id;
wire            reg2_read_id;
wire[4:0]       reg1_addr_id;
wire[4:0]       reg2_addr_id;

//informations to ex
wire[7:0]       aluop_id;
wire[2:0]       alusel_id;
wire[31:0]      reg1_id;
wire[31:0]      reg2_id;
wire[4:0]      write_addr_id;
wire            write_reg_id;
wire[31:0]      inst_id;
wire            next_inst_in_delayslot_id;

//branch control
wire[31:0]      link_addr_id;
wire            is_in_delayslot_from_id_ex;
wire            is_in_delayslot_to_id_ex;

//stall
wire            stallreq_from_id;
wire[31:0]      excepttype_id;
wire[31:0]      current_inst_address_id;  

//from ex




id u_id(
	.rst                      (rst                          ),
    .pc_i                     (id_pc_ifid                   ),
    .inst_i                   (id_inst_ifid                 ),
    .ex_aluop_i               (alu_op_from_ex                ),
    .ex_wreg_i                (write_reg_from_ex           ),
    .ex_wdata_i               (write_data_from_ex          ),
    .ex_wd_i                  (write_addr_from_ex           ),
    .mem_wreg_i               (write_reg_from_mem           ),
    .mem_wdata_i              (write_data_from_mem          ),
    .mem_wd_i                 (write_address_from_mem          ),
    .reg1_data_i              (reg1_data_id               ),
    .reg2_data_i              (reg2_data_id               ),
    .is_in_delayslot_i        (is_in_delayslot_from_id_ex         ),
    .if_excepttype_i          (if_excepttype_from_if_id),
    .reg1_read_o              (reg1_read_id               ),
    .reg2_read_o              (reg2_read_id               ),
    .reg1_addr_o              (reg1_addr_id               ),
    .reg2_addr_o              (reg2_addr_id               ),
    .aluop_o                  (aluop_id                   ),
    .alusel_o                 (alusel_id                  ),
    .reg1_o                   (reg1_id                    ),
    .reg2_o                   (reg2_id                    ),
    .wd_o                     (write_addr_id              ),
    .wreg_o                   (write_reg_id               ),
    .inst_o                   (inst_id                    ),
    .next_inst_in_delayslot_o (next_inst_in_delayslot_id  ),
    .branch_flag_o            (branch_flag_from_id             ),
    .branch_target_address_o (branch_target_address_from_id  ),
    .link_addr_o              (link_addr_id               ),
    .is_in_delayslot_o        (is_in_delayslot_to_id_ex   ),
    .stallreq                 (stallreq_from_id        ),
    .excepttype_o             (excepttype_id),
    .current_inst_address_o   (current_inst_address_id)
);
//from mem/wb
wire        wreg_from_mem_wb;
wire[4:0]   wd_from_mem_wb;
wire[31:0]  wdata_from_mem_wb;

regfile u_regfile(
	.clk    (clk    ),
    .rst    (rst    ),
    .we     (wreg_from_mem_wb     ),
    .waddr  (wd_from_mem_wb  ),
    .wdata  (wdata_from_mem_wb  ),
    .re1    (reg1_read_id),
    .raddr1 (reg1_addr_id ),
    .rdata1 (reg1_data_id),
    .re2    (reg2_read_id    ),
    .raddr2 (reg2_addr_id ),
    .rdata2 (reg2_data_id )
);

wire[7:0]   aluop_to_ex;
wire[2:0]   alusel_to_ex;
wire[31:0]  reg1_to_ex;
wire[31:0]  reg2_to_ex;
wire[4:0]   wd_to_ex;
wire        wreg_to_ex;
wire[31:0]  link_address_to_ex;
wire        is_in_delayslot_to_ex;
wire[31:0]  inst_to_ex;
wire[31:0]  excepttype_to_ex;
wire[31:0]  current_inst_address_to_ex;

//id_ex
id_ex u_id_ex(
	.clk                      (clk                      ),
    .rst                      (rst                      ),
    .stall                    (stall                    ),
    .flush                    (flush                    ),
    .id_aluop                 (aluop_id                 ),
    .id_alusel                (alusel_id                ),
    .id_reg1                  (reg1_id                  ),
    .id_reg2                  (reg2_id                  ),
    .id_wd                    (write_addr_id                    ),
    .id_wreg                  (write_reg_id                  ),
    .id_link_address          (link_addr_id          ),
    .id_is_in_delayslot       (is_in_delayslot_to_id_ex       ),
    .next_inst_in_delayslot_i (next_inst_in_delayslot_id ),
    .id_inst                  (inst_id                  ),
    .id_current_inst_address  (current_inst_address_id  ),
    .id_excepttype            (excepttype_id            ),
    .ex_aluop                 (aluop_to_ex                 ),
    .ex_alusel                (alusel_to_ex                ),
    .ex_reg1                  (reg1_to_ex                 ),
    .ex_reg2                  (reg2_to_ex                  ),
    .ex_wd                    (wd_to_ex                    ),
    .ex_wreg                  (wreg_to_ex                  ),
    .ex_link_address          (link_address_to_ex          ),
    .ex_is_in_delayslot       (is_in_delayslot_to_ex       ),
    .is_in_delayslot_o        (is_in_delayslot_from_id_ex        ),
    .ex_inst                  (inst_to_ex                 ),
    .ex_excepttype            (excepttype_to_ex            ),
    .ex_current_inst_address  (current_inst_address_to_ex  )
);

//ex to ex_mem
wire        cp0_reg_we_from_ex;
wire[4:0]   cp0_reg_write_addr_from_ex;
wire[31:0]  cp0_reg_data_from_ex;
wire        whilo_from_ex;
wire[31:0]  hi_from_ex;
wire[31:0]  lo_from_ex;
wire        is_in_delayslot_from_ex;
wire[31:0]  excepttype_from_ex;
wire[31:0]  current_inst_address_from_ex;
wire[31:0]  mem_addr_from_ex;
wire[31:0]  data_to_mem_from_ex;
wire        l_op_from_ex;
wire        s_op_from_ex;
wire        cached_trans_from_ex;

assign      ex_data_addr = mem_addr_from_ex;         // ex级获取tag及其它信息

//cp0 and ex
wire[31:0]  cp0_reg_data_to_ex;
wire[4:0]   ex_cp0_reg_read_addr_to_cp0;
//hilo and ex
wire[31:0]  hi_to_ex;
wire[31:0]  lo_to_ex;
//div and ex
wire[63:0]  result_from_div;
wire        ready_from_div;
wire        signed_div_from_ex;
wire[31:0]  opdata1_from_ex;
wire[31:0]  opdata2_from_ex;
wire        start_from_ex;

//from mem_wb
wire cp0_reg_we_from_mem_wb;
wire[4:0] cp0_reg_write_addr_from_mem_wb;
wire[31:0] cp0_reg_data_from_mem_wb;
wire [31:0] hi_from_mem_wb;
wire [31:0] lo_from_mem_wb;
wire whilo_from_mem_wb;

//from mem
wire        cp0_reg_we_from_mem;
wire[4:0]   cp0_reg_write_addr_from_mem;
wire[31:0]  cp0_reg_data_from_mem;
wire[31:0]  hi_from_mem;
wire[31:0]  lo_from_mem;
wire        whilo_from_mem;

//ex to ctrl
wire stallreq_from_ex;


ex_top u_ex_top(
	.rst                    (rst                    ),
    .clk                    (clk),  //仅用于mult
    .flush                  (flush), //仅用于mult
    .alu_op_i               (aluop_to_ex              ),
    .alu_sel_i              (alusel_to_ex            ),
    .reg1_i                 (reg1_to_ex                 ),
    .reg2_i                 (reg2_to_ex                 ),
    .write_addr_i           (wd_to_ex           ),
    .write_reg_i            (wreg_to_ex        ),
    .inst_i                 (inst_to_ex                 ),
    .write_addr_o           (write_addr_from_ex           ),
    .write_reg_o            (write_reg_from_ex     ),
    .write_data_o           (write_data_from_ex           ),
    .cp0_reg_data_i         (cp0_reg_data_to_ex         ),
    .wb_cp0_reg_we          (cp0_reg_we_from_mem_wb         ),
    .wb_cp0_reg_write_addr  (cp0_reg_write_addr_from_mem_wb  ),
    .wb_cp0_reg_data        (cp0_reg_data_from_mem_wb      ),
    .mem_cp0_reg_we         (cp0_reg_we_from_mem         ),
    .mem_cp0_reg_write_addr (cp0_reg_write_addr_from_mem ),
    .mem_cp0_reg_data       (cp0_reg_data_from_mem       ),
    .cp0_reg_read_addr_o    (ex_cp0_reg_read_addr_to_cp0    ),
    .cp0_reg_we_o           (cp0_reg_we_from_ex           ),
    .cp0_reg_write_addr_o   (cp0_reg_write_addr_from_ex   ),
    .cp0_reg_data_o         (cp0_reg_data_from_ex         ),
    .hi_i                   (hi_to_ex                   ),
    .lo_i                   (lo_to_ex               ),
    .wb_write_hilo_i        (whilo_from_mem_wb        ),
    .wb_hi_i                (hi_from_mem_wb                ),
    .wb_lo_i                (lo_from_mem_wb                ),
    .mem_write_hilo_i       (whilo_from_mem       ),
    .mem_hi_i               (hi_from_mem               ),
    .mem_lo_i               (lo_from_mem               ),
    .write_hilo_o           (whilo_from_ex           ),
    .hi_o                   (hi_from_ex                   ),
    .lo_o                   (lo_from_ex                   ),
    .div_result_i           (result_from_div           ),
    .div_ready_i            (ready_from_div            ),
    .signed_div_o           (signed_div_from_ex           ),
    .div_start_o            (start_from_ex            ),
    .div_op_data1_o         (opdata1_from_ex         ),
    .div_op_data2_o         (opdata2_from_ex         ),
    .stallreq               (stallreq_from_ex               ),
    .link_address_i         (link_address_to_ex         ),
    .is_in_delayslot_i      (is_in_delayslot_to_ex      ),
    .is_in_delayslot_o      (is_in_delayslot_from_ex      ),
    .excepttype_i           (excepttype_to_ex          ),
    .current_inst_address_i (current_inst_address_to_ex ),
    .excepttype_o           (excepttype_from_ex           ),
    .current_inst_address_o (current_inst_address_from_ex ),

    .mem_cached_trans       (cached_trans_from_ex),
    .mem_l_op               (l_op_from_ex),
    .mem_s_op               (s_op_from_ex),
    .mem_op_o               (alu_op_from_ex              ),
    .mem_addr_o             (mem_addr_from_ex            ),
    .data_to_mem_o          (data_to_mem_from_ex          )
);
 
 wire[4:0]      wd_from_ex_mem;
 wire           wreg_from_ex_mem;
 wire[31:0]     wdata_from_ex_mem;
 wire[31:0]     hi_from_ex_mem;
 wire[31:0]     lo_from_ex_mem;
 wire           whilo_from_ex_mem;
 wire[7:0]      aluop_from_ex_mem;
 wire[31:0]     mem_addr_from_ex_mem;
 wire[31:0]     mem_wdata_from_ex_mem; 
 wire           l_op_from_ex_mem;
 wire           s_op_from_ex_mem;
 wire           cached_trans_from_ex_mem;

 wire           cp0_reg_we_from_ex_mem;
 wire[4:0]      cp0_reg_write_addr_from_ex_mem;
 wire[31:0]     cp0_reg_data_from_ex_mem;
 wire[31:0]     excepttype_from_ex_mem;
 wire           is_in_delayslot_from_ex_mem;
 wire[31:0]     current_inst_address_from_ex_mem;

ex_mem u_ex_mem(
	.clk                      (clk                      ),
    .rst                      (rst                      ),
    .stall                    (stall                    ),
    .flush                    (flush                    ),
    .ex_wd                    (write_addr_from_ex                   ),
    .ex_wreg                  (write_reg_from_ex                  ),
    .ex_wdata                 (write_data_from_ex                 ),
    .ex_hi                    (hi_from_ex                    ),
    .ex_lo                    (lo_from_ex                    ),
    .ex_whilo                 (whilo_from_ex                ),
    .ex_aluop                 (alu_op_from_ex                 ),
    .ex_mem_addr              (mem_addr_from_ex              ),
    .ex_mem_wdata             (data_to_mem_from_ex                  ),
    .ex_l_op                  (l_op_from_ex),
    .ex_s_op                  (s_op_from_ex),
    .ex_cached_trans          (cached_trans_from_ex),
    .ex_cp0_reg_we            (cp0_reg_we_from_ex            ),
    .ex_cp0_reg_write_addr    (cp0_reg_write_addr_from_ex    ),
    .ex_cp0_reg_data          (cp0_reg_data_from_ex          ),
    .ex_excepttype            (excepttype_from_ex            ),
    .ex_is_in_delayslot       (is_in_delayslot_from_ex       ),
    .ex_current_inst_address  (current_inst_address_from_ex  ),
    .mem_wd                   (wd_from_ex_mem                   ),
    .mem_wreg                 (wreg_from_ex_mem                 ),
    .mem_wdata                (wdata_from_ex_mem                ),
    .mem_hi                   (hi_from_ex_mem                   ),
    .mem_lo                   (lo_from_ex_mem                   ),
    .mem_whilo                (whilo_from_ex_mem                ),
    .mem_aluop                (aluop_from_ex_mem                ),
    .mem_mem_addr             (mem_addr_from_ex_mem             ),
    .mem_mem_wdata            (mem_wdata_from_ex_mem            ),
    .mem_l_op                 (l_op_from_ex_mem),
    .mem_s_op                 (s_op_from_ex_mem),
    .mem_cached_trans         (cached_trans_from_ex_mem),
    .mem_cp0_reg_we           (cp0_reg_we_from_ex_mem           ),
    .mem_cp0_reg_write_addr   (cp0_reg_write_addr_from_ex_mem   ),
    .mem_cp0_reg_data         (cp0_reg_data_from_ex_mem         ),
    .mem_excepttype           (excepttype_from_ex_mem           ),
    .mem_is_in_delayslot      (is_in_delayslot_from_ex_mem      ),
    .mem_current_inst_address (current_inst_address_from_ex_mem )
);

//mem to mem/wb
wire wreg_from_mem;
wire[4:0] wd_from_mem;
wire[31:0] wdata_from_mem;
wire        is_in_delayslot_from_mem;
wire[31:0]  excepttype_from_mem;
wire[31:0]  current_inst_address_from_mem;
//mem and cp0
wire [31:0] status_from_cp0;
wire [31:0] cause_from_cp0;
wire [31:0] epc_from_cp0;
wire [31:0] bad_addr_from_mem;
//mem to ctrl
wire        stallreq_from_mem;

wire [31:0] epc_lastest;
mem u_mem(
	.rst                  (rst                  ),
    .alu_op_i             (aluop_from_ex_mem             ),
    .mem_addr_i           (mem_addr_from_ex_mem           ),
    .mem_wdata_i          (mem_wdata_from_ex_mem               ),
    .mem_l_op             (l_op_from_ex_mem),
    .mem_s_op             (s_op_from_ex_mem),
    .mem_cached_trans     (cached_trans_from_ex_mem),
    .write_address_i      (wd_from_ex_mem      ),
    .write_reg_i          (wreg_from_ex_mem         ),
    .write_data_i         (wdata_from_ex_mem         ),
    .write_address_o      (write_address_from_mem      ),
    .write_reg_o          (write_reg_from_mem         ),
    .write_data_o         (write_data_from_mem         ),
    .hi_i                 (hi_from_ex_mem                ),
    .lo_i                 (lo_from_ex_mem                 ),
    .write_hilo_i         (whilo_from_ex_mem         ),
    .hi_o                 (hi_from_mem                 ),
    .lo_o                 (lo_from_mem                 ),
    .write_hilo_o         (whilo_from_mem         ),
    .cp0_reg_we_i         (cp0_reg_we_from_ex_mem         ),
    .cp0_reg_write_addr_i (cp0_reg_write_addr_from_ex_mem ),
    .cp0_reg_data_i       (cp0_reg_data_from_ex_mem       ),
    .wb_cp0_reg_we        (cp0_reg_we_from_mem_wb),
    .wb_cp0_write_addr    (cp0_reg_write_addr_from_mem_wb),
    .wb_cp0_reg_data      (cp0_reg_data_from_mem_wb),
    .cp0_reg_we_o         (cp0_reg_we_from_mem         ),
    .cp0_reg_write_addr_o (cp0_reg_write_addr_from_mem ),
    .cp0_reg_data_o       (cp0_reg_data_from_mem       ),
    .bad_addr_o           (bad_addr_from_mem),
    .is_in_delayslot_i    (is_in_delayslot_from_ex_mem    ),
    .is_in_delayslot_o    (is_in_delayslot_from_mem    ),
    .excepttype_i         (excepttype_from_ex_mem),
    .current_inst_address_i (current_inst_address_from_ex_mem),
    .excepttype_o         (excepttype_from_mem),
    .current_inst_address_o (current_inst_address_from_mem),
    .cp0_status_i         (status_from_cp0),
	.cp0_cause_i          (cause_from_cp0),
	.cp0_epc_i            (epc_from_cp0),
    .mem_data_rdata       (data_axi_rdata       ),
    .mem_data_rvalid      (data_axi_rvalid),
    .mem_data_bvalid      (data_axi_bvalid),
    .mem_data_ren         (data_axi_ren           ),
    .mem_data_wen         (data_axi_wen         ),
    .mem_data_wsel        (data_axi_wsel),
    .mem_data_addr        (data_axi_addr        ),
    .mem_data_wdata       (data_axi_wdata       ),
    .cached_trans         (cached_trans),
    .cp0_epc_o            (epc_lastest),
    .stallreq             (stallreq_from_mem)
);

//mem/wb to wb



mem_wb u_mem_wb(
	.clk                    (clk                    ),
    .rst                    (rst                    ),
    .stall                  (stall                  ),
    .flush                  (flush                  ),
    .mem_wd                 (write_address_from_mem                ),
    .mem_wreg               (write_reg_from_mem               ),
    .mem_wdata              (write_data_from_mem              ),
    .mem_hi                 (hi_from_mem                 ),
    .mem_lo                 (lo_from_mem                 ),
    .mem_whilo              (whilo_from_mem              ),
    .mem_cp0_reg_we         (cp0_reg_we_from_mem         ),
    .mem_cp0_reg_write_addr (cp0_reg_write_addr_from_mem ),
    .mem_cp0_reg_data       (cp0_reg_data_from_mem       ),
    .current_pc_i           (current_inst_address_from_mem),
    .wb_wd                  (wd_from_mem_wb                  ),
    .wb_wreg                (wreg_from_mem_wb                ),
    .wb_wdata               (wdata_from_mem_wb               ),
    .wb_hi                  (hi_from_mem_wb                  ),
    .wb_lo                  (lo_from_mem_wb                  ),
    .wb_whilo               (whilo_from_mem_wb               ),
    .wb_cp0_reg_we          (cp0_reg_we_from_mem_wb          ),
    .wb_cp0_reg_write_addr  (cp0_reg_write_addr_from_mem_wb  ),
    .wb_cp0_reg_data        (cp0_reg_data_from_mem_wb      ),
    .current_pc_o           (debug_wb_pc)
);

assign debug_wb_rf_wen = {4{wreg_from_mem_wb}};
assign debug_wb_rf_wnum = wd_from_mem_wb;
assign debug_wb_rf_wdata = wdata_from_mem_wb;

ctrl u_ctrl(
	.rst              (rst              ),
    .excepttype_i     (excepttype_from_mem     ),
    .cp0_epc_i        (epc_lastest       ),
    .stallreq_from_pc (stallreq_from_pc),
    .stallreq_from_id (stallreq_from_id ),
    .stallreq_from_ex (stallreq_from_ex ),
    .stallreq_from_mem(stallreq_from_mem),
    .new_pc           (new_pc           ),
    .flush            (flush            ),
    .stall            (stall            )
);

div u_div(
	.rst          (rst          ),
    .clk          (clk          ),
    .signed_div_i (signed_div_from_ex ),
    .opdata1_i    (opdata1_from_ex    ),
    .opdata2_i    (opdata2_from_ex    ),
    .start_i      (start_from_ex      ),
    .annul_i      (flush      ),
    .result_o     (result_from_div     ),
    .ready_o      (ready_from_div     )
);


hilo u_hilo(
	.clk          (clk          ),
    .rst          (rst          ),
    .write_enable (whilo_from_mem_wb ),
    .hi_i         (hi_from_mem_wb         ),
    .lo_i         (lo_from_mem_wb        ),
    .hi_o         (hi_to_ex         ),
    .lo_o         (lo_to_ex         )
);

wire[31:0] count_from_cp0;
wire[31:0] compare_from_cp0;
wire[31:0] badvaddr_from_cp0;

cp0 u_cp0(
	.clk                 (clk                 ),
    .rst                 (rst                       ),

    .we_i                (cp0_reg_we_from_mem_wb                ),
    .waddr_i             (cp0_reg_write_addr_from_mem_wb             ),
    .data_i              (cp0_reg_data_from_mem_wb              ),

    .raddr_i             (ex_cp0_reg_read_addr_to_cp0             ),

    .excepttype_i        (excepttype_from_mem        ),
    .int_i               (int              ),

    .current_inst_addr_i (current_inst_address_from_mem ),
    .is_in_delayslot_i   (is_in_delayslot_from_mem   ),
    .bad_addr_i          (bad_addr_from_mem),

    .data_o              (cp0_reg_data_to_ex              ),
    .count_o             (count_from_cp0             ),
    .compare_o           (compare_from_cp0           ),
    .status_o            (status_from_cp0            ),
    .cause_o             (cause_from_cp0             ),
    .epc_o               (epc_from_cp0               ),
    .badvaddr_o          (badvaddr_from_cp0)
);


endmodule