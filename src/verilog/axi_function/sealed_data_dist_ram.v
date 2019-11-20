module sealed_data_dist_ram(
    input wire          clka,
    input wire[3:0]     wea,
    input wire[6:0]     waddr,
    input wire[6:0]     raddr,
    input wire[31:0]    dina,
    output wire[31:0]   douta
);

    wire[7:0]    spo0, spo1, spo2, spo3;
    wire[7:0]    dbyte0, dbyte1, dbyte2, dbyte3;
    assign douta = { spo3, spo2, spo1, spo0 };
    assign spo0 = (waddr == raddr && wea[0]) ? dina[7:0] : dbyte0;
    assign spo1 = (waddr == raddr && wea[1]) ? dina[15:8] : dbyte1;
    assign spo2 = (waddr == raddr && wea[2]) ? dina[23:16] : dbyte2;
    assign spo3 = (waddr == raddr && wea[3]) ? dina[31:24] : dbyte3;
    data_bank_dist_ram byte0(
        .clk    (clka),
        .we     (wea[0]),
        .a      (waddr),
        .dpra   (raddr),
        .d      (dina[7:0]),
        .dpo    (dbyte0)
    );

    data_bank_dist_ram byte1(
        .clk    (clka),
        .we     (wea[1]),
        .a      (waddr),
        .dpra   (raddr),
        .d      (dina[15:8]),
        .dpo    (dbyte1)
    );
    
    data_bank_dist_ram byte2(
        .clk    (clka),
        .we     (wea[2]),
        .a      (waddr),
        .dpra   (raddr),
        .d      (dina[23:16]),
        .dpo    (dbyte2)
    );
    
    data_bank_dist_ram byte3(
        .clk    (clka),
        .we     (wea[3]),
        .a      (waddr),
        .dpra   (raddr),
        .d      (dina[31:24]),
        .dpo    (dbyte3)
    );
    

endmodule // sealed_data_dist_ram