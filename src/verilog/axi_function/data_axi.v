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

    //��
    //���͵�ַ
    output wire[3:0]    d_arid,     //�����ݷ��͵�ID�� 
    output wire[31:0]   d_araddr,   //�����ݷ��͵ĵ�ַ
    output wire[3:0]    d_arlen,    //�����ݷ��͵������䳤��(���ݴ�������) �̶�Ϊ0
    output wire[2:0]    d_arsize,   //�����ݷ��͵��������С(���ݴ���ÿ�ĵ��ֽ���)
    output wire[1:0]    d_arburst,  //�����ݷ��͵�����������  �̶�Ϊ2'b01
    output wire[1:0]    d_arlock,   //�����ݷ��͵�ԭ�������̶�Ϊ2'b01
    output wire[3:0]    d_arcache,  //�����ݷ��͵�CACHE���� �̶�Ϊ4'b0000
    output wire[2:0]    d_arprot,   //�����ݷ��͵ı������� �̶�Ϊ3'b000
    output wire         d_arvalid,  //�����ݷ��͵�ַ�����źţ������ݷ��͵�ַ��Ч
    //input wire          d_arready,  //�����ݷ��͵�ַ�����źţ�slave��׼���ý��ܵ�ַ����

    //��������
    input wire[3:0]     d_rid,      // �����ݽ��յ�slave�˷��ص�id
    input wire[31:0]    d_rdata,    // �����ݽ��յ�slave�˷��ص�����
    input wire[1:0]     d_rresp,    // �����ݽ��յ�slave�˷��ص��Ƿ�ɹ�����źţ��ɺ���
    input wire          d_rlast,    // �����ݽ��յ�slave�˷��ص��Ƿ������һ�������źţ��ɺ��ԣ���������burst���䣩
    input wire          d_rvalid,   // �����ݽ��յ�slave�˷��ص������Ƿ���Ч�ź�
    //output reg          d_rready,   // �����ݷ��͵�master��׼���ý������ݴ�����ź�

    //д
    //���͵�ַ
    output wire[3:0]    d_awid,     // д���ݷ��͵�ID��
    output wire[31:0]   d_awaddr,   // д���ݷ��͵ĵ�ַ
    output wire[3:0]    d_awlen,    // д���ݷ��͵������䳤�ȣ��̶�Ϊ0
    output wire[2:0]    d_awsize,   // д���ݷ��͵��������С���̶�Ϊ3'b010(4Bytes)
    output wire[1:0]    d_awburst,  // д���ݷ��͵����������ͣ��̶�Ϊ2'b01
    output wire[1:0]    d_awlock,   // д���ݷ��͵�ԭ�������̶�Ϊ2'b00
    output wire[3:0]    d_awcache,  // д���ݷ��͵�CACHE���ԣ��̶�Ϊ4'b0000
    output wire[2:0]    d_awprot,   // д���ݷ��͵ı������ԣ��̶�Ϊ3'b000
    output wire         d_awvalid,  // д���ݷ��͵ĵ�ַ�����ź�
    //input  wire         d_awready,  // д���ݽ��յ�slave��׼���ý��ܵ�ַ���ź�

    //��������
    output wire[3:0]    d_wid,      // д���ݷ��͵�ID��
    output wire[31:0]   d_wdata,    // д���ݷ��͵�����
    output wire[3:0]    d_wstrb,    // д���ݷ��͵��ֽ�ѡͨλ
    output wire         d_wlast,    // д���ݷ��͵��Ƿ������һ�����ݵ��źţ��̶�Ϊ1
    output wire         d_wvalid,   // д���ݷ��͵������Ƿ���Ч���ź�
    input  wire         d_wready,   // д���ݽ��յ�slave��׼���ý������ݵ��ź�

    //����д��Ӧ
    input wire[3:0]     d_bid,      // д���ݵ�ID��
    input wire[1:0]     d_bresp,    // д���ݽ��յ�д�Ƿ�ɹ���ɣ��ɺ���
    input wire          d_bvalid  // д���ݽ��յ���Ӧ�Ƿ���Ч�ź�
    //output wire         d_bready,   // д���ݷ��͵�master��׼���Ž���д��Ӧ���ź�
);

    //wire    cached_trans = (mem_addr[31:29] == 3'b101) ?  1'b0 : 1'b1;   // 0xA0000000~0xBFFFFFFF ����cache
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

    //��
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

    //д
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