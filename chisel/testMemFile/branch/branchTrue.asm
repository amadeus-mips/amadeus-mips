addi $8, $0, 2
addi $9, $0, 2
addi $10, $0, 10
addi $2, $0, 12
addi $3, $0, 12
beq $2, $3, branch
addi $2, $2, 1
addi $2, $2, 1
j end
branch:
addi $2, $2, -2
end:
nop