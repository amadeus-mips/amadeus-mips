
module ex_top(
    input wire rst,//ʱ�Ӹ�λ�ź�
    input wire clk, //������mult
    input wire flush, //������mult

    input wire [7:0]alu_op_i,//alu��������  ��λ
    input wire [2:0]alu_sel_i,//alu�������ѡ�� ��λ
    input wire [31:0]reg1_i,//�Ĵ�������
    input wire [31:0]reg2_i,//�Ĵ�������
    input wire [4:0] write_addr_i,//����Ĵ洢��ַ
    input wire write_reg_i,//����Ƿ�д�Ĵ����ź�
    input wire [31:0] inst_i,


    output wire[4:0] write_addr_o,//д�Ĵ����ĵ�ַ
    output wire write_reg_o,//�Ƿ�д�Ĵ���
    output wire[31:0] write_data_o,//������

    //cp0��ض˿�
    input wire[31:0] cp0_reg_data_i,

    input wire       wb_cp0_reg_we,//д�ؽ׶ζ�cp0��дʹ���ź�
    input wire[4:0]  wb_cp0_reg_write_addr,//д�ؽ׶�cp0��д��ַ
    input wire[31:0] wb_cp0_reg_data,//д�ؽ׶�cp0��д����

    input wire       mem_cp0_reg_we,//�ô�׶ζ�cp0��дʹ���ź�
    input wire[4:0]  mem_cp0_reg_write_addr,//�ô�׶�cp0��д��ַ
    input wire[31:0] mem_cp0_reg_data,//�ô�׶�cp0��д����

    output wire[4:0]  cp0_reg_read_addr_o,//ֱ����cp0���ӣ�Ҫ��ȡ��cp0�Ĵ�����ַ
    
    output wire       cp0_reg_we_o,//��cp0��дʹ���ź�
    output wire[4:0]  cp0_reg_write_addr_o,//��cp0��д��ַ
    output wire[31:0] cp0_reg_data_o,//��cp0��д����

    //hi lo��ض˿�
    input wire[31:0] hi_i,//hilo��hi��ֵ
    input wire[31:0] lo_i,//hilo��lo��ֵ

    input wire wb_write_hilo_i,//д�ؽ׶ζ�hilo��дʹ���ź�
    input wire[31:0] wb_hi_i,//д�ؽ׶ζ�hiд���ֵ
    input wire[31:0] wb_lo_i,//д�ؽ׶ζ�hoд���ֵ

    input wire mem_write_hilo_i,//�ô�׶ζ�hilo��дʹ���ź�
    input wire[31:0] mem_hi_i,//�ô�׶ζ�hiд���ֵ
    input wire[31:0] mem_lo_i,//�ô�׶ζ�loд���ֵ

    output wire write_hilo_o,//ex�׶�hilo��дʹ���ź�
    output wire [31:0] hi_o,//ex�׶�h��hiд���ֵ
    output wire [31:0] lo_o,//ex�׶ζ�loд���ֵ

    //div ��ض˿�
    input wire[63:0] div_result_i,//����������
    input wire div_ready_i,//�����Ƿ����

    output wire signed_div_o,//�Ƿ��з��ų���
    output wire div_start_o,//�Ƿ�ʼ����
    output wire[31:0] div_op_data1_o,//������
    output wire[31:0] div_op_data2_o,//����

    output wire stallreq,//��ˮ����ͣ�ź�

    //��ת��ض˿�
    input wire[31:0] link_address_i,//������ת��ָ���ַ
    input wire is_in_delayslot_i,//�Ƿ����ӳٲ���
    output wire is_in_delayslot_o,
    //�쳣�������
    input wire[31:0] excepttype_i,
    input wire[31:0] current_inst_address_i,
    output wire[31:0] excepttype_o,
    output wire[31:0] current_inst_address_o,

    //�ô���ؽӿ�
    output wire       mem_l_op,
    output wire       mem_s_op,
    output wire       mem_cached_trans,
    output wire [7:0] mem_op_o,//ִ�н׶���������(�ж���load����store)
    output wire[31:0] mem_addr_o,//���в����Ĵ洢����ַ
    output wire[31:0] data_to_mem_o//loadָ����ڴ������
);


    wire[31:0] logic_result;//�洢�߼��������ļĴ���
    wire[31:0] arith_result;//�洢�����������ļĴ���
    wire[31:0] shift_result;//�洢λ���������ļĴ���
    wire[31:0] move_result;//�洢�ƶ���������ļĴ���
    wire[31:0] hi;//����hi��ֵ
    wire[31:0] lo;//
    wire[63:0] mult_result;//�洢�˷��������ļĴ���
    wire overexcept;//��ʾ�Ƿ����쳣
    
    //������Ӧ���߼�����
    ex_logic ex_logic0(
        .reg1_i(reg1_i),
        .reg2_i(reg2_i),
        .alu_op_i(alu_op_i),
        .rst(rst),
        .logic_result(logic_result)
    );

    //����λ������
    ex_shift ex_shift0(
        .reg1_i(reg1_i),
        .reg2_i(reg2_i),
        .alu_op_i(alu_op_i),
        .rst(rst),
        .shift_result(shift_result)
    );

    //hi lo���
    //���hilo��������
    ex_hilo_register ex_hilo_register0(
        .rst(rst),
        .mem_hi_i(mem_hi_i),
        .mem_lo_i(mem_lo_i),
        .mem_write_hilo_i(mem_write_hilo_i),

        .wb_hi_i(wb_hi_i),
        .wb_lo_i(wb_lo_i),
        .wb_write_hilo_i(wb_write_hilo_i),

        .hi_i(hi_i),
        .lo_i(lo_i),

        .hi(hi),
        .lo(lo)
    );

    //��hilo����д
    ex_hilo_write ex_hilo_write0(
        .reg1_i(reg1_i),
        .hi(hi),
        .lo(lo),
        .div_result_i(div_result_i),
        .mult_result(mult_result),
        .alu_op_i(alu_op_i),
        .rst(rst),
        .hi_o(hi_o),
        .lo_o(lo_o),
        .write_hilo_o(write_hilo_o)
    );

    //�ƶ�����ָ��
    ex_move ex_move0(
        .hi(hi),
        .lo(lo),
        .alu_op_i(alu_op_i),
        .inst_i(inst_i),
        .cp0_reg_data_i(cp0_reg_data_i),
        .rst(rst),

        .mem_cp0_reg_we(mem_cp0_reg_we),
        .mem_cp0_reg_write_addr(mem_cp0_reg_write_addr),
        .mem_cp0_reg_data(mem_cp0_reg_data),

        .wb_cp0_reg_we(wb_cp0_reg_we),
        .wb_cp0_reg_write_addr(wb_cp0_reg_write_addr),
        .wb_cp0_reg_data(wb_cp0_reg_data),

        .move_result(move_result),
        .cp0_reg_read_addr_o(cp0_reg_read_addr_o)
    );
    
    //cp0�������
    ex_cpzero ex_cpzero0(
        .rst(rst),
        .reg1_i(reg1_i),
        .inst_i(inst_i),
        .alu_op_i(alu_op_i),
        .cp0_reg_we_o(cp0_reg_we_o),
        .cp0_reg_data_o(cp0_reg_data_o),
        .cp0_reg_write_addr_o(cp0_reg_write_addr_o)
    );

    //������������
    wire add_over_flag;//�ӷ�����ź�
    wire sub_over_flag;//��������ź�
    ex_arith ex_arith0(
        .rst(rst),
        .reg1_i(reg1_i),
        .reg2_i(reg2_i),
        .alu_op_i(alu_op_i),
        .add_over_flag(add_over_flag),
        .sub_over_flag(sub_over_flag),
        .arith_result(arith_result)
    );
    
    wire stallreq_for_div;//�����Ƿ���ͣ��ˮ��
    wire stallreq_for_mult;//�˷��Ƿ���ͣ��ˮ��
    //����stall����
    assign    stallreq = stallreq_for_div | stallreq_for_mult;

    //�����������
    ex_div ex_div0(
        .rst(rst),
        .alu_op_i(alu_op_i),
        .reg1_i(reg1_i),
        .reg2_i(reg2_i),
        .div_ready_i(div_ready_i),
        .stallreq_for_div(stallreq_for_div),
        .div_op_data1_o(div_op_data1_o),
        .div_op_data2_o(div_op_data2_o),
        .div_start_o(div_start_o),
        .signed_div_o(signed_div_o)
    );

    //�˷�����
    ex_mult ex_mult0(
        .rst(rst),
        .clk(clk),
        .flush(flush),

        .alu_op_i(alu_op_i),
        .reg1_i(reg1_i),
        .reg2_i(reg2_i),

        .mult_result(mult_result),

        .stallreq_for_mult(stallreq_for_mult)
    );

    //ѡ����Ӧ�Ľ�����
    ex_select_result ex_select_result0(
        .alu_op_i(alu_op_i),
        .write_addr_i(write_addr_i),
        .write_reg_i(write_reg_i),
        .add_over_flag(add_over_flag),
        .sub_over_flag(sub_over_flag),
        .alu_sel_i(alu_sel_i),

        .logic_result(logic_result),
        .shift_result(shift_result),
        .move_result(move_result),
        .arith_result(arith_result),
        .link_address_i(link_address_i),

        .write_addr_o(write_addr_o),
        .write_data_o(write_data_o),
        .overexcept(overexcept),
        .write_reg_o(write_reg_o)
    );

    //�ô����
    wire[31:0] offeset;
    assign offeset = {{16{inst_i[15]}},inst_i[15:0]};   
    assign mem_op_o = alu_op_i;
    assign mem_addr_o = reg1_i + offeset;
    assign data_to_mem_o = reg2_i;
    assign mem_l_op = (alu_op_i == `EXE_LB_OP || alu_op_i == `EXE_LBU_OP || 
                             alu_op_i == `EXE_LH_OP || alu_op_i == `EXE_LHU_OP || 
                             alu_op_i == `EXE_LW_OP) ? 1'b1 : 1'b0; 
    assign mem_s_op = (alu_op_i == `EXE_SB_OP || alu_op_i == `EXE_SH_OP || 
                             alu_op_i == `EXE_SW_OP) ? 1'b1 : 1'b0;
    assign mem_cached_trans = (mem_addr_o[31:29] == 3'b101) ? 1'b0 : 1'b1;                        
//-------------ex hanlde in advance-------------------------------
//����������
    reg rdata_except;
    always @(*) begin
        if(rst == 1'b1) begin
            rdata_except = 1'b0;
        end else begin
            if((alu_op_i == `EXE_LH_OP || alu_op_i == `EXE_LHU_OP) && mem_addr_o[0] != 1'b0) begin
                rdata_except = 1'b1;
            end else if(alu_op_i == `EXE_LW_OP && mem_addr_o[1:0] != 2'b00) begin
                rdata_except = 1'b1;
            end else begin
                rdata_except = 1'b0;
            end
        end
    end

    //д��������
    reg wdata_except;
    always @(*) begin
        if(rst == 1'b1) begin
            wdata_except = 1'b0;
        end else begin
            if(alu_op_i == `EXE_SH_OP && mem_addr_o[0] != 1'b0) begin
                wdata_except = 1'b1;
            end else if(alu_op_i == `EXE_SW_OP && mem_addr_o[1:0] != 2'b00) begin
                wdata_except = 1'b1;
            end else begin
                wdata_except = 1'b0;
            end
        end
    end
//---------------------------------------------------------------



    //��תָ��
    assign is_in_delayslot_o = is_in_delayslot_i;

    //�쳣����
    assign excepttype_o = {excepttype_i[31:17],overexcept,wdata_except,rdata_except,excepttype_i[13:0]};
    assign current_inst_address_o = current_inst_address_i;

endmodule