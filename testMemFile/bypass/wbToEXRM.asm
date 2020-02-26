addi $1, $zero, 2
addi $2, $zero, 3
addi $3, $zero, 0
addi $4, $zero, 200
nop
nop
nop
nop
nop
add $3, $1, $2
nop
sw $3, 0($4)
lw $5, 0($4)