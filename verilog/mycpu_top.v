`include "defines.v"

module mycpu_top(
    input wire[5:0]     int,
    //AXIʱ���븴λ�ź�
    input wire          aclk,//AXIʱ��
    input wire          aresetn,//AXI��λ���͵�ƽ��Ч

    //�������ַͨ����(��ar��ͷ)
    output wire[3:0]    arid,//�������ID��  ȡָΪ0 ȡ��Ϊ1
    output wire[31:0]   araddr,//������ĵ�ַ
    output wire[3:0]    arlen,//����������źţ�������ĳ���(���ݴ�������) �̶�Ϊ0
    output wire[2:0]    arsize,//����������źţ�������Ĵ�С(���ݴ���ÿ�ĵ��ֽ���)
    output wire[1:0]    arburst,//����������źţ���������  �̶�Ϊ2��b01
    output wire[1:0]    arlock,//����������źţ�ԭ����  �̶�Ϊ0
    output wire[3:0]    arcache,//����������źţ�CACHE���� �̶�Ϊ0
    output wire[2:0]    arprot,//����������źţ��������� �̶�Ϊ0
    output wire         arvalid,//�������ַ�����źţ��������ַ��Ч
    input wire          arready,//�������ַ�����źţ�slave��׼���ý��ܵ�ַ����

    //����������ͨ����(��r��ͷ)
    input wire[3:0]     rid,//�������ID�ţ�ͬһ�����ridӦ��aridһ��  ָ�����Ϊ0���� ����Ϊ1
    input wire[31:0]    rdata,//������Ķ�������
    input wire[1:0]     rresp,//����������źţ����ζ������Ƿ�ɹ���� �ɺ���
    input wire          rlast,//����������źţ����ζ���������һ�����ݵ�ָʾ�ź� �ɺ���
    input wire          rvalid,//���������������źţ�������������Ч
    output wire         rready,//���������������źţ�master��׼���ý������ݴ���
    
    //д�����ַͨ����(��aw��ͷ)
    output wire[3:0]    awid,//д����id �̶�Ϊ1
    output wire[31:0]   awaddr,//д�����ַ
    output wire[3:0]    awlen,//д��������źţ�������ĳ���(���ݴ�������) �̶�Ϊ0
    output wire[2:0]    awsize,//д��������źţ�������Ĵ�С(���ݴ���ÿ�ĵ��ֽ���)
    output wire[1:0]    awburst,//д��������źţ��������� �̶�Ϊ2��b01
    output wire[1:0]    awlock,//д��������źţ�ԭ���� �̶�Ϊ0
    output wire[3:0]    awcache,//д��������źţ�CACHE���� �̶�Ϊ0
    output wire[2:0]    awprot,//д��������źţ��������� �̶�Ϊ0
    output wire         awvalid,//д�����ַ�����źţ�д�����ַ��Ч
    input wire          awready,//д�����ַ�����źţ�slave��׼���ý��ܵ�ַ����
    
    //д��������ͨ����(��w��ͷ)
    output wire[3:0]    wid, //д�����ID�� �̶�Ϊ1
    output wire[31:0]   wdata, //д�����д����
    output wire[3:0]    wstrb, //д��������źţ��ֽ�ѡͨλ
    output wire         wlast, //д��������źţ�����д��������һ�����ݵ�ָʾ�ź� �̶�Ϊ1
    output wire         wvalid, //д�������������źţ�д����������Ч
    input  wire         wready, //д�������������źţ�slave��׼���ý������ݴ���
    
    //д������Ӧͨ����(��b��ͷ)
    input wire[3:0]     bid,//д�����ID�ţ�ͬһ�����bid��wid��awidӦһ��  �ɺ���
    input wire[1:0]     bresp,//д��������źţ�����д�����Ƿ�ɹ����  �ɺ���
    input wire          bvalid,//д������Ӧ�����źţ�д������Ӧ��Ч
    output wire         bready,//д������Ӧ�����źţ�master��׼���ý���д��Ӧ

    //debug 
    output wire[31:0]   debug_wb_pc,
    output wire[3:0]    debug_wb_rf_wen,
    output wire[4:0]    debug_wb_rf_wnum,
    output wire[31:0]   debug_wb_rf_wdata
);

wire        flush;
//inst
wire[31:0]      pc_from_cpu;
wire            pc_en_from_cpu;
wire[31:0]      inst_from_axi_interface;
wire            inst_valid_from_axi_interface;

//data
wire        data_ren;
wire        data_wen;
wire[3:0]   data_wsel;
wire[31:0]  data_addr;
wire[31:0]  data_wdata;

wire[31:0]  data_rdata;
wire        data_rvalid;
wire        data_bvalid;
wire        cached_trans;
wire[31:0]  ex_data_addr;

axi_master_interface u_axi_master_interface(
	.aclk             (aclk             ),
    .aresetn          (aresetn          ),

    .arid             (arid             ),
    .araddr           (araddr           ),
    .arlen            (arlen            ),
    .arsize           (arsize           ),
    .arburst          (arburst          ),
    .arlock           (arlock           ),
    .arcache          (arcache          ),
    .arprot           (arprot           ),
    .arvalid          (arvalid          ),
    .arready          (arready          ),

    .rid              (rid              ),
    .rdata            (rdata            ),
    .rresp            (rresp            ),
    .rlast            (rlast            ),
    .rvalid           (rvalid           ),
    .rready           (rready           ),

    .awid             (awid             ),
    .awaddr           (awaddr           ),
    .awlen            (awlen            ),
    .awsize           (awsize           ),
    .awburst          (awburst          ),
    .awlock           (awlock           ),
    .awcache          (awcache          ),
    .awprot           (awprot           ),
    .awvalid          (awvalid          ),
    .awready          (awready          ),

    .wid              (wid              ),
    .wdata            (wdata            ),
    .wstrb            (wstrb            ),
    .wlast            (wlast            ),
    .wvalid           (wvalid           ),
    .wready           (wready           ),

    .bid              (bid              ),
    .bresp            (bresp            ),
    .bvalid           (bvalid           ),
    .bready           (bready           ),

    .flush            (flush),
    .pc_i             (pc_from_cpu             ),
    .pc_en_i          (pc_en_from_cpu            ),
    .inst_o           (inst_from_axi_interface             ),
    .inst_valid_o     (inst_valid_from_axi_interface       ),

    .data_ren         (data_ren         ),
    .data_wen         (data_wen         ),
    .data_wsel        (data_wsel        ),
    .data_addr        (data_addr        ),
    .data_wdata       (data_wdata       ),
    .cached_trans     (cached_trans),
    .ex_data_addr     (ex_data_addr),
    .data_rdata       (data_rdata       ),
    .data_rvalid      (data_rvalid      ),
    .data_bvalid      (data_bvalid      )
);



mycpu u_mycpu(
	.clk               (aclk               ),
    .resetn            (aresetn            ),
    .int               (int               ),
    
    .pc_o              (pc_from_cpu              ),
    .pc_en             (pc_en_from_cpu             ),
    .inst              (inst_from_axi_interface              ),
    .inst_valid        (inst_valid_from_axi_interface        ),

    .data_axi_ren      (data_ren      ),
    .data_axi_wen      (data_wen     ),
    .data_axi_wsel     (data_wsel),
    .data_axi_addr     (data_addr    ),
    .data_axi_wdata    (data_wdata   ),
    .cached_trans      (cached_trans),
    .ex_data_addr      (ex_data_addr),

    .data_axi_rdata    (data_rdata   ),
    .data_axi_rvalid   (data_rvalid),
    .data_axi_bvalid   (data_bvalid),
    
    .axi_flush         (flush),

    .debug_wb_pc       (debug_wb_pc       ),
    .debug_wb_rf_wen   (debug_wb_rf_wen   ),
    .debug_wb_rf_wnum  (debug_wb_rf_wnum  ),
    .debug_wb_rf_wdata (debug_wb_rf_wdata )
);



endmodule