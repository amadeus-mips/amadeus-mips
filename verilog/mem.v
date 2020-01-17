module mem(
    input wire      rst,

    input wire[7:0]  alu_op_i,
    input wire[31:0] mem_addr_i,        //内存地址
    input wire[31:0] mem_wdata_i,            //要写入内存的数据
    input wire       mem_l_op,
    input wire       mem_s_op,
    input wire       mem_cached_trans,

    //regfile相关     
    input wire[4:0] write_address_i,    //写寄存器的地址
    input wire      write_reg_i,        //是否写寄存器
    input wire[31:0] write_data_i,      //写寄存器的数据
    
    output wire[4:0] write_address_o,
    output wire      write_reg_o,
    output reg[31:0] write_data_o,

    //hi,lo相关
    input wire[31:0] hi_i,
    input wire[31:0] lo_i,
    input wire      write_hilo_i,       //写使能

    output wire[31:0] hi_o,
    output wire[31:0] lo_o,
    output wire      write_hilo_o,
    
    //cp0相关
    input wire       cp0_reg_we_i,      //cp0写使能信号
    input wire[4:0]  cp0_reg_write_addr_i,//cp0写地址
    input wire[31:0] cp0_reg_data_i,    //cp0写数据

    //wb阶段cp0
    input wire       wb_cp0_reg_we,
    input wire[4:0]  wb_cp0_write_addr,
    input wire[31:0] wb_cp0_reg_data,

    output wire       cp0_reg_we_o,
    output wire[4:0]  cp0_reg_write_addr_o,
    output wire[31:0] cp0_reg_data_o,
    
    output reg[31:0] bad_addr_o,

    //跳转相关
    input wire       is_in_delayslot_i,
    output wire       is_in_delayslot_o,

    //异常相关
    input wire[31:0] excepttype_i,
    input wire[31:0] current_inst_address_i,
    output reg[31:0] excepttype_o,
    output wire[31:0] current_inst_address_o,

    input wire[31:0]  cp0_status_i,
    input wire[31:0]  cp0_cause_i,
    input wire[31:0]  cp0_epc_i,

    input  wire[31:0]   mem_data_rdata,     //读到的数据
    input  wire         mem_data_rvalid,    //读到的数据有效
    input  wire         mem_data_bvalid,    //写数据完成
    //input  wire         mem_data_wait,      //尚未处理

    output wire          mem_data_ren,       //读数据使能
    output wire          mem_data_wen,       //写数据使能
    output reg[3:0]     mem_data_wsel,      //写数据选通
    output wire[31:0]    mem_data_addr,      //数据地址
    output reg[31:0]    mem_data_wdata,     //要写的数据
    output wire          cached_trans,       // 能否经过cache

    output wire[31:0] cp0_epc_o,

    //处理axi访存stall
    output wire       stallreq
);
    assign cached_trans = mem_cached_trans;
    //always @(*) begin
    //   if(rst == 1'b1) begin
    //      cached_trans <= 1'b0;
    //    end else begin
    //        if(mem_addr_i[31:29] == 3'b101) begin
    //           cached_trans <= 1'b0;
    //        end else begin
    //            cached_trans <= 1'b1;
    //        end
    //    end
    //end
    
