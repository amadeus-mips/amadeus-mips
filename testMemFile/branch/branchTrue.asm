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