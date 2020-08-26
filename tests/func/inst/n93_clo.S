#include <asm.h>
#include <regdef.h>
#include <inst_test.h>

LEAF(n93_clo_test)
    .set noreorder
    addiu s0, s0 ,1
    addiu s2, zero, 0x0
###test inst
    TEST_CLO(0x0, 0x0)
    TEST_CLO(0xffffffff, 0x20)
    TEST_CLO(0x0abdfea2, 0x0)
    TEST_CLO(0xfffa0178, 0xd)
    TEST_CLO(0x80000001, 0x1)
    TEST_CLO(0xbae28945, 0x1)
    TEST_CLO(0xfffffffe, 0x1f)
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
END(n93_clo_test)