//****************signal to ram******************************************************************************//
    assign mem_data_ren = ((~(|excepttype_o)) & mem_l_op);
    assign mem_data_wen = ((~(|excepttype_o)) & mem_s_op);
    assign mem_data_addr = {mem_addr_i[31:2], 2'b00};
    always @(*) begin
        if(rst == 1'b1) begin
            mem_data_wdata <= 32'd0;
            mem_data_wsel <= 4'd0;
        end else begin
            case (alu_op_i)
                `EXE_SB_OP: begin
                    mem_data_wdata <= {mem_wdata_i[7:0], mem_wdata_i[7:0], mem_wdata_i[7:0], mem_wdata_i[7:0]};
                    case (mem_addr_i[1:0])
                        2'b11: begin
                            mem_data_wsel <= 4'b1000;
                        end
                        2'b10: begin
                            mem_data_wsel <= 4'b0100;
                        end
                        2'b01: begin
                            mem_data_wsel <= 4'b0010;
                        end
                        2'b00: begin
                            mem_data_wsel <= 4'b0001;
                        end
                    endcase
                end 
                `EXE_SH_OP: begin
                    mem_data_wdata  <= {mem_wdata_i[15:0], mem_wdata_i[15:0]};
                    if(mem_addr_i[1] == 1'b1) begin
                        mem_data_wsel <= 4'b1100;
                    end else begin
                        mem_data_wsel <= 4'b0011;
                    end
                end
                `EXE_SW_OP: begin
                    mem_data_wdata  <= mem_wdata_i;
                    mem_data_wsel <= 4'b1111;
                end
                default: begin
                    mem_data_wdata  <= 32'd0;
                    mem_data_wsel <= 4'd0;
                end
            endcase
        end
    end
//***********************************************************************************************************//        
    always @(*) begin
        if(rst == 1'b1) begin
            write_data_o    <= 32'h00000000;
        end else begin   
            case (alu_op_i)
                `EXE_LB_OP: begin
                    if(mem_data_rvalid == 1'b1) begin
                        case (mem_addr_i[1:0])
                            2'b11: begin
                                write_data_o <= {{24{mem_data_rdata[31]}}, mem_data_rdata[31:24]};
                            end
                            2'b10: begin
                                write_data_o <= {{24{mem_data_rdata[23]}}, mem_data_rdata[23:16]};
                            end
                            2'b01: begin
                                write_data_o <= {{24{mem_data_rdata[15]}}, mem_data_rdata[15:8]};
                            end
                            2'b00: begin
                                write_data_o <= {{24{mem_data_rdata[7]}}, mem_data_rdata[7:0]};
                            end
                        endcase
                    end else begin
                        write_data_o <= write_data_i;
                    end
                end
                `EXE_LBU_OP: begin
                    if(mem_data_rvalid == 1'b1) begin
                        case (mem_addr_i[1:0])
                            2'b11: begin
                                write_data_o <= {{24{1'b0}}, mem_data_rdata[31:24]};
                            end
                            2'b10: begin
                                write_data_o <= {{24{1'b0}}, mem_data_rdata[23:16]};
                            end
                            2'b01: begin
                                write_data_o <= {{24{1'b0}}, mem_data_rdata[15:8]};
                            end
                            2'b00: begin
                                write_data_o <= {{24{1'b0}}, mem_data_rdata[7:0]};
                            end
                        endcase                     
                    end else begin
                        write_data_o <= write_data_i;
                    end
                end
                `EXE_LH_OP: begin
                    if(mem_data_rvalid == 1'b1) begin
                        case (mem_addr_i[1])
                            1'b1: begin
                                write_data_o <= {{16{mem_data_rdata[31]}}, mem_data_rdata[31:16]};
                            end
                            1'b0: begin
                                write_data_o <= {{16{mem_data_rdata[15]}}, mem_data_rdata[15:0]};
                            end
                        endcase
                    end else begin
                        write_data_o <= write_data_i;
                    end
                end
                `EXE_LHU_OP: begin
                    if(mem_data_rvalid == 1'b1) begin
                        case (mem_addr_i[1])
                            1'b1: begin
                                write_data_o <= {{16{1'b0}}, mem_data_rdata[31:16]};
                            end
                            1'b0: begin
                                write_data_o <= {{16{1'b0}}, mem_data_rdata[15:0]};
                            end 
                        endcase
                    end else begin
                        write_data_o <= write_data_i;
                    end
                end
                `EXE_LW_OP: begin
                    if(mem_data_rvalid == 1'b1) begin
                        write_data_o    <= mem_data_rdata;
                    end else begin
                        write_data_o <= write_data_i;
                    end
                end
                default: begin
                    write_data_o <= write_data_i;
                end 
            endcase
        end
    end

    assign stallreq = (|excepttype_o == 1'b1) ? 1'b0 :
                        (mem_l_op == 1'b1) ? (~mem_data_rvalid) : 
                        (mem_s_op == 1'b1) ? (~mem_data_bvalid) : 1'b0;

//***********except handle *****************************************************//
    //数据依赖
    reg[31:0]   cp0_status;
    reg[31:0]   cp0_epc;
    reg[31:0]   cp0_cause;

    always @(*) begin
        if(rst == 1'b1) begin
            cp0_status <= 32'h00000000;
        end else begin
            if(wb_cp0_reg_we == 1'b1 && wb_cp0_write_addr == 5'b01100) begin
                cp0_status <= wb_cp0_reg_data; 
            end else begin
                cp0_status <= cp0_status_i;
            end
        end
    end
    
    always @(*) begin
        if(rst == 1'b1) begin
            cp0_cause <= 32'h00000000;
        end else begin
            if(wb_cp0_reg_we == 1'b1 && wb_cp0_write_addr == 5'b01101) begin
                cp0_cause <= wb_cp0_reg_data;
            end else begin
                cp0_cause <= cp0_cause_i;
            end
        end
    end

    always @(*) begin
        if(rst == 1'b1) begin
            cp0_epc <= 32'h00000000;
        end else begin
            if(wb_cp0_reg_we == 1'b1 && wb_cp0_write_addr == 5'b01110) begin
                cp0_epc <= wb_cp0_reg_data;
            end else begin
                cp0_epc <= cp0_epc_i;
            end
        end
    end
    assign cp0_epc_o = cp0_epc;

    always @(*) begin
        if(rst == 1'b1) begin
            bad_addr_o <= 32'h00000000;
        end else begin
            if(excepttype_o[13] == 1'b1) begin
                bad_addr_o <= current_inst_address_i;
            end else begin
                bad_addr_o <= mem_addr_i;
            end
        end
    end
    
    always @(*) begin
        if(rst == 1'b1) begin
            excepttype_o <= 32'h00000000;
        end else begin
            excepttype_o <= excepttype_i;
            if(current_inst_address_i != 32'h00000000)begin 
                if(cp0_status[0] == 1'b1 && cp0_status[1] == 1'b0 
                        && cp0_status[15:8] != 8'b00000000 
                        && cp0_cause[15:8] != 8'b00000000) begin
                    excepttype_o[0] <= 1'b1;
                end
            end
        end
    end
//***********************************************************//    

    
//***************data pass***********************************//
    assign write_address_o  = write_address_i;
    assign write_reg_o     = write_reg_i;
    assign hi_o            = hi_i;
    assign lo_o            = lo_i;
    assign write_hilo_o    = write_hilo_i;
    assign cp0_reg_we_o    = cp0_reg_we_i;
    assign cp0_reg_write_addr_o = cp0_reg_write_addr_i;
    assign cp0_reg_data_o  = cp0_reg_data_i;
    assign is_in_delayslot_o = is_in_delayslot_i;
    assign current_inst_address_o = current_inst_address_i;
//***********************************************************//

endmodule // mem