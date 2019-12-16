`include "../defines.v"

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

    //���͵�ַ
    output wire[3:0]    i_arid,     // ��ָ��͵�id
    output wire[31:0]   i_araddr,   // ��ָ��͵ĵ�ַ
    output wire[3:0]    i_arlen,    // ��ָ��͵������䳤�ȣ����ݴ������������̶�Ϊ0
    output wire[2:0]    i_arsize,   // ��ָ��͵��������С��ÿ�Ĵ�����ֽ�����
    output wire[1:0]    i_arburst,  // ��ָ��͵����������ͣ��̶�Ϊ2'b01
    output wire[1:0]    i_arlock,   // ��ָ��͵�ԭ�������̶�Ϊ2'b00
    output wire[3:0]    i_arcache,  // ��ָ��͵�cache���ԣ��̶�Ϊ4'b0000
    output wire[2:0]    i_arprot,   // ��ָ��͵ı������ԣ��̶�Ϊ3'b000
    output wire         i_arvalid,  // ��ָ��͵������ַ��Ч�ź�
    //input  wire         i_arready,  // ��ָ����յ�slave��׼���ý��ܵ�ַ���ź�

    //����ָ��
    input wire[3:0]     i_rid,      // ��ָ����յ�slave�˷��ص�id
    input wire[31:0]    i_rdata,    // ��ָ����յ�slave�˷��ص�ָ��
    input wire[1:0]     i_rresp,    // ��ָ����յ�slave�˷��ص��Ƿ�ɹ�����źţ��ɺ���
    input wire          i_rlast,     // ��ָ����յ�slave�˷��ص��Ƿ������һ�������źţ��ɺ��ԣ���������burst���䣩
    input wire          i_rvalid  // ��ָ����յ�slave�˷��ص�ָ���Ƿ���Ч�ź�
    //output reg          i_rready,   // ��ָ��͵�master��׼���ý���ָ�����ź�

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
    assign i_arprot     = 3'b000;       // ��awprot�йأ�Ŀǰ�ݶ�3'b000
    assign i_arvalid    = flush ? 1'b0 : cached_trans ? miss : pc_en;

    //reg[31:0] last_pc;
    //reg inst_valid_tmp;
    //assign inst_valid   = (last_pc != pc) ? 1'b0 : hit;
    assign inst_valid = cached_trans ? hit : 1'b0;
    //always @(posedge clk) begin
    //    if(resetn == 1'b0) begin
    //        last_pc <= 32'd1;
    //    end else begin
    //        last_pc <= pc;
    //    end
    //end 
                              
    // TODO delete
    // always @(posedge clk) begin
    //     if(resetn == 1'b0) begin
    //         inst <= 32'h00000000;
    //         last_pc <= 32'd1;
    //         inst_valid_tmp <= 1'b0;
    //     end else begin
    //         last_pc <= pc;
    //         if(i_rid == `AXI_INST_Id && i_rvalid == 1'b1) begin
    //             inst <= i_rdata;
    //             inst_valid_tmp <= 1'b1;
    //         end else if(last_pc != pc) begin
    //             inst_valid_tmp <= 1'b0; 
    //         end else begin
    //             inst_valid_tmp <= inst_valid_tmp;
    //         end
    //     end
    // end

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