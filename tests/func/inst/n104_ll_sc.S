#include <asm.h>
#include <regdef.h>
#include <inst_test.h>

LEAF(n104_ll_sc_test)
    .set noreorder
    addiu s0, s0 ,1
    li  s2, 0x0
###test inst

##1
    li t0, 0x8000c000
    li t1, 0xdeadbeaf
    sw t1, 0(t0)
    ll t2, 0(t0)
    bne t1, t2, inst_error
    nop
    nop
    li t3, 0xfaceb00c
    sc t3, 0(t0)
    li t4, 1
    bne t3, t4, inst_error
    nop
    lw t2, 0(t0)
    li t4, 0xfaceb00c
    bne t2, t4, inst_error
    nop
##2
    li t1, 0xafaffafa
    sw t1, 0(t0)
    li t2, 0xbfbffbfb
    sc t2, 0(t0)
    lw t3, 0(t0)
    bne t3, t1, inst_error
    nop
    bne t2, zero, inst_error
    nop
##3
    li t1, 0xcfcffcfc
    sw t1, 0(t0)
    ll t2, 0(t0)
    bne t2, t1, inst_error
    nop
    syscall
    nop
    nop
    nop
    li s2, 0x0
    li t0, 0x8000c000
    li t1, 0xcfcffcfc
    li t3, 0xdfdffdfd
    sc t3, 0(t0)
    bne t3, zero, inst_error
    nop
    lw t4, 0(t0)
    bne t4, t1, inst_error
    nop
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
END(n104_ll_sc_test)
