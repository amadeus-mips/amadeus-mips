// 用于axi总线
`define AXI_INST_Id     4'b0000
`define AXI_DATA_Id     4'b0001

`define R_IDLE          3'd0
`define AR_WAIT         3'd1
`define AR_FINISH       3'd2
`define R_WAIT          3'd3
`define R_FINISH        3'd4

`define W_IDLE          3'd0
`define AW_WAIT         3'd1
`define AW_FINISH       3'd2
`define W_WAIT          3'd3
`define W_FINISH        3'd4
`define B_WAIT          3'd5