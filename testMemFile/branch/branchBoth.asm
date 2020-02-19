  beq   $8, $9, equal
  addi $12, $12, 10
equal:
  addi  $12, $12, -5
  beq   $12, $10, neq
  addi $12, $12, 13
neq:
  addi  $12, $12, 13
