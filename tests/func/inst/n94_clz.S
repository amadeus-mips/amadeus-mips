#include <asm.h>
#include <regdef.h>
#include <inst_test.h>

LEAF(n94_clz_test)
    .set noreorder
    addiu s0, s0 ,1
    addiu s2, zero, 0x0
###test inst
    TEST_CLZ(0x0, 0x20)
    TEST_CLZ(0xffffffff, 0x0)
    TEST_CLZ(0x0abdfea2, 0x4)
    TEST_CLZ(0xfffa0178, 0x0)
    TEST_CLZ(0x80000001, 0x0)
    TEST_CLZ(0xbae28945, 0x0)
    TEST_CLZ(0x2000235a, 0x2)
    TEST_CLZ(0x4abed249, 0x1)
    TEST_CLZ(0x00000001, 0x1f)
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
END(n94_clz_test)
