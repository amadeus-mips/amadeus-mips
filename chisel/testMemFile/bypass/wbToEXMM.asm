addi $1, $zero, 2
addi $2, $zero, 3
addi $3, $zero, 5
addi $4, $zero, 200
sw $3, 0($4)
nop
nop
nop
nop
nop
lw $5, 0($4)
nop
sw $5, 4($4)
lw $6, 4($4)