#include <asm.h>
#include <regdef.h>
#include <inst_test.h>

LEAF(n98_swr_test)
    .set noreorder
    addiu s0, s0 ,1
    li  s2, 0x0
###test inst
    TEST_SWR(0x0020cbb6, 0x6ac6e045, 0x8000d000, 0, 0x0020cbb6)
    TEST_SWR(0x735f3fa6, 0xc0b9305a, 0x8000d000, 1, 0x5f3fa65a)
    TEST_SWR(0x730f0c14, 0xab309418, 0x8000d000, 2, 0x0c149418)
    TEST_SWR(0xd6948ca2, 0x01f80a99, 0x8000d000, 3, 0xa2f80a99)
    TEST_SWR(0xe52e236a, 0xb2824c88, 0x8000d000, 0, 0xe52e236a)
    TEST_SWR(0xa97d161f, 0x70e07e0a, 0x8000d000, 1, 0x7d161f0a)
    TEST_SWR(0x2ced9105, 0x0e96f2c1, 0x8000d000, 2, 0x9105f2c1)
    TEST_SWR(0x6d514064, 0x79386eb0, 0x8000d000, 3, 0x64386eb0)
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
END(n98_swr_test)
