addi $t0, $zero, 24
#t0 stores the memory address
addi $t1, $zero, -1
sw $zero, 0($t0)
sh $t1, 0($t0)
lw $t2, 0($t0)