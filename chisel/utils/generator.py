instructions = ["NOP", "ADD", "ADDU", "ADDI", "ADDIU", "SUB", "SUBU", "SLT ", "SLTU", "SLTI", "SLTIU", "AND", "ANDI",
                "LUI", "NOR", "OR", "ORI", "XOR", "XORI", "SLL", "SLLV", "SRL", "SRLV", "SRA", "SRAV"]
immeInstructions = ["ADDI", "ADDIU", "SLTIU", "ANDI", "LUI", "ORI", "XORI"]
memInstructions = ["LW ", "SW", "LH", "LHU", "SH", "LB", "LBU", "SB"]
branchInstructions = ["BEQ", "BNE", "BGEZ", "BGTZ", "BLEZ", "BLTZ", "BGEZAL", "BLTZAL"]
# should not test jump, as you can jump directly to nop sections
# jumpInstructions = [ "J" , "JAL" , "JR" , "JALR" ]
