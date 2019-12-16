`include "../defines.v"

module axi_master_interface(
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
    output wire          arvalid,//�������ַ�����źţ��������ַ��Ч
    input wire          arready,//�������ַ�����źţ�slave��׼���ý��ܵ�ַ����

    //����������ͨ����(��r��ͷ)
    input wire[3:0]     rid,//�������ID�ţ�ͬһ�����ridӦ��aridһ��  ָ�����Ϊ0���� ����Ϊ1
    input wire[31:0]    rdata,//������Ķ�������
    input wire[1:0]     rresp,//����������źţ����ζ������Ƿ�ɹ���� �ɺ���
    input wire          rlast,//����������źţ����ζ���������һ�����ݵ�ָʾ�ź� �ɺ���
    input wire          rvalid,//���������������źţ�������������Ч
    output reg          rready,//���������������źţ�master��׼���ý������ݴ���
    
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
    input  wire    wready, //д�������������źţ�slave��׼���ý������ݴ���
    
    //д������Ӧͨ����(��b��ͷ)
    input wire[3:0]     bid,//д�����ID�ţ�ͬһ�����bid��wid��awidӦһ��  �ɺ���
    input wire[1:0]     bresp,//д��������źţ�����д�����Ƿ�ɹ����  �ɺ���
    input wire          bvalid,//д������Ӧ�����źţ�д������Ӧ��Ч
    output reg         bready,//д������Ӧ�����źţ�master��׼���ý���д��Ӧ

    //����mycpu
    input wire          flush,
    //inst
    input wire[31:0]    pc_i,
    input wire          pc_en_i,
    output wire[31:0]   inst_o,
    output wire         inst_valid_o,   

    //data
    input wire          data_ren,
    input wire          data_wen,
    input wire[3:0]     data_wsel,
    input wire[31:0]    data_addr,
    input wire[31:0]    data_wdata,
    input wire          cached_trans,
    input wire[31:0]    ex_data_addr,
    output wire[31:0]   data_rdata,
    output wire         data_rvalid,
    output wire         data_bvalid
);

//����ָ������ݣ�
//reg[1:0]        axi_wait;   // [1] inst
                            // [0] data
//reg[1:0]        axi_ar_finish;   // ͬ��
//reg[1:0]        axi_r_finish;   // ͬ��

reg[2:0]        r_state;   // see defines.v


wire[3:0]       inst_arid;
wire[31:0]      inst_araddr;
wire[3:0]       inst_arlen;
wire[2:0]       inst_arsize;
wire[1:0]       inst_arburst;
wire[1:0]       inst_arlock;
wire[3:0]       inst_arcache;
wire[2:0]       inst_arprot;
wire            inst_arvalid;

wire[3:0]       data_arid;
wire[31:0]      data_araddr;
wire[3:0]       data_arlen;
wire[2:0]       data_arsize;
wire[1:0]       data_arburst;
wire[1:0]       data_arlock;
wire[3:0]       data_arcache;
wire[2:0]       data_arprot;
wire            data_arvalid;

reg         arvalid_tmp;    // ����״̬��
reg[1:0]    cnt;
reg         cnt_en;
wire        rvalid_s = (flush != 1'b1 && r_state == `R_WAIT && rvalid == 1'b1 && rready == 1'b1 && cnt_en == 1'b1 && cnt != 2'b01) ? 1'b0 : (rvalid & rready);

