
module ex_top(
    input wire rst,//时钟复位信号
    input wire clk, //仅用于mult
    input wire flush, //仅用于mult

    input wire [7:0]alu_op_i,//alu操作类型  八位
    input wire [2:0]alu_sel_i,//alu结果运算选择 三位
    input wire [31:0]reg1_i,//寄存器数据
    input wire [31:0]reg2_i,//寄存器数据
    input wire [4:0] write_addr_i,//结果的存储地址
    input wire write_reg_i,//结果是否写寄存器信号
    input wire [31:0] inst_i,


    output wire[4:0] write_addr_o,//写寄存器的地址
    output wire write_reg_o,//是否写寄存器
    output wire[31:0] write_data_o,//计算结果

    //cp0相关端口
    input wire[31:0] cp0_reg_data_i,

    input wire       wb_cp0_reg_we,//写回阶段对cp0的写使能信号
    input wire[4:0]  wb_cp0_reg_write_addr,//写回阶段cp0的写地址
    input wire[31:0] wb_cp0_reg_data,//写回阶段cp0的写数据

    input wire       mem_cp0_reg_we,//访存阶段对cp0的写使能信号
    input wire[4:0]  mem_cp0_reg_write_addr,//访存阶段cp0的写地址
    input wire[31:0] mem_cp0_reg_data,//访存阶段cp0的写数据

    output wire[4:0]  cp0_reg_read_addr_o,//直接与cp0连接，要读取的cp0寄存器地址
    
    output wire       cp0_reg_we_o,//对cp0的写使能信号
    output wire[4:0]  cp0_reg_write_addr_o,//对cp0的写地址
    output wire[31:0] cp0_reg_data_o,//对cp0的写数据

    //hi lo相关端口
    input wire[31:0] hi_i,//hilo中hi的值
    input wire[31:0] lo_i,//hilo中lo的值

    input wire wb_write_hilo_i,//写回阶段对hilo的写使能信号
    input wire[31:0] wb_hi_i,//写回阶段对hi写入的值
    input wire[31:0] wb_lo_i,//写回阶段对ho写入的值

    input wire mem_write_hilo_i,//访存阶段对hilo的写使能信号
    input wire[31:0] mem_hi_i,//访存阶段对hi写入的值
    input wire[31:0] mem_lo_i,//访存阶段对lo写入的值

    output wire write_hilo_o,//ex阶段hilo的写使能信号
    output wire [31:0] hi_o,//ex阶段h对hi写入的值
    output wire [31:0] lo_o,//ex阶段对lo写入的值

    //div 相关端口
    input wire[63:0] div_result_i,//除法运算结果
    input wire div_ready_i,//除法是否结束

    output wire signed_div_o,//是否有符号除法
    output wire div_start_o,//是否开始除法
    output wire[31:0] div_op_data1_o,//被除数
    output wire[31:0] div_op_data2_o,//除数

    output wire stallreq,//流水线暂停信号

    //跳转相关端口
    input wire[31:0] link_address_i,//连接跳转的指令地址
    input wire is_in_delayslot_i,//是否在延迟槽中
    output wire is_in_delayslot_o,
    //异常处理相关
    input wire[31:0] excepttype_i,
    input wire[31:0] current_inst_address_i,
    output wire[31:0] excepttype_o,
    output wire[31:0] current_inst_address_o,

    //访存相关接口
    output wire       mem_l_op,
    output wire       mem_s_op,
    output wire       mem_cached_trans,
    output wire [7:0] mem_op_o,//执行阶段运算类型(判断是load还是store)
    output wire[31:0] mem_addr_o,//进行操作的存储器地址
    output wire[31:0] data_to_mem_o//load指令传入内存的数据
);


    wire[31:0] logic_result;//存储逻辑运算结果的寄存器
    wire[31:0] arith_result;//存储算术运算结果的寄存器
    wire[31:0] shift_result;//存储位移运算结果的寄存器
    wire[31:0] move_result;//存储移动操作结果的寄存器
    wire[31:0] hi;//保存hi的值
    wire[31:0] lo;//
    wire[63:0] mult_result;//存储乘法运算结果的寄存器
    wire overexcept;//表示是否有异常
    
    //进行相应的逻辑运算
    ex_logic ex_logic0(
        .reg1_i(reg1_i),
        .reg2_i(reg2_i),
        .alu_op_i(alu_op_i),
        .rst(rst),
        .logic_result(logic_result)
    );

    //进行位移运算
    ex_shift ex_shift0(
        .reg1_i(reg1_i),
        .reg2_i(reg2_i),
        .alu_op_i(alu_op_i),
        .rst(rst),
        .shift_result(shift_result)
    );

    //hi lo相关
    //解决hilo数据依赖
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

    //对hilo进行写
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

    //移动操作指令
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
    
    //cp0相关运算
    ex_cpzero ex_cpzero0(
        .rst(rst),
        .reg1_i(reg1_i),
        .inst_i(inst_i),
        .alu_op_i(alu_op_i),
        .cp0_reg_we_o(cp0_reg_we_o),
        .cp0_reg_data_o(cp0_reg_data_o),
        .cp0_reg_write_addr_o(cp0_reg_write_addr_o)
    );

    //进行算数运算
    wire add_over_flag;//加法溢出信号
    wire sub_over_flag;//减法溢出信号
    ex_arith ex_arith0(
        .rst(rst),
        .reg1_i(reg1_i),
        .reg2_i(reg2_i),
        .alu_op_i(alu_op_i),
        .add_over_flag(add_over_flag),
        .sub_over_flag(sub_over_flag),
        .arith_result(arith_result)
    );
    
    wire stallreq_for_div;//除法是否暂停流水线
    wire stallreq_for_mult;//乘法是否暂停流水线
    //更新stall请求
    assign    stallreq = stallreq_for_div | stallreq_for_mult;

    //除法相关运算
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

    //乘法运算
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

    //选择相应的结果输出
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

    //访存操作
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
//读数据例外
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

    //写数据例外
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



    //跳转指令
    assign is_in_delayslot_o = is_in_delayslot_i;

    //异常处理
    assign excepttype_o = {excepttype_i[31:17],overexcept,wdata_except,rdata_except,excepttype_i[13:0]};
    assign current_inst_address_o = current_inst_address_i;

endmodule