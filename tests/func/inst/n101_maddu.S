#include <asm.h>
#include <regdef.h>
#include <inst_test.h>

LEAF(n101_maddu_test)
    .set noreorder
    addiu s0, s0 ,1
    li  s2, 0x0
###test inst
    TEST_MADDU(0x327b23c6, 0x6b8b4567, 0x99c9a500, 0x6c1a1b60, 0x736be44f, 0x1d902567)
    TEST_MADDU(0x19495cff, 0x74b0dc51, 0xee026dc0, 0xd85e0aec, 0xe272d8a5, 0x43b78951)
    TEST_MADDU(0x45b90738, 0xd70d64f0, 0x5cc4e136, 0x00000000, 0x45b90738, 0xd70d64f0)
    TEST_MADDU(0x45b90738, 0xd70d64f0, 0x00000000, 0xae15e524, 0x45b90738, 0xd70d64f0)
    TEST_MADDU(0x45b90738, 0xd70d64f0, 0x00000000, 0x00000000, 0x45b90738, 0xd70d64f0)
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
END(n101_maddu_test)
