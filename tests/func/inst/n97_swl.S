#include <asm.h>
#include <regdef.h>
#include <inst_test.h>

LEAF(n97_swl_test)
    .set noreorder
    addiu s0, s0 ,1
    li  s2, 0x0
###test inst
    TEST_SWL(0x0020cbb6, 0x6ac6e045, 0x8000d000, 0, 0x6ac6e000)
    TEST_SWL(0x735f3fa6, 0xc0b9305a, 0x8000d000, 1, 0xc0b9735f)
    TEST_SWL(0x730f0c14, 0xab309418, 0x8000d000, 2, 0xab730f0c)
    TEST_SWL(0xd6948ca2, 0x01f80a99, 0x8000d000, 3, 0xd6948ca2)
    TEST_SWL(0xe52e236a, 0xb2824c88, 0x8000d000, 0, 0xb2824ce5)
    TEST_SWL(0xa97d161f, 0x70e07e0a, 0x8000d000, 1, 0x70e0a97d)
    TEST_SWL(0x2ced9105, 0x0e96f2c1, 0x8000d000, 2, 0x0e2ced91)
    TEST_SWL(0x6d514064, 0x79386eb0, 0x8000d000, 3, 0x6d514064)
###detect exception
    bne s2, zero, inst_error
    nop
###score ++
    addiu s3, s3, 1
###output (s0<<24)|s3
inst_error:  
    sll t1, s0, 24
    or t0, t1, s3 
    sw t0, 0(s1)
    jr ra
    nop
END(n97_swl_test)