//�ٲã������ȴ����ָ�
wire     i_d_arvalid_tmp = (inst_arvalid == 1'b1) ? inst_arvalid : data_arvalid;

assign arid     = (inst_arvalid == 1'b1) ? inst_arid    : data_arid;
assign araddr   = (inst_arvalid == 1'b1) ? inst_araddr  : data_araddr;
assign arlen    = (inst_arvalid == 1'b1) ? inst_arlen   : data_arlen;
assign arsize   = (inst_arvalid == 1'b1) ? inst_arsize  : data_arsize;
assign arburst  = (inst_arvalid == 1'b1) ? inst_arburst : data_arburst;
assign arlock   = (inst_arvalid == 1'b1) ? inst_arlock  : data_arlock;
assign arcache  = (inst_arvalid == 1'b1) ? inst_arcache : data_arcache;
assign arprot   = (inst_arvalid == 1'b1) ? inst_arprot  : data_arprot;
assign arvalid  = (flush == 1'b1) ? 1'b0 : 
                    (r_state == `R_IDLE) ? i_d_arvalid_tmp : arvalid_tmp;



always @(posedge aclk) begin
    if(aresetn == 1'b0) begin        
        r_state <= `R_IDLE;
        arvalid_tmp <= 1'b0;
        rready <= 1'b0;
        cnt     <= 2'b00;
        cnt_en  <= 1'b0;
    end else begin
        if(flush == 1'b1) begin            
            r_state <= `R_IDLE;
            arvalid_tmp <= 1'b0;
            rready <= 1'b0;
            cnt_en  <= 1'b1;
        end else begin
            case (r_state)
                `R_IDLE: begin                   
                    if(arvalid == 1'b1 && arready == 1'b0) begin
                        r_state <= `AR_WAIT;
                        arvalid_tmp <= 1'b1;
                    end else if(arvalid == 1'b1 && arready == 1'b1) begin
                        r_state <= `AR_FINISH;
                        cnt     <= cnt + 1;
                        arvalid_tmp <= 1'b0;
                    end else begin
                        r_state <= r_state;
                    end
                end
                `AR_WAIT: begin                    
                    if(arvalid_tmp == 1'b1 && arready == 1'b1) begin
                        r_state <= `AR_FINISH;
                        cnt     <= cnt + 1;
                        arvalid_tmp <= 1'b0;
                    end else begin
                        r_state <= r_state;             
                    end
                end
                `AR_FINISH: begin                    
                    r_state <= `R_WAIT;
                    rready <= 1'b1;
                end
                `R_WAIT: begin
                    if(rvalid == 1'b1 && rready == 1'b1) begin
                        if(cnt_en == 1'b1 && cnt != 2'b01) begin
                            r_state <= r_state;
                            rready <= 1'b1;
                            if(rlast == 1'b1) begin                                                        
                                cnt    <= cnt - 1;  
                            end
                        end else if(rlast != 1'b1) begin
                            r_state <= r_state;
                            rready <= 1'b1;                            
                        end else begin                                    
                            r_state <= `R_FINISH;
                            cnt     <= 2'b00;
                            rready <= 1'b0;
                        end
                    end else begin
                        r_state <= r_state;                        
                    end
                end
                `R_FINISH: begin
                    r_state <= `R_IDLE;
                    cnt_en <= 1'b0;
                end
                default: begin                    
                    r_state <= r_state;
                end
            endcase
        end
    end
end

//д����
reg[2:0]        w_state;

wire[3:0]       data_awid;
wire[31:0]      data_awaddr;
wire[3:0]       data_awlen;
wire[2:0]       data_awsize;
wire[1:0]       data_awburst;
wire[1:0]       data_awlock;
wire[3:0]       data_awcache;
wire[2:0]       data_awprot;
wire            data_awvalid;

wire[3:0]       data_wid;
wire[31:0]      data_wdata1;
wire[3:0]       data_wstrb;
wire            data_wlast;
wire            data_wvalid;

// ����״̬��
reg     awvalid_tmp;
reg     wvalid_tmp;

assign awid     = data_awid;
assign awaddr   = data_awaddr;
assign awlen    = data_awlen;
assign awsize   = data_awsize;
assign awburst  = data_awburst;
assign awlock   = data_awlock;
assign awcache  = data_awcache;
assign awprot   = data_awprot;
assign awvalid  = (w_state == `W_IDLE) ? data_awvalid : awvalid_tmp;

assign wid      = data_wid;
assign wdata    = data_wdata1;
assign wstrb    = data_wstrb;
assign wlast    = data_wlast;
assign wvalid   = wvalid_tmp & data_wvalid;

//�ȴ���ַ�ٴ����ݣ��ɸĽ�
always @(posedge aclk) begin
    if(aresetn == 1'b0) begin
        w_state <= `W_IDLE;
        awvalid_tmp <= 1'b0;
        wvalid_tmp  <= 1'b0;
        bready <= 1'b0;
    end else begin
        case (w_state)
            `W_IDLE: begin
                if(data_awvalid == 1'b1 && awready == 1'b0) begin
                    w_state     <= `AW_WAIT;
                    awvalid_tmp <= 1'b1;
                    wvalid_tmp  <= 1'b0;
                end else if(data_awvalid == 1'b1 && awready == 1'b1) begin
                    w_state     <= `AW_FINISH;
                    awvalid_tmp <= 1'b0;
                    wvalid_tmp  <= 1'b0;
                end else begin
                    w_state     <= w_state;
                    awvalid_tmp <= 1'b0;
                    wvalid_tmp  <= 1'b0;                    
                end
            end 
            `AW_WAIT: begin
                if(awvalid_tmp == 1'b1 && awready == 1'b1) begin
                    w_state     <= `AW_FINISH;
                    awvalid_tmp <= 1'b0;
                end else begin
                    w_state     <= w_state;
                end
            end
            `AW_FINISH: begin
                w_state <= `W_WAIT;
                wvalid_tmp  <= 1'b1;
            end
            `W_WAIT: begin
                if(wvalid_tmp == 1'b1 && wready == 1'b1) begin
                    if(data_wlast == 1'b1) begin
                        w_state <= `W_FINISH;
                        wvalid_tmp  <= 1'b0;
                    end else begin
                        w_state <= w_state;
                        wvalid_tmp  <= 1'b1;
                    end
                end else begin
                    w_state <= w_state;
                end
            end
            `W_FINISH: begin
                w_state <= `B_WAIT;
                bready  <= 1'b1;
            end
            `B_WAIT: begin
                if(bready == 1'b1 && bvalid == 1'b1) begin
                    w_state <= `W_IDLE;
                    bready  <= 1'b0;
                end else begin
                    w_state <= w_state;
                end
            end
            default: begin
                w_state <= w_state;
            end
        endcase
    end
end


inst_axi u_inst_axi(
    .clk           (aclk),
	.resetn        (aresetn           ),
    .flush         (flush),
    .pc            (pc_i            ),
    .pc_en         (pc_en_i        ),
    .inst          (inst_o          ),
    .inst_valid    (inst_valid_o  ),

    .i_arid        (inst_arid        ),
    .i_araddr      (inst_araddr      ),
    .i_arlen       (inst_arlen       ),
    .i_arsize      (inst_arsize      ),
    .i_arburst     (inst_arburst     ),
    .i_arlock      (inst_arlock      ),
    .i_arcache     (inst_arcache     ),
    .i_arprot      (inst_arprot      ),
    .i_arvalid     (inst_arvalid     ),

    .i_rid         (rid         ),
    .i_rdata       (rdata       ),
    .i_rresp       (rresp       ),
    .i_rlast       (rlast       ),
    .i_rvalid      (rvalid_s      )
);

data_axi u_data_axi(
    .clk        (aclk),
	.resetn     (aresetn     ),
    .flush      (flush),
    // connect to cpu
    .mem_ren    (data_ren    ),
    .mem_wen    (data_wen    ),
    .mem_wsel   (data_wsel   ),
    .mem_addr   (data_addr   ),
    .mem_wdata  (data_wdata  ),
    .cached_trans (cached_trans),
    .ex_addr   (ex_data_addr),
    .mem_rdata  (data_rdata  ),
    .mem_rvalid (data_rvalid ),
    .mem_bvalid (data_bvalid ),

    .d_arid     (data_arid     ),
    .d_araddr   (data_araddr   ),
    .d_arlen    (data_arlen    ),
    .d_arsize   (data_arsize   ),
    .d_arburst  (data_arburst  ),
    .d_arlock   (data_arlock   ),
    .d_arcache  (data_arcache  ),
    .d_arprot   (data_arprot   ),
    .d_arvalid  (data_arvalid  ),

    .d_rid      (rid      ),
    .d_rdata    (rdata    ),
    .d_rresp    (rresp    ),
    .d_rlast    (rlast    ),
    .d_rvalid   (rvalid & rready   ),

    .d_awid     (data_awid     ),
    .d_awaddr   (data_awaddr   ),
    .d_awlen    (data_awlen    ),
    .d_awsize   (data_awsize   ),
    .d_awburst  (data_awburst  ),
    .d_awlock   (data_awlock   ),
    .d_awcache  (data_awcache  ),
    .d_awprot   (data_awprot   ),
    .d_awvalid  (data_awvalid  ),

    .d_wid      (data_wid      ),
    .d_wdata    (data_wdata1    ),
    .d_wstrb    (data_wstrb    ),
    .d_wlast    (data_wlast    ),
    .d_wvalid   (data_wvalid   ),
    .d_wready   (wready & wvalid),

    .d_bid      (bid      ),
    .d_bresp    (bresp    ),
    .d_bvalid   (bvalid   )
);



endmodule