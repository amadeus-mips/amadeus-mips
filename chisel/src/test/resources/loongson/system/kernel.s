
kernel.elf:     file format elf32-tradlittlemips


Disassembly of section .bss:

807f0000 <_sbss>:
	...

807f0080 <TCBT>:
	...

807f0088 <current>:
_sbss():
807f0088:	00000000 	nop

Disassembly of section .MIPS.abiflags:

807f0090 <.MIPS.abiflags>:
807f0090:	02200000 	0x2200000
807f0094:	05000101 	bltz	t0,807f049c <_ebss+0x410>
807f0098:	00000000 	nop
	...

Disassembly of section .text:

80000000 <INITLOCATE>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:14
80000000:	3c088000 	lui	t0,0x8000
80000004:	25081000 	addiu	t0,t0,4096
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:15
80000008:	3c098000 	lui	t1,0x8000
8000000c:	25291190 	addiu	t1,t1,4496
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:16
80000010:	3c0aa000 	lui	t2,0xa000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:17
80000014:	010a4025 	or	t0,t0,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:18
80000018:	012a4825 	or	t1,t1,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:19
8000001c:	3c0abfc0 	lui	t2,0xbfc0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:20
80000020:	01485025 	or	t2,t2,t0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:22
80000024:	8d4b0000 	lw	t3,0(t2)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:23
80000028:	254a0004 	addiu	t2,t2,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:24
8000002c:	ad0b0000 	sw	t3,0(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:25
80000030:	25080004 	addiu	t0,t0,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:26
80000034:	1509fffb 	bne	t0,t1,80000024 <INITLOCATE+0x24>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:27
80000038:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:30
8000003c:	3c088000 	lui	t0,0x8000
80000040:	25082000 	addiu	t0,t0,8192
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:31
80000044:	3c098000 	lui	t1,0x8000
80000048:	252927fc 	addiu	t1,t1,10236
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:32
8000004c:	3c0aa000 	lui	t2,0xa000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:33
80000050:	010a4025 	or	t0,t0,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:34
80000054:	012a4825 	or	t1,t1,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:35
80000058:	3c0abfc0 	lui	t2,0xbfc0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:36
8000005c:	01485025 	or	t2,t2,t0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:38
80000060:	8d4b0000 	lw	t3,0(t2)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:39
80000064:	254a0004 	addiu	t2,t2,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:40
80000068:	ad0b0000 	sw	t3,0(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:41
8000006c:	25080004 	addiu	t0,t0,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:42
80000070:	1509fffb 	bne	t0,t1,80000060 <INITLOCATE+0x60>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:43
80000074:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:46
80000078:	3c088000 	lui	t0,0x8000
8000007c:	25083000 	addiu	t0,t0,12288
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:47
80000080:	3c098000 	lui	t1,0x8000
80000084:	25293258 	addiu	t1,t1,12888
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:48
80000088:	3c0aa000 	lui	t2,0xa000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:49
8000008c:	010a4025 	or	t0,t0,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:50
80000090:	012a4825 	or	t1,t1,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:51
80000094:	3c0abfc0 	lui	t2,0xbfc0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:52
80000098:	01485025 	or	t2,t2,t0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:54
8000009c:	8d4b0000 	lw	t3,0(t2)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:55
800000a0:	254a0004 	addiu	t2,t2,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:56
800000a4:	ad0b0000 	sw	t3,0(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:57
800000a8:	25080004 	addiu	t0,t0,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:58
800000ac:	1509fffb 	bne	t0,t1,8000009c <INITLOCATE+0x9c>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:59
800000b0:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:63
800000b4:	3c1a8000 	lui	k0,0x8000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:64
800000b8:	275a202c 	addiu	k0,k0,8236
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:65
800000bc:	03400008 	jr	k0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:66
800000c0:	00000000 	nop
	...

80000380 <EHANDLERLOCATE_380>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:71
80000380:	3c1a8000 	lui	k0,0x8000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:72
80000384:	275a253c 	addiu	k0,k0,9532
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:73
80000388:	03400008 	jr	k0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:74
8000038c:	00000000 	nop
	...

80001000 <_text_ebase_begin>:
	...

80001180 <EHANDLERLOCATE>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:83
80001180:	3c1a8000 	lui	k0,0x8000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:84
80001184:	275a253c 	addiu	k0,k0,9532
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:85
80001188:	03400008 	jr	k0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/evec.S:86
8000118c:	00000000 	nop

80001190 <_text_ebase_end>:
	...

80002000 <_text_code_begin>:
monitor_version():
80002000:	494e4f4d 	0x494e4f4d
80002004:	20524f54 	addi	s2,v0,20308
80002008:	20726f66 	addi	s2,v1,28518
8000200c:	5350494d 	beql	k0,s0,80014544 <_text_test_end+0x112ec>
80002010:	2d203233 	sltiu	zero,t1,12851
80002014:	696e6920 	0x696e6920
80002018:	6c616974 	0x6c616974
8000201c:	64657a69 	0x64657a69
80002020:	0000002e 	0x2e
80002024:	807f0000 	lb	ra,0(v1)
80002028:	807f008c 	lb	ra,140(v1)

8000202c <START>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:50
8000202c:	3c1a807f 	lui	k0,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:51
80002030:	275a0000 	addiu	k0,k0,0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:52
80002034:	3c1b807f 	lui	k1,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:53
80002038:	277b008c 	addiu	k1,k1,140

8000203c <bss_init>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:55
8000203c:	135b0005 	beq	k0,k1,80002054 <bss_init_done>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:56
80002040:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:57
80002044:	af400000 	sw	zero,0(k0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:58
80002048:	275a0004 	addiu	k0,k0,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:59
8000204c:	1000fffb 	b	8000203c <bss_init>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:60
80002050:	00000000 	nop

80002054 <bss_init_done>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:64
80002054:	40086000 	mfc0	t0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:65
80002058:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:66
8000205c:	3909ff07 	xori	t1,t0,0xff07
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:69
80002060:	01094024 	and	t0,t0,t1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:70
80002064:	40886000 	mtc0	t0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:71
80002068:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:73
8000206c:	40086000 	mfc0	t0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:74
80002070:	3c090040 	lui	t1,0x40
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:75
80002074:	01094826 	xor	t1,t0,t1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:76
80002078:	01094024 	and	t0,t0,t1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:77
8000207c:	40886000 	mtc0	t0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:78
80002080:	340a1000 	li	t2,0x1000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:79
80002084:	408a7801 	mtc0	t2,c0_ebase
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:80
80002088:	40086800 	mfc0	t0,c0_cause
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:81
8000208c:	3c090080 	lui	t1,0x80
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:82
80002090:	01284826 	xor	t1,t1,t0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:83
80002094:	01094024 	and	t0,t0,t1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:84
80002098:	40886800 	mtc0	t0,c0_cause
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:86
8000209c:	3c1d8080 	lui	sp,0x8080
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:87
800020a0:	27bd0000 	addiu	sp,sp,0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:88
800020a4:	03a0f025 	move	s8,sp
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:89
800020a8:	3c08807f 	lui	t0,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:90
800020ac:	25080000 	addiu	t0,t0,0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:91
800020b0:	3c09807f 	lui	t1,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:92
800020b4:	ad280070 	sw	t0,112(t1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:93
800020b8:	3c09807f 	lui	t1,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:94
800020bc:	ad280074 	sw	t0,116(t1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:115
800020c0:	3c08bfe4 	lui	t0,0xbfe4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:118
800020c4:	24090007 	li	t1,7
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:119
800020c8:	a1090002 	sb	t1,2(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:123
800020cc:	24090080 	li	t1,128
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:124
800020d0:	a1090003 	sb	t1,3(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:126
800020d4:	24090023 	li	t1,35
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:127
800020d8:	a1090000 	sb	t1,0(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:128
800020dc:	00094a02 	srl	t1,t1,0x8
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:129
800020e0:	a1090001 	sb	t1,1(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:131
800020e4:	24090003 	li	t1,3
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:132
800020e8:	a1090003 	sb	t1,3(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:133
800020ec:	24090003 	li	t1,3
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:134
800020f0:	a1090004 	sb	t1,4(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:135
800020f4:	24090001 	li	t1,1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:136
800020f8:	a1090001 	sb	t1,1(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:141
800020fc:	40086000 	mfc0	t0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:142
80002100:	35081000 	ori	t0,t0,0x1000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:143
80002104:	40886000 	mtc0	t0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:147
80002108:	34080020 	li	t0,0x20
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:149
8000210c:	2508ffff 	addiu	t0,t0,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:150
80002110:	27bdfffc 	addiu	sp,sp,-4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:151
80002114:	afa00000 	sw	zero,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:152
80002118:	1500fffc 	bnez	t0,8000210c <bss_init_done+0xb8>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:153
8000211c:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:154
80002120:	3c08807f 	lui	t0,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:155
80002124:	25080080 	addiu	t0,t0,128
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:156
80002128:	ad1d0000 	sw	sp,0(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:158
8000212c:	40096000 	mfc0	t1,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:159
80002130:	400a6800 	mfc0	t2,c0_cause
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:160
80002134:	35290001 	ori	t1,t1,0x1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:161
80002138:	afaa0074 	sw	t2,116(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:162
8000213c:	afa90070 	sw	t1,112(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:163
80002140:	3c0b8000 	lui	t3,0x8000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:164
80002144:	256b21dc 	addiu	t3,t3,8668
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:165
80002148:	afab0078 	sw	t3,120(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:167
8000214c:	03a07025 	move	t6,sp
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:169
80002150:	34080020 	li	t0,0x20
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:171
80002154:	2508ffff 	addiu	t0,t0,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:172
80002158:	27bdfffc 	addiu	sp,sp,-4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:173
8000215c:	afa00000 	sw	zero,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:174
80002160:	1500fffc 	bnez	t0,80002154 <bss_init_done+0x100>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:175
80002164:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:176
80002168:	3c08807f 	lui	t0,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:177
8000216c:	25080080 	addiu	t0,t0,128
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:178
80002170:	ad1d0004 	sw	sp,4(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:179
80002174:	addd007c 	sw	sp,124(t6)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:181
80002178:	3c0a807f 	lui	t2,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:182
8000217c:	254a0084 	addiu	t2,t2,132
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:183
80002180:	8d4a0000 	lw	t2,0(t2)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:184
80002184:	3c09807f 	lui	t1,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:185
80002188:	ad2a0088 	sw	t2,136(t1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:292
8000218c:	40086000 	mfc0	t0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:293
80002190:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:294
80002194:	35080001 	ori	t0,t0,0x1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:295
80002198:	39091000 	xori	t1,t0,0x1000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:296
8000219c:	01094024 	and	t0,t0,t1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:297
800021a0:	40886000 	mtc0	t0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:298
800021a4:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:301
800021a8:	0800086c 	j	800021b0 <WELCOME>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:302
800021ac:	00000000 	nop

800021b0 <WELCOME>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:307
800021b0:	3c108000 	lui	s0,0x8000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:308
800021b4:	26102000 	addiu	s0,s0,8192
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:309
800021b8:	82040000 	lb	a0,0(s0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:311
800021bc:	26100001 	addiu	s0,s0,1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:312
800021c0:	0c0009c6 	jal	80002718 <WRITESERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:313
800021c4:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:314
800021c8:	82040000 	lb	a0,0(s0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:315
800021cc:	1480fffb 	bnez	a0,800021bc <WELCOME+0xc>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:316
800021d0:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:317
800021d4:	08000891 	j	80002244 <SHELL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:318
800021d8:	00000000 	nop

800021dc <IDLELOOP>:
	...
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:333
80002204:	08000877 	j	800021dc <IDLELOOP>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/init.S:334
80002208:	00000000 	nop

8000220c <SCHEDULE>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:14
8000220c:	3c09807f 	lui	t1,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:15
80002210:	25290080 	addiu	t1,t1,128
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:16
80002214:	8d2a0000 	lw	t2,0(t1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:17
80002218:	3c0c807f 	lui	t4,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:18
8000221c:	8d8b0088 	lw	t3,136(t4)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:19
80002220:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:20
80002224:	154b0003 	bne	t2,t3,80002234 <SCHEDULE+0x28>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:21
80002228:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:22
8000222c:	8d2a0004 	lw	t2,4(t1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:23
80002230:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:25
80002234:	0140e825 	move	sp,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:26
80002238:	ad9d0088 	sw	sp,136(t4)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:27
8000223c:	08000986 	j	80002618 <RETURNFRMTRAP>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/sched.S:28
80002240:	00000000 	nop

80002244 <SHELL>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:27
80002244:	0c0009ce 	jal	80002738 <READSERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:28
80002248:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:30
8000224c:	34080052 	li	t0,0x52
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:31
80002250:	10480026 	beq	v0,t0,800022ec <.OP_R>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:32
80002254:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:33
80002258:	34080044 	li	t0,0x44
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:34
8000225c:	10480034 	beq	v0,t0,80002330 <.OP_D>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:35
80002260:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:36
80002264:	34080041 	li	t0,0x41
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:37
80002268:	10480046 	beq	v0,t0,80002384 <.OP_A>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:38
8000226c:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:39
80002270:	34080047 	li	t0,0x47
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:40
80002274:	10480059 	beq	v0,t0,800023dc <.OP_G>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:41
80002278:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:42
8000227c:	34080054 	li	t0,0x54
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:43
80002280:	10480003 	beq	v0,t0,80002290 <.OP_T>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:44
80002284:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:45
80002288:	08000946 	j	80002518 <.DONE>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:46
8000228c:	00000000 	nop

80002290 <.OP_T>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:49
80002290:	0c0009da 	jal	80002768 <READSERIALWORD>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:50
80002294:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:51
80002298:	27bdffe8 	addiu	sp,sp,-24
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:52
8000229c:	afb00000 	sw	s0,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:53
800022a0:	afb10004 	sw	s1,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:70
800022a4:	2410ffff 	li	s0,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:71
800022a8:	afb0000c 	sw	s0,12(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:72
800022ac:	afb00010 	sw	s0,16(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:73
800022b0:	afb00014 	sw	s0,20(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:76
800022b4:	3411000c 	li	s1,0xc
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:77
800022b8:	27b0000c 	addiu	s0,sp,12
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:79
800022bc:	82040000 	lb	a0,0(s0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:80
800022c0:	2631ffff 	addiu	s1,s1,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:81
800022c4:	0c0009c6 	jal	80002718 <WRITESERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:82
800022c8:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:83
800022cc:	26100001 	addiu	s0,s0,1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:84
800022d0:	1620fffa 	bnez	s1,800022bc <.OP_T+0x2c>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:85
800022d4:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:87
800022d8:	8fb00000 	lw	s0,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:88
800022dc:	8fb10004 	lw	s1,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:89
800022e0:	27bd0018 	addiu	sp,sp,24
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:91
800022e4:	08000946 	j	80002518 <.DONE>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:92
800022e8:	00000000 	nop

800022ec <.OP_R>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:95
800022ec:	27bdfff8 	addiu	sp,sp,-8
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:96
800022f0:	afb00000 	sw	s0,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:97
800022f4:	afb10004 	sw	s1,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:99
800022f8:	3c10807f 	lui	s0,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:100
800022fc:	34110078 	li	s1,0x78
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:102
80002300:	82040000 	lb	a0,0(s0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:103
80002304:	2631ffff 	addiu	s1,s1,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:104
80002308:	0c0009c6 	jal	80002718 <WRITESERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:105
8000230c:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:106
80002310:	26100001 	addiu	s0,s0,1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:107
80002314:	1620fffa 	bnez	s1,80002300 <.OP_R+0x14>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:108
80002318:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:110
8000231c:	8fb00000 	lw	s0,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:111
80002320:	8fb10004 	lw	s1,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:112
80002324:	27bd0008 	addiu	sp,sp,8
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:113
80002328:	08000946 	j	80002518 <.DONE>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:114
8000232c:	00000000 	nop

80002330 <.OP_D>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:117
80002330:	27bdfff8 	addiu	sp,sp,-8
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:118
80002334:	afb00000 	sw	s0,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:119
80002338:	afb10004 	sw	s1,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:121
8000233c:	0c0009da 	jal	80002768 <READSERIALWORD>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:122
80002340:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:123
80002344:	00408025 	move	s0,v0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:124
80002348:	0c0009da 	jal	80002768 <READSERIALWORD>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:125
8000234c:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:126
80002350:	00408825 	move	s1,v0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:129
80002354:	82040000 	lb	a0,0(s0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:130
80002358:	2631ffff 	addiu	s1,s1,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:131
8000235c:	0c0009c6 	jal	80002718 <WRITESERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:132
80002360:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:133
80002364:	26100001 	addiu	s0,s0,1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:134
80002368:	1620fffa 	bnez	s1,80002354 <.OP_D+0x24>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:135
8000236c:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:137
80002370:	8fb00000 	lw	s0,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:138
80002374:	8fb10004 	lw	s1,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:139
80002378:	27bd0008 	addiu	sp,sp,8
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:140
8000237c:	08000946 	j	80002518 <.DONE>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:141
80002380:	00000000 	nop

80002384 <.OP_A>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:144
80002384:	27bdfff8 	addiu	sp,sp,-8
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:145
80002388:	afb00000 	sw	s0,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:146
8000238c:	afb10004 	sw	s1,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:148
80002390:	0c0009da 	jal	80002768 <READSERIALWORD>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:149
80002394:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:150
80002398:	00408025 	move	s0,v0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:151
8000239c:	0c0009da 	jal	80002768 <READSERIALWORD>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:152
800023a0:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:153
800023a4:	00408825 	move	s1,v0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:154
800023a8:	00118882 	srl	s1,s1,0x2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:156
800023ac:	0c0009da 	jal	80002768 <READSERIALWORD>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:157
800023b0:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:158
800023b4:	ae020000 	sw	v0,0(s0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:159
800023b8:	2631ffff 	addiu	s1,s1,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:160
800023bc:	26100004 	addiu	s0,s0,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:161
800023c0:	1620fffa 	bnez	s1,800023ac <.OP_A+0x28>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:162
800023c4:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:164
800023c8:	8fb00000 	lw	s0,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:165
800023cc:	8fb10004 	lw	s1,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:166
800023d0:	27bd0008 	addiu	sp,sp,8
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:167
800023d4:	08000946 	j	80002518 <.DONE>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:168
800023d8:	00000000 	nop

800023dc <.OP_G>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:171
800023dc:	0c0009da 	jal	80002768 <READSERIALWORD>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:172
800023e0:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:174
800023e4:	34040006 	li	a0,0x6
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:175
800023e8:	0c0009c6 	jal	80002718 <WRITESERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:176
800023ec:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:179
800023f0:	40827000 	mtc0	v0,c0_epc
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:184
800023f4:	3c1f807f 	lui	ra,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:185
800023f8:	27ff0000 	addiu	ra,ra,0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:186
800023fc:	afe20078 	sw	v0,120(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:187
80002400:	affd007c 	sw	sp,124(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:189
80002404:	8fe10000 	lw	at,0(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:190
80002408:	8fe20004 	lw	v0,4(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:191
8000240c:	8fe30008 	lw	v1,8(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:192
80002410:	8fe4000c 	lw	a0,12(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:193
80002414:	8fe50010 	lw	a1,16(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:194
80002418:	8fe60014 	lw	a2,20(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:195
8000241c:	8fe70018 	lw	a3,24(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:196
80002420:	8fe8001c 	lw	t0,28(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:197
80002424:	8fe90020 	lw	t1,32(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:198
80002428:	8fea0024 	lw	t2,36(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:199
8000242c:	8feb0028 	lw	t3,40(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:200
80002430:	8fec002c 	lw	t4,44(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:201
80002434:	8fed0030 	lw	t5,48(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:202
80002438:	8fee0034 	lw	t6,52(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:203
8000243c:	8fef0038 	lw	t7,56(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:204
80002440:	8ff0003c 	lw	s0,60(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:205
80002444:	8ff10040 	lw	s1,64(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:206
80002448:	8ff20044 	lw	s2,68(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:207
8000244c:	8ff30048 	lw	s3,72(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:208
80002450:	8ff4004c 	lw	s4,76(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:209
80002454:	8ff50050 	lw	s5,80(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:210
80002458:	8ff60054 	lw	s6,84(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:211
8000245c:	8ff70058 	lw	s7,88(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:212
80002460:	8ff8005c 	lw	t8,92(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:213
80002464:	8ff90060 	lw	t9,96(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:216
80002468:	8ffc006c 	lw	gp,108(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:217
8000246c:	8ffd0070 	lw	sp,112(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:218
80002470:	8ffe0074 	lw	s8,116(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:220
80002474:	3c1f8000 	lui	ra,0x8000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:221
80002478:	27ff2484 	addiu	ra,ra,9348
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:222
8000247c:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:224
80002480:	42000018 	eret

80002484 <.USERRET2>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:230
80002484:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:232
80002488:	3c1f807f 	lui	ra,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:233
8000248c:	27ff0000 	addiu	ra,ra,0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:235
80002490:	afe10000 	sw	at,0(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:236
80002494:	afe20004 	sw	v0,4(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:237
80002498:	afe30008 	sw	v1,8(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:238
8000249c:	afe4000c 	sw	a0,12(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:239
800024a0:	afe50010 	sw	a1,16(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:240
800024a4:	afe60014 	sw	a2,20(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:241
800024a8:	afe70018 	sw	a3,24(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:242
800024ac:	afe8001c 	sw	t0,28(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:243
800024b0:	afe90020 	sw	t1,32(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:244
800024b4:	afea0024 	sw	t2,36(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:245
800024b8:	afeb0028 	sw	t3,40(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:246
800024bc:	afec002c 	sw	t4,44(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:247
800024c0:	afed0030 	sw	t5,48(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:248
800024c4:	afee0034 	sw	t6,52(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:249
800024c8:	afef0038 	sw	t7,56(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:250
800024cc:	aff0003c 	sw	s0,60(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:251
800024d0:	aff10040 	sw	s1,64(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:252
800024d4:	aff20044 	sw	s2,68(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:253
800024d8:	aff30048 	sw	s3,72(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:254
800024dc:	aff4004c 	sw	s4,76(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:255
800024e0:	aff50050 	sw	s5,80(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:256
800024e4:	aff60054 	sw	s6,84(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:257
800024e8:	aff70058 	sw	s7,88(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:258
800024ec:	aff8005c 	sw	t8,92(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:259
800024f0:	aff90060 	sw	t9,96(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:262
800024f4:	affc006c 	sw	gp,108(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:263
800024f8:	affd0070 	sw	sp,112(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:264
800024fc:	affe0074 	sw	s8,116(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:266
80002500:	8ffd007c 	lw	sp,124(ra)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:267
80002504:	34040007 	li	a0,0x7
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:268
80002508:	0c0009c6 	jal	80002718 <WRITESERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:269
8000250c:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:271
80002510:	08000946 	j	80002518 <.DONE>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:272
80002514:	00000000 	nop

80002518 <.DONE>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:275
80002518:	08000891 	j	80002244 <SHELL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/shell.S:276
8000251c:	00000000 	nop

80002520 <FATAL>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:27
80002520:	34040080 	li	a0,0x80
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:28
80002524:	0c0009c6 	jal	80002718 <WRITESERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:29
80002528:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:30
8000252c:	3c028000 	lui	v0,0x8000
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:31
80002530:	2442202c 	addiu	v0,v0,8236
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:32
80002534:	00400008 	jr	v0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:33
80002538:	00000000 	nop

8000253c <EXCEPTIONHANDLER>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:37
8000253c:	401a6000 	mfc0	k0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:38
80002540:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:39
80002544:	3b5b0001 	xori	k1,k0,0x1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:40
80002548:	037ad024 	and	k0,k1,k0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:41
8000254c:	409a6000 	mtc0	k0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:43
80002550:	3c1a807f 	lui	k0,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:44
80002554:	8f5a0088 	lw	k0,136(k0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:46
80002558:	af5d007c 	sw	sp,124(k0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:47
8000255c:	0340e825 	move	sp,k0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:48
80002560:	afa10000 	sw	at,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:49
80002564:	afa20004 	sw	v0,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:50
80002568:	afa30008 	sw	v1,8(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:51
8000256c:	afa4000c 	sw	a0,12(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:52
80002570:	afa50010 	sw	a1,16(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:53
80002574:	afa60014 	sw	a2,20(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:54
80002578:	afa70018 	sw	a3,24(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:55
8000257c:	afa8001c 	sw	t0,28(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:56
80002580:	afa90020 	sw	t1,32(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:57
80002584:	afaa0024 	sw	t2,36(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:58
80002588:	afab0028 	sw	t3,40(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:59
8000258c:	afac002c 	sw	t4,44(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:60
80002590:	afad0030 	sw	t5,48(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:61
80002594:	afae0034 	sw	t6,52(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:62
80002598:	afaf0038 	sw	t7,56(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:63
8000259c:	afb8003c 	sw	t8,60(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:64
800025a0:	afb90040 	sw	t9,64(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:65
800025a4:	afb00044 	sw	s0,68(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:66
800025a8:	afb10048 	sw	s1,72(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:67
800025ac:	afb2004c 	sw	s2,76(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:68
800025b0:	afb30050 	sw	s3,80(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:69
800025b4:	afb40054 	sw	s4,84(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:70
800025b8:	afb50058 	sw	s5,88(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:71
800025bc:	afb6005c 	sw	s6,92(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:72
800025c0:	afb70060 	sw	s7,96(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:73
800025c4:	afbc0064 	sw	gp,100(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:74
800025c8:	afbe0068 	sw	s8,104(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:75
800025cc:	afbf006c 	sw	ra,108(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:76
800025d0:	401a6000 	mfc0	k0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:77
800025d4:	401b6800 	mfc0	k1,c0_cause
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:78
800025d8:	afba0070 	sw	k0,112(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:79
800025dc:	401a7000 	mfc0	k0,c0_epc
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:80
800025e0:	afbb0074 	sw	k1,116(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:81
800025e4:	afba0078 	sw	k0,120(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:84
800025e8:	401a6800 	mfc0	k0,c0_cause
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:85
800025ec:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:86
800025f0:	335b00ff 	andi	k1,k0,0xff
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:87
800025f4:	001bd882 	srl	k1,k1,0x2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:88
800025f8:	341a0000 	li	k0,0x0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:89
800025fc:	137a002c 	beq	k1,k0,800026b0 <WAKEUPSHELL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:90
80002600:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:91
80002604:	341a0008 	li	k0,0x8
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:92
80002608:	137a0032 	beq	k1,k0,800026d4 <SYSCALL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:93
8000260c:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:97
80002610:	08000948 	j	80002520 <FATAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:98
80002614:	00000000 	nop

80002618 <RETURNFRMTRAP>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:102
80002618:	8fba0070 	lw	k0,112(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:103
8000261c:	375a0001 	ori	k0,k0,0x1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:104
80002620:	3b5b0004 	xori	k1,k0,0x4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:105
80002624:	035bd024 	and	k0,k0,k1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:106
80002628:	8fbb0078 	lw	k1,120(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:107
8000262c:	409a6000 	mtc0	k0,c0_status
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:108
80002630:	409b7000 	mtc0	k1,c0_epc
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:109
80002634:	8fa10000 	lw	at,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:110
80002638:	8fa20004 	lw	v0,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:111
8000263c:	8fa30008 	lw	v1,8(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:112
80002640:	8fa4000c 	lw	a0,12(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:113
80002644:	8fa50010 	lw	a1,16(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:114
80002648:	8fa60014 	lw	a2,20(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:115
8000264c:	8fa70018 	lw	a3,24(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:116
80002650:	8fa8001c 	lw	t0,28(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:117
80002654:	8fa90020 	lw	t1,32(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:118
80002658:	8faa0024 	lw	t2,36(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:119
8000265c:	8fab0028 	lw	t3,40(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:120
80002660:	8fac002c 	lw	t4,44(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:121
80002664:	8fad0030 	lw	t5,48(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:122
80002668:	8fae0034 	lw	t6,52(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:123
8000266c:	8faf0038 	lw	t7,56(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:124
80002670:	8fb8003c 	lw	t8,60(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:125
80002674:	8fb90040 	lw	t9,64(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:126
80002678:	8fb00044 	lw	s0,68(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:127
8000267c:	8fb10048 	lw	s1,72(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:128
80002680:	8fb2004c 	lw	s2,76(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:129
80002684:	8fb30050 	lw	s3,80(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:130
80002688:	8fb40054 	lw	s4,84(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:131
8000268c:	8fb50058 	lw	s5,88(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:132
80002690:	8fb6005c 	lw	s6,92(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:133
80002694:	8fb70060 	lw	s7,96(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:134
80002698:	8fbc0064 	lw	gp,100(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:135
8000269c:	8fbe0068 	lw	s8,104(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:136
800026a0:	8fbf006c 	lw	ra,108(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:138
800026a4:	8fbd007c 	lw	sp,124(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:139
800026a8:	42000018 	eret
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:140
800026ac:	00000000 	nop

800026b0 <WAKEUPSHELL>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:145
800026b0:	3c09807f 	lui	t1,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:146
800026b4:	8d290088 	lw	t1,136(t1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:147
800026b8:	3c08807f 	lui	t0,0x807f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:148
800026bc:	8d080080 	lw	t0,128(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:149
800026c0:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:150
800026c4:	1509ffd4 	bne	t0,t1,80002618 <RETURNFRMTRAP>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:151
800026c8:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:152
800026cc:	08000883 	j	8000220c <SCHEDULE>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:153
800026d0:	00000000 	nop

800026d4 <SYSCALL>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:156
800026d4:	8fba0078 	lw	k0,120(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:157
800026d8:	275a0004 	addiu	k0,k0,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:158
800026dc:	afba0078 	sw	k0,120(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:159
800026e0:	34080003 	li	t0,0x3
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:160
800026e4:	10480006 	beq	v0,t0,80002700 <.syscall_wait>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:161
800026e8:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:162
800026ec:	3408001e 	li	t0,0x1e
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:163
800026f0:	10480005 	beq	v0,t0,80002708 <.syscall_putc>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:164
800026f4:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:168
800026f8:	08000986 	j	80002618 <RETURNFRMTRAP>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:169
800026fc:	00000000 	nop

80002700 <.syscall_wait>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:172
80002700:	08000883 	j	8000220c <SCHEDULE>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:173
80002704:	00000000 	nop

80002708 <.syscall_putc>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:176
80002708:	0c0009c6 	jal	80002718 <WRITESERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:177
8000270c:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:178
80002710:	08000986 	j	80002618 <RETURNFRMTRAP>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/trap.S:179
80002714:	00000000 	nop

80002718 <WRITESERIAL>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:15
80002718:	3c09bfe4 	lui	t1,0xbfe4

8000271c <.TESTW>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:17
8000271c:	81280005 	lb	t0,5(t1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:18
80002720:	31080020 	andi	t0,t0,0x20
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:19
80002724:	1100fffd 	beqz	t0,8000271c <.TESTW>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:20
80002728:	00000000 	nop

8000272c <.WSERIAL>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:22
8000272c:	a1240000 	sb	a0,0(t1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:23
80002730:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:24
80002734:	00000000 	nop

80002738 <READSERIAL>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:34
80002738:	3c09bfe4 	lui	t1,0xbfe4

8000273c <.TESTR>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:36
8000273c:	81280005 	lb	t0,5(t1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:37
80002740:	31080001 	andi	t0,t0,0x1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:38
80002744:	15000005 	bnez	t0,8000275c <.RSERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:39
80002748:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:41
8000274c:	34020003 	li	v0,0x3
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:42
80002750:	0000200c 	syscall	0x80
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:44
80002754:	080009cf 	j	8000273c <.TESTR>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:45
80002758:	00000000 	nop

8000275c <.RSERIAL>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:47
8000275c:	81220000 	lb	v0,0(t1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:48
80002760:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:49
80002764:	00000000 	nop

80002768 <READSERIALWORD>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:73
80002768:	27bdffec 	addiu	sp,sp,-20
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:74
8000276c:	afbf0000 	sw	ra,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:75
80002770:	afb00004 	sw	s0,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:76
80002774:	afb10008 	sw	s1,8(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:77
80002778:	afb2000c 	sw	s2,12(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:78
8000277c:	afb30010 	sw	s3,16(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:80
80002780:	0c0009ce 	jal	80002738 <READSERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:81
80002784:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:82
80002788:	00028025 	or	s0,zero,v0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:83
8000278c:	0c0009ce 	jal	80002738 <READSERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:84
80002790:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:85
80002794:	00028825 	or	s1,zero,v0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:86
80002798:	0c0009ce 	jal	80002738 <READSERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:87
8000279c:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:88
800027a0:	00029025 	or	s2,zero,v0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:89
800027a4:	0c0009ce 	jal	80002738 <READSERIAL>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:90
800027a8:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:91
800027ac:	00029825 	or	s3,zero,v0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:93
800027b0:	321000ff 	andi	s0,s0,0xff
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:94
800027b4:	327300ff 	andi	s3,s3,0xff
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:95
800027b8:	325200ff 	andi	s2,s2,0xff
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:96
800027bc:	323100ff 	andi	s1,s1,0xff
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:97
800027c0:	00131025 	or	v0,zero,s3
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:98
800027c4:	00021200 	sll	v0,v0,0x8
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:99
800027c8:	00521025 	or	v0,v0,s2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:100
800027cc:	00021200 	sll	v0,v0,0x8
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:101
800027d0:	00511025 	or	v0,v0,s1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:102
800027d4:	00021200 	sll	v0,v0,0x8
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:103
800027d8:	00501025 	or	v0,v0,s0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:105
800027dc:	8fbf0000 	lw	ra,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:106
800027e0:	8fb00004 	lw	s0,4(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:107
800027e4:	8fb10008 	lw	s1,8(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:108
800027e8:	8fb2000c 	lw	s2,12(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:109
800027ec:	8fb30010 	lw	s3,16(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:110
800027f0:	27bd0014 	addiu	sp,sp,20
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:111
800027f4:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/utils.S:112
800027f8:	00000000 	nop

800027fc <_text_code_end>:
	...

80003000 <_text_test_begin>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:23
80003000:	24420001 	addiu	v0,v0,1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:24
80003004:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:25
80003008:	00000000 	nop

8000300c <UTEST_STREAM>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:32
8000300c:	3c048010 	lui	a0,0x8010
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:33
80003010:	3c058040 	lui	a1,0x8040
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:34
80003014:	3c060030 	lui	a2,0x30
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:35
80003018:	00863021 	addu	a2,a0,a2

8000301c <stream_next>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:37
8000301c:	10860005 	beq	a0,a2,80003034 <stream_end>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:38
80003020:	24a50004 	addiu	a1,a1,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:40
80003024:	8c820000 	lw	v0,0(a0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:41
80003028:	aca2fffc 	sw	v0,-4(a1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:42
8000302c:	1000fffb 	b	8000301c <stream_next>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:43
80003030:	24840004 	addiu	a0,a0,4

80003034 <stream_end>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:46
80003034:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:47
80003038:	00000000 	nop

8000303c <UTEST_MATRIX>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:66
8000303c:	3c048040 	lui	a0,0x8040
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:67
80003040:	3c058041 	lui	a1,0x8041
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:68
80003044:	3c068042 	lui	a2,0x8042
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:69
80003048:	24070060 	li	a3,96
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:78
8000304c:	00001825 	move	v1,zero

80003050 <loop1>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:80
80003050:	1067001a 	beq	v1,a3,800030bc <loop1end>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:81
80003054:	00034080 	sll	t0,v1,0x2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:83
80003058:	00035240 	sll	t2,v1,0x9
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:84
8000305c:	00884021 	addu	t0,a0,t0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:85
80003060:	00aa5021 	addu	t2,a1,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:86
80003064:	00004825 	move	t1,zero

80003068 <loop2>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:88
80003068:	11270012 	beq	t1,a3,800030b4 <loop2end>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:89
8000306c:	00091240 	sll	v0,t1,0x9
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:91
80003070:	8d0f0000 	lw	t7,0(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:92
80003074:	00c21021 	addu	v0,a2,v0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:93
80003078:	01406025 	move	t4,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:94
8000307c:	00005825 	move	t3,zero

80003080 <loop3>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:96
80003080:	11670009 	beq	t3,a3,800030a8 <loop3end>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:97
80003084:	256b0001 	addiu	t3,t3,1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:99
80003088:	8d8d0000 	lw	t5,0(t4)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:100
8000308c:	8c4e0000 	lw	t6,0(v0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:101
80003090:	71ed6802 	mul	t5,t7,t5
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:102
80003094:	24420004 	addiu	v0,v0,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:103
80003098:	258c0004 	addiu	t4,t4,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:104
8000309c:	01cd6821 	addu	t5,t6,t5
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:105
800030a0:	1000fff7 	b	80003080 <loop3>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:106
800030a4:	ac4dfffc 	sw	t5,-4(v0)

800030a8 <loop3end>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:109
800030a8:	25290001 	addiu	t1,t1,1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:110
800030ac:	1000ffee 	b	80003068 <loop2>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:111
800030b0:	25080200 	addiu	t0,t0,512

800030b4 <loop2end>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:114
800030b4:	1000ffe6 	b	80003050 <loop1>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:115
800030b8:	24630001 	addiu	v1,v1,1

800030bc <loop1end>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:118
800030bc:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:119
800030c0:	00000000 	nop

800030c4 <UTEST_CRYPTONIGHT>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:148
800030c4:	3c048040 	lui	a0,0x8040
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:149
800030c8:	3c05dead 	lui	a1,0xdead
800030cc:	34a5beef 	ori	a1,a1,0xbeef
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:150
800030d0:	3c06face 	lui	a2,0xface
800030d4:	34c6b00c 	ori	a2,a2,0xb00c
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:151
800030d8:	3c070010 	lui	a3,0x10
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:152
800030dc:	00041825 	or	v1,zero,a0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:153
800030e0:	00001025 	move	v0,zero
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:154
800030e4:	3c080008 	lui	t0,0x8

800030e8 <fill_next>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:156
800030e8:	ac620000 	sw	v0,0(v1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:157
800030ec:	24420001 	addiu	v0,v0,1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:158
800030f0:	1448fffd 	bne	v0,t0,800030e8 <fill_next>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:159
800030f4:	24630004 	addiu	v1,v1,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:161
800030f8:	00004825 	move	t1,zero
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:162
800030fc:	3c0a0007 	lui	t2,0x7
80003100:	354affff 	ori	t2,t2,0xffff

80003104 <crn_hext>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:164
80003104:	00aa4024 	and	t0,a1,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:165
80003108:	00084080 	sll	t0,t0,0x2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:166
8000310c:	00884021 	addu	t0,a0,t0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:167
80003110:	8d020000 	lw	v0,0(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:168
80003114:	00051842 	srl	v1,a1,0x1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:169
80003118:	00021040 	sll	v0,v0,0x1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:170
8000311c:	00431026 	xor	v0,v0,v1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:171
80003120:	004a1824 	and	v1,v0,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:172
80003124:	00463026 	xor	a2,v0,a2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:173
80003128:	00031880 	sll	v1,v1,0x2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:174
8000312c:	ad060000 	sw	a2,0(t0)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:175
80003130:	00831821 	addu	v1,a0,v1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:176
80003134:	8c680000 	lw	t0,0(v1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:177
80003138:	00023025 	or	a2,zero,v0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:178
8000313c:	70481002 	mul	v0,v0,t0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:179
80003140:	25290001 	addiu	t1,t1,1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:180
80003144:	00452821 	addu	a1,v0,a1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:181
80003148:	ac650000 	sw	a1,0(v1)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:182
8000314c:	14e9ffed 	bne	a3,t1,80003104 <crn_hext>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:183
80003150:	01052826 	xor	a1,t0,a1

80003154 <crn_end>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:185
80003154:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:186
80003158:	00000000 	nop

8000315c <UTEST_PUTC>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:194
8000315c:	3402001e 	li	v0,0x1e
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:195
80003160:	3404004f 	li	a0,0x4f
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:196
80003164:	0000200c 	syscall	0x80
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:197
80003168:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:198
8000316c:	3404004b 	li	a0,0x4b
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:199
80003170:	0000200c 	syscall	0x80
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:200
80003174:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:201
80003178:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:202
8000317c:	00000000 	nop

80003180 <UTEST_1PTB>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:216
80003180:	3c080400 	lui	t0,0x400
	...
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:221
80003190:	2508ffff 	addiu	t0,t0,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:222
80003194:	34090000 	li	t1,0x0
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:223
80003198:	340a0001 	li	t2,0x1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:224
8000319c:	340b0002 	li	t3,0x2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:225
800031a0:	1500fffb 	bnez	t0,80003190 <UTEST_1PTB+0x10>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:226
800031a4:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:227
800031a8:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:228
800031ac:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:229
800031b0:	00000000 	nop

800031b4 <UTEST_2DCT>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:238
800031b4:	3c080100 	lui	t0,0x100
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:239
800031b8:	34090001 	li	t1,0x1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:240
800031bc:	340a0002 	li	t2,0x2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:241
800031c0:	340b0003 	li	t3,0x3
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:243
800031c4:	01495026 	xor	t2,t2,t1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:244
800031c8:	012a4826 	xor	t1,t1,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:245
800031cc:	01495026 	xor	t2,t2,t1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:246
800031d0:	016a5826 	xor	t3,t3,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:247
800031d4:	014b5026 	xor	t2,t2,t3
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:248
800031d8:	016a5826 	xor	t3,t3,t2
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:249
800031dc:	012b4826 	xor	t1,t1,t3
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:250
800031e0:	01695826 	xor	t3,t3,t1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:251
800031e4:	012b4826 	xor	t1,t1,t3
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:252
800031e8:	2508ffff 	addiu	t0,t0,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:253
800031ec:	1500fff5 	bnez	t0,800031c4 <UTEST_2DCT+0x10>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:254
800031f0:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:255
800031f4:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:256
800031f8:	00000000 	nop

800031fc <UTEST_3CCT>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:265
800031fc:	3c080400 	lui	t0,0x400
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:267
80003200:	15000003 	bnez	t0,80003210 <UTEST_3CCT+0x14>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:268
80003204:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:269
80003208:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:270
8000320c:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:272
80003210:	08000c86 	j	80003218 <UTEST_3CCT+0x1c>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:273
80003214:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:275
80003218:	2508ffff 	addiu	t0,t0,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:276
8000321c:	08000c80 	j	80003200 <UTEST_3CCT+0x4>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:277
80003220:	2508ffff 	addiu	t0,t0,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:278
80003224:	00000000 	nop

80003228 <UTEST_4MDCT>:
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:287
80003228:	3c080200 	lui	t0,0x200
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:288
8000322c:	27bdfffc 	addiu	sp,sp,-4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:290
80003230:	afa80000 	sw	t0,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:291
80003234:	8fa90000 	lw	t1,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:292
80003238:	2529ffff 	addiu	t1,t1,-1
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:293
8000323c:	afa90000 	sw	t1,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:294
80003240:	8fa80000 	lw	t0,0(sp)
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:295
80003244:	1500fffa 	bnez	t0,80003230 <UTEST_4MDCT+0x8>
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:296
80003248:	00000000 	nop
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:297
8000324c:	27bd0004 	addiu	sp,sp,4
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:298
80003250:	03e00008 	jr	ra
/home/alkaid/source/Phoenix/tests/system/kernel/kern/test.S:299
80003254:	00000000 	nop

Disassembly of section .reginfo:

00000000 <.reginfo>:
   0:	14000f00 	bnez	zero,3c04 <INITLOCATE-0x7fffc3fc>
   4:	00000000 	nop
	...

Disassembly of section .gnu.attributes:

00000000 <.gnu.attributes>:
   0:	00000f41 	0xf41
   4:	756e6700 	jalx	5b99c00 <INITLOCATE-0x7a466400>
   8:	00070100 	sll	zero,a3,0x4
   c:	05040000 	0x5040000

Disassembly of section .debug_line:

00000000 <.debug_line>:
   0:	0000007b 	0x7b
   4:	00220002 	ror	zero,v0,0x0
   8:	01010000 	0x1010000
   c:	000d0efb 	0xd0efb
  10:	01010101 	0x1010101
  14:	01000000 	0x1000000
  18:	6b010000 	0x6b010000
  1c:	006e7265 	0x6e7265
  20:	65766500 	0x65766500
  24:	00532e63 	0x532e63
  28:	00000001 	movf	zero,zero,$fcc0
  2c:	00020500 	sll	zero,v0,0x14
  30:	03800000 	0x3800000
  34:	8383010d 	lb	v1,269(gp)
  38:	4b4b4b4b 	c2	0x14b4b4b
  3c:	4b4b4b4c 	c2	0x14b4b4c
  40:	834d4b4b 	lb	t5,19275(k0)
  44:	4b4b4b83 	c2	0x14b4b83
  48:	4b4b4c4b 	c2	0x14b4c4b
  4c:	4d4b4b4b 	0x4d4b4b4b
  50:	4b4b8383 	c2	0x14b8383
  54:	4b4c4b4b 	c2	0x14c4b4b
  58:	4b4b4b4b 	c2	0x14b4b4b
  5c:	4b4b4b4e 	c2	0x14b4b4e
  60:	1705c002 	bne	t8,a1,ffff006c <KERNEL_STACK_INIT+0x7f7f006c>
  64:	024b4b4b 	0x24b4b4b
  68:	01010004 	sllv	zero,at,t0
  6c:	80020500 	lb	v0,1280(zero)
  70:	03800011 	mthi	gp
  74:	4b0100d2 	c2	0x10100d2
  78:	04024b4b 	bltzl	zero,12da8 <INITLOCATE-0x7ffed258>
  7c:	b3010100 	0xb3010100
  80:	02000000 	0x2000000
  84:	00002200 	sll	a0,zero,0x8
  88:	fb010100 	sdc2	$1,256(t8)
  8c:	01000d0e 	0x1000d0e
  90:	00010101 	0x10101
  94:	00010000 	sll	zero,at,0x0
  98:	656b0100 	0x656b0100
  9c:	00006e72 	tlt	zero,zero,0x1b9
  a0:	74696e69 	jalx	1a5b9a4 <INITLOCATE-0x7e5a465c>
  a4:	0100532e 	0x100532e
  a8:	00000000 	nop
  ac:	202c0205 	addi	t4,at,517
  b0:	31038000 	andi	v1,t0,0x8000
  b4:	4b4b4b01 	c2	0x14b4b01
  b8:	4b4b4b4c 	c2	0x14b4b4c
  bc:	4b4e4b4b 	c2	0x14e4b4b
  c0:	4b4b4d4b 	c2	0x14b4d4b
  c4:	4b4b4b4c 	c2	0x14b4b4c
  c8:	4b4b4b4b 	c2	0x14b4b4b
  cc:	4b4b4b4b 	c2	0x14b4b4b
  d0:	4b4b4b4c 	c2	0x14b4b4c
  d4:	4b4b4b4b 	c2	0x14b4b4b
  d8:	4a15034b 	c2	0x15034b
  dc:	4b4e4b4d 	c2	0x14e4b4d
  e0:	4b4b4b4c 	c2	0x14b4b4c
  e4:	4b4b4b4c 	c2	0x14b4b4c
  e8:	4b4f4b4b 	c2	0x14f4b4b
  ec:	4b4c4e4b 	c2	0x14c4e4b
  f0:	4b4b4b4b 	c2	0x14b4b4b
  f4:	4b4c4b4b 	c2	0x14c4b4b
  f8:	4b4b4b4b 	c2	0x14b4b4b
  fc:	4c4c4b4b 	0x4c4c4b4b
 100:	4b4b4b4c 	c2	0x14b4b4c
 104:	4b4b4b4b 	c2	0x14b4b4b
 108:	4b4b4c4b 	c2	0x14b4c4b
 10c:	eb034b4b 	swc2	$3,19275(t8)
 110:	4b4b4a00 	c2	0x14b4a00
 114:	4b4b4b4b 	c2	0x14b4b4b
 118:	4b4f4b4d 	c2	0x14f4b4d
 11c:	4b4b4c4b 	c2	0x14b4c4b
 120:	4b4b4b4b 	c2	0x14b4b4b
 124:	4b4b4f4b 	c2	0x14b4f4b
 128:	4b4b4b4b 	c2	0x14b4b4b
 12c:	4b4b4b4b 	c2	0x14b4b4b
 130:	0004024b 	0x4024b
 134:	00450101 	0x450101
 138:	00020000 	sll	zero,v0,0x0
 13c:	00000023 	negu	zero,zero
 140:	0efb0101 	jal	bec0404 <INITLOCATE-0x7413fbfc>
 144:	0101000d 	break	0x101
 148:	00000101 	0x101
 14c:	00000100 	sll	zero,zero,0x4
 150:	72656b01 	0x72656b01
 154:	7300006e 	0x7300006e
 158:	64656863 	0x64656863
 15c:	0100532e 	0x100532e
 160:	00000000 	nop
 164:	220c0205 	addi	t4,s0,517
 168:	0d038000 	jal	40e0000 <INITLOCATE-0x7bf20000>
 16c:	4b4b4b01 	c2	0x14b4b01
 170:	4b4b4b4b 	c2	0x14b4b4b
 174:	4b4c4b4b 	c2	0x14c4b4b
 178:	04024b4b 	bltzl	zero,12ea8 <INITLOCATE-0x7ffed158>
 17c:	f0010100 	0xf0010100
 180:	02000000 	0x2000000
 184:	00002300 	sll	a0,zero,0xc
 188:	fb010100 	sdc2	$1,256(t8)
 18c:	01000d0e 	0x1000d0e
 190:	00010101 	0x10101
 194:	00010000 	sll	zero,at,0x0
 198:	656b0100 	0x656b0100
 19c:	00006e72 	tlt	zero,zero,0x1b9
 1a0:	6c656873 	0x6c656873
 1a4:	00532e6c 	0x532e6c
 1a8:	00000001 	movf	zero,zero,$fcc0
 1ac:	44020500 	0x44020500
 1b0:	03800022 	sub	zero,gp,zero
 1b4:	4c4b011a 	0x4c4b011a
 1b8:	4b4b4b4b 	c2	0x14b4b4b
 1bc:	4b4b4b4b 	c2	0x14b4b4b
 1c0:	4b4b4b4b 	c2	0x14b4b4b
 1c4:	4b4b4b4b 	c2	0x14b4b4b
 1c8:	4b4b4b4d 	c2	0x14b4b4d
 1cc:	4a11034b 	c2	0x11034b
 1d0:	4d4b4b4b 	0x4d4b4b4b
 1d4:	4b4b4c4b 	c2	0x14b4c4b
 1d8:	4b4b4b4b 	c2	0x14b4b4b
 1dc:	4c4b4b4c 	0x4c4b4b4c
 1e0:	4b4b4d4b 	c2	0x14b4d4b
 1e4:	4b4c4b4c 	c2	0x14c4b4c
 1e8:	4b4b4b4b 	c2	0x14b4b4b
 1ec:	4b4b4c4b 	c2	0x14b4c4b
 1f0:	4b4d4b4b 	c2	0x14d4b4b
 1f4:	4b4b4c4b 	c2	0x14b4c4b
 1f8:	4d4b4b4b 	0x4d4b4b4b
 1fc:	4b4b4b4b 	c2	0x14b4b4b
 200:	4b4c4b4b 	c2	0x14c4b4b
 204:	4d4b4b4b 	0x4d4b4b4b
 208:	4b4c4b4b 	c2	0x14c4b4b
 20c:	4b4b4b4b 	c2	0x14b4b4b
 210:	4b4b4c4b 	c2	0x14b4c4b
 214:	4b4b4b4b 	c2	0x14b4b4b
 218:	4b4b4b4c 	c2	0x14b4b4c
 21c:	4c4b4d4b 	0x4c4b4d4b
 220:	4f4d4b4b 	c3	0x14d4b4b
 224:	4c4b4b4b 	0x4c4b4b4b
 228:	4b4b4b4b 	c2	0x14b4b4b
 22c:	4b4b4b4b 	c2	0x14b4b4b
 230:	4b4b4b4b 	c2	0x14b4b4b
 234:	4b4b4b4b 	c2	0x14b4b4b
 238:	4b4b4b4b 	c2	0x14b4b4b
 23c:	4b4b4b4b 	c2	0x14b4b4b
 240:	4c4b4b4d 	0x4c4b4b4d
 244:	504c4b4b 	beql	v0,t4,12f74 <INITLOCATE-0x7ffed08c>
 248:	4b4c4b4c 	c2	0x14c4b4c
 24c:	4b4b4b4b 	c2	0x14b4b4b
 250:	4b4b4b4b 	c2	0x14b4b4b
 254:	4b4b4b4b 	c2	0x14b4b4b
 258:	4b4b4b4b 	c2	0x14b4b4b
 25c:	4b4b4b4b 	c2	0x14b4b4b
 260:	4d4b4b4b 	0x4d4b4b4b
 264:	4b4c4b4b 	c2	0x14c4b4b
 268:	4b4c4b4b 	c2	0x14c4b4b
 26c:	04024b4d 	bltzl	zero,12fa4 <INITLOCATE-0x7ffed05c>
 270:	d7010100 	ldc1	$f1,256(t8)
 274:	02000000 	0x2000000
 278:	00002200 	sll	a0,zero,0x8
 27c:	fb010100 	sdc2	$1,256(t8)
 280:	01000d0e 	0x1000d0e
 284:	00010101 	0x10101
 288:	00010000 	sll	zero,at,0x0
 28c:	656b0100 	0x656b0100
 290:	00006e72 	tlt	zero,zero,0x1b9
 294:	74736574 	jalx	1cd95d0 <INITLOCATE-0x7e326a30>
 298:	0100532e 	0x100532e
 29c:	00000000 	nop
 2a0:	30000205 	andi	zero,zero,0x205
 2a4:	16038000 	bne	s0,v1,fffe02a8 <KERNEL_STACK_INIT+0x7f7e02a8>
 2a8:	514b4b01 	beql	t2,t3,12eb0 <INITLOCATE-0x7ffed150>
 2ac:	4c4b4b4b 	0x4c4b4b4b
 2b0:	4b4b4c4b 	c2	0x14b4c4b
 2b4:	034b4d4b 	0x34b4d4b
 2b8:	4b4b4a13 	c2	0x14b4a13
 2bc:	4a09034b 	c2	0x9034b
 2c0:	4b4c4b4c 	c2	0x14c4b4c
 2c4:	4b4c4b4b 	c2	0x14c4b4b
 2c8:	4b4b4b4c 	c2	0x14b4b4c
 2cc:	4b4c4b4c 	c2	0x14c4b4c
 2d0:	4b4b4b4b 	c2	0x14b4b4b
 2d4:	4b4d4b4b 	c2	0x14d4b4b
 2d8:	4d4b4d4b 	0x4d4b4d4b
 2dc:	4a1d034b 	c2	0x1d034b
 2e0:	4b83834b 	c2	0x183834b
 2e4:	4b4c4b4b 	c2	0x14c4b4b
 2e8:	4b4c4b4b 	c2	0x14c4b4b
 2ec:	4b4b4b84 	c2	0x14b4b84
 2f0:	4b4b4b4b 	c2	0x14b4b4b
 2f4:	4b4b4b4b 	c2	0x14b4b4b
 2f8:	4b4b4b4b 	c2	0x14b4b4b
 2fc:	4b4b4b4b 	c2	0x14b4b4b
 300:	4b524b4c 	c2	0x1524b4c
 304:	4b4b4b4b 	c2	0x14b4b4b
 308:	034b4b4b 	0x34b4b4b
 30c:	4b4b4a0e 	c2	0x14b4a0e
 310:	4b4b4c4b 	c2	0x14b4c4b
 314:	4b4b4b4b 	c2	0x14b4b4b
 318:	09034b4b 	j	40d2d2c <INITLOCATE-0x7bf2d2d4>
 31c:	4b4b4b4a 	c2	0x14b4b4a
 320:	4b4b4b4c 	c2	0x14b4b4c
 324:	4b4b4b4b 	c2	0x14b4b4b
 328:	4b4b4b4b 	c2	0x14b4b4b
 32c:	09034b4b 	j	40d2d2c <INITLOCATE-0x7bf2d2d4>
 330:	4b4b4c4a 	c2	0x14b4c4a
 334:	4c4b4c4b 	0x4c4b4c4b
 338:	034b4b4b 	0x34b4b4b
 33c:	4c4b4a09 	0x4c4b4a09
 340:	4b4b4b4b 	c2	0x14b4b4b
 344:	4b4b4b4b 	c2	0x14b4b4b
 348:	0004024b 	0x4024b
 34c:	00b40101 	0xb40101
 350:	00020000 	sll	zero,v0,0x0
 354:	00000022 	neg	zero,zero
 358:	0efb0101 	jal	bec0404 <INITLOCATE-0x7413fbfc>
 35c:	0101000d 	break	0x101
 360:	00000101 	0x101
 364:	00000100 	sll	zero,zero,0x4
 368:	72656b01 	0x72656b01
 36c:	7400006e 	jalx	1b8 <INITLOCATE-0x7ffffe48>
 370:	2e706172 	sltiu	s0,s3,24946
 374:	00010053 	0x10053
 378:	05000000 	bltz	t0,37c <INITLOCATE-0x7ffffc84>
 37c:	00252002 	ror	a0,a1,0x0
 380:	011a0380 	0x11a0380
 384:	4b4b4b4b 	c2	0x14b4b4b
 388:	4b4e4b4b 	c2	0x14e4b4b
 38c:	4c4b4b4b 	0x4c4b4b4b
 390:	4b4b4c4b 	c2	0x14b4c4b
 394:	4b4b4b4b 	c2	0x14b4b4b
 398:	4b4b4b4b 	c2	0x14b4b4b
 39c:	4b4b4b4b 	c2	0x14b4b4b
 3a0:	4b4b4b4b 	c2	0x14b4b4b
 3a4:	4b4b4b4b 	c2	0x14b4b4b
 3a8:	4b4b4b4b 	c2	0x14b4b4b
 3ac:	4b4b4b4b 	c2	0x14b4b4b
 3b0:	4b4b4b4b 	c2	0x14b4b4b
 3b4:	4b4b4d4b 	c2	0x14b4d4b
 3b8:	4b4b4b4b 	c2	0x14b4b4b
 3bc:	4e4b4b4b 	c3	0x4b4b4b
 3c0:	4b4b4e4b 	c2	0x14b4e4b
 3c4:	4b4b4b4b 	c2	0x14b4b4b
 3c8:	4b4b4b4b 	c2	0x14b4b4b
 3cc:	4b4b4b4b 	c2	0x14b4b4b
 3d0:	4b4b4b4b 	c2	0x14b4b4b
 3d4:	4b4b4b4b 	c2	0x14b4b4b
 3d8:	4b4b4b4b 	c2	0x14b4b4b
 3dc:	4b4b4b4b 	c2	0x14b4b4b
 3e0:	4b4b4b4b 	c2	0x14b4b4b
 3e4:	4f4b4b4c 	c3	0x14b4b4c
 3e8:	4b4b4b4b 	c2	0x14b4b4b
 3ec:	4b4b4b4b 	c2	0x14b4b4b
 3f0:	4b4b4b4d 	c2	0x14b4b4d
 3f4:	4b4b4b4b 	c2	0x14b4b4b
 3f8:	4d4b4e4b 	0x4d4b4e4b
 3fc:	4b4b4d4b 	c2	0x14b4d4b
 400:	0004024b 	0x4024b
 404:	00740101 	0x740101
 408:	00020000 	sll	zero,v0,0x0
 40c:	00000023 	negu	zero,zero
 410:	0efb0101 	jal	bec0404 <INITLOCATE-0x7413fbfc>
 414:	0101000d 	break	0x101
 418:	00000101 	0x101
 41c:	00000100 	sll	zero,zero,0x4
 420:	72656b01 	0x72656b01
 424:	7500006e 	jalx	40001b8 <INITLOCATE-0x7bfffe48>
 428:	736c6974 	q16sll	xr5,xr10,xr1,xr11,13
 42c:	0100532e 	0x100532e
 430:	00000000 	nop
 434:	27180205 	addiu	t8,t8,517
 438:	0e038000 	jal	80e0000 <INITLOCATE-0x77f20000>
 43c:	4b4b4c01 	c2	0x14b4c01
 440:	4b4b4c4b 	c2	0x14b4c4b
 444:	4c4a0a03 	0x4c4a0a03
 448:	4c4b4b4b 	0x4c4b4b4b
 44c:	4c4b4c4b 	0x4c4b4c4b
 450:	18034b4b 	0x18034b4b
 454:	4b4b4b4a 	c2	0x14b4b4a
 458:	4b4c4b4b 	c2	0x14c4b4b
 45c:	4b4b4b4b 	c2	0x14b4b4b
 460:	4b4b4b4b 	c2	0x14b4b4b
 464:	4b4c4b4b 	c2	0x14c4b4b
 468:	4b4b4b4b 	c2	0x14b4b4b
 46c:	4b4b4b4b 	c2	0x14b4b4b
 470:	4b4b4c4b 	c2	0x14b4c4b
 474:	4b4b4b4b 	c2	0x14b4b4b
 478:	0004024b 	0x4024b
 47c:	地址 0x000000000000047c 越界。


Disassembly of section .debug_info:

00000000 <.debug_info>:
   0:	0000005d 	0x5d
   4:	00000002 	srl	zero,zero,0x0
   8:	01040000 	0x1040000
	...
  14:	6e72656b 	0x6e72656b
  18:	6576652f 	0x6576652f
  1c:	00532e63 	0x532e63
  20:	6d6f682f 	0x6d6f682f
  24:	6c612f65 	0x6c612f65
  28:	6469616b 	0x6469616b
  2c:	756f732f 	jalx	5bdccbc <INITLOCATE-0x7a423344>
  30:	2f656372 	sltiu	a1,k1,25458
  34:	656f6850 	0x656f6850
  38:	2f78696e 	sltiu	t8,k1,26990
  3c:	74736574 	jalx	1cd95d0 <INITLOCATE-0x7e326a30>
  40:	79732f73 	0x79732f73
  44:	6d657473 	0x6d657473
  48:	72656b2f 	0x72656b2f
  4c:	006c656e 	0x6c656e
  50:	20554e47 	addi	s5,v0,20039
  54:	32205341 	andi	zero,s1,0x5341
  58:	2e34322e 	sltiu	s4,s1,12846
  5c:	01003039 	0x1003039
  60:	00006180 	sll	t4,zero,0x6
  64:	12000200 	beqz	s0,868 <INITLOCATE-0x7ffff798>
  68:	04000000 	bltz	zero,6c <INITLOCATE-0x7fffff94>
  6c:	00007f01 	0x7f01
  70:	00200000 	0x200000
  74:	00220c80 	0x220c80
  78:	72656b80 	0x72656b80
  7c:	6e692f6e 	0x6e692f6e
  80:	532e7469 	beql	t9,t6,1d228 <INITLOCATE-0x7ffe2dd8>
  84:	6f682f00 	0x6f682f00
  88:	612f656d 	0x612f656d
  8c:	69616b6c 	0x69616b6c
  90:	6f732f64 	0x6f732f64
  94:	65637275 	0x65637275
  98:	6f68502f 	0x6f68502f
  9c:	78696e65 	st.h	$w25,210(t5)
  a0:	7365742f 	0x7365742f
  a4:	732f7374 	q16sll	xr13,xr12,xr13,xr11,12
  a8:	65747379 	0x65747379
  ac:	656b2f6d 	0x656b2f6d
  b0:	6c656e72 	0x6c656e72
  b4:	554e4700 	bnel	t2,t6,11cb8 <INITLOCATE-0x7ffee348>
  b8:	20534120 	addi	s3,v0,16672
  bc:	34322e32 	ori	s2,at,0x2e32
  c0:	0030392e 	0x30392e
  c4:	00628001 	0x628001
  c8:	00020000 	sll	zero,v0,0x0
  cc:	00000026 	xor	zero,zero,zero
  d0:	01360104 	0x1360104
  d4:	220c0000 	addi	t4,s0,0
  d8:	22448000 	addi	a0,s2,-32768
  dc:	656b8000 	0x656b8000
  e0:	732f6e72 	0x732f6e72
  e4:	64656863 	0x64656863
  e8:	2f00532e 	sltiu	zero,t8,21294
  ec:	656d6f68 	0x656d6f68
  f0:	6b6c612f 	0x6b6c612f
  f4:	2f646961 	sltiu	a0,k1,26977
  f8:	72756f73 	d32sar	xr13,xr11,xr5,xr13,9
  fc:	502f6563 	beql	at,t7,1968c <INITLOCATE-0x7ffe6974>
 100:	6e656f68 	0x6e656f68
 104:	742f7869 	jalx	bde1a4 <INITLOCATE-0x7f421e5c>
 108:	73747365 	s8sdi	xr13,k1,28,
 10c:	7379732f 	0x7379732f
 110:	2f6d6574 	sltiu	t5,k1,25972
 114:	6e72656b 	0x6e72656b
 118:	47006c65 	bz.b	$w0,1b2b0 <INITLOCATE-0x7ffe4d50>
 11c:	4120554e 	0x4120554e
 120:	2e322053 	sltiu	s2,s1,8275
 124:	392e3432 	xori	t6,t1,0x3432
 128:	80010030 	lb	at,48(zero)
 12c:	00000062 	0x62
 130:	003a0002 	ror	zero,k0,0x0
 134:	01040000 	0x1040000
 138:	0000017f 	0x17f
 13c:	80002244 	lb	zero,8772(zero)
 140:	80002520 	lb	zero,9504(zero)
 144:	6e72656b 	0x6e72656b
 148:	6568732f 	0x6568732f
 14c:	532e6c6c 	beql	t9,t6,1b300 <INITLOCATE-0x7ffe4d00>
 150:	6f682f00 	0x6f682f00
 154:	612f656d 	0x612f656d
 158:	69616b6c 	0x69616b6c
 15c:	6f732f64 	0x6f732f64
 160:	65637275 	0x65637275
 164:	6f68502f 	0x6f68502f
 168:	78696e65 	st.h	$w25,210(t5)
 16c:	7365742f 	0x7365742f
 170:	732f7374 	q16sll	xr13,xr12,xr13,xr11,12
 174:	65747379 	0x65747379
 178:	656b2f6d 	0x656b2f6d
 17c:	6c656e72 	0x6c656e72
 180:	554e4700 	bnel	t2,t6,11d84 <INITLOCATE-0x7ffee27c>
 184:	20534120 	addi	s3,v0,16672
 188:	34322e32 	ori	s2,at,0x2e32
 18c:	0030392e 	0x30392e
 190:	00618001 	movt	s0,v1,$fcc0
 194:	00020000 	sll	zero,v0,0x0
 198:	0000004e 	0x4e
 19c:	02730104 	0x2730104
 1a0:	30000000 	andi	zero,zero,0x0
 1a4:	32588000 	andi	t8,s2,0x8000
 1a8:	656b8000 	0x656b8000
 1ac:	742f6e72 	jalx	bdb9c8 <INITLOCATE-0x7f424638>
 1b0:	2e747365 	sltiu	s4,s3,29541
 1b4:	682f0053 	0x682f0053
 1b8:	2f656d6f 	sltiu	a1,k1,28015
 1bc:	616b6c61 	0x616b6c61
 1c0:	732f6469 	0x732f6469
 1c4:	6372756f 	0x6372756f
 1c8:	68502f65 	0x68502f65
 1cc:	696e656f 	0x696e656f
 1d0:	65742f78 	0x65742f78
 1d4:	2f737473 	sltiu	s3,k1,29811
 1d8:	74737973 	jalx	1cde5cc <INITLOCATE-0x7e321a34>
 1dc:	6b2f6d65 	0x6b2f6d65
 1e0:	656e7265 	0x656e7265
 1e4:	4e47006c 	c3	0x47006c
 1e8:	53412055 	beql	k0,at,8340 <INITLOCATE-0x7fff7cc0>
 1ec:	322e3220 	andi	t6,s1,0x3220
 1f0:	30392e34 	andi	t9,at,0x2e34
 1f4:	61800100 	0x61800100
 1f8:	02000000 	0x2000000
 1fc:	00006200 	sll	t4,zero,0x8
 200:	4e010400 	lwxc1	$f16,at(s0)
 204:	20000003 	addi	zero,zero,3
 208:	18800025 	blez	a0,2a0 <INITLOCATE-0x7ffffd60>
 20c:	6b800027 	0x6b800027
 210:	2f6e7265 	sltiu	t6,k1,29285
 214:	70617274 	q16sll	xr9,xr12,xr5,xr8,1
 218:	2f00532e 	sltiu	zero,t8,21294
 21c:	656d6f68 	0x656d6f68
 220:	6b6c612f 	0x6b6c612f
 224:	2f646961 	sltiu	a0,k1,26977
 228:	72756f73 	d32sar	xr13,xr11,xr5,xr13,9
 22c:	502f6563 	beql	at,t7,197bc <INITLOCATE-0x7ffe6844>
 230:	6e656f68 	0x6e656f68
 234:	742f7869 	jalx	bde1a4 <INITLOCATE-0x7f421e5c>
 238:	73747365 	s8sdi	xr13,k1,28,
 23c:	7379732f 	0x7379732f
 240:	2f6d6574 	sltiu	t5,k1,25972
 244:	6e72656b 	0x6e72656b
 248:	47006c65 	bz.b	$w0,1b3e0 <INITLOCATE-0x7ffe4c20>
 24c:	4120554e 	0x4120554e
 250:	2e322053 	sltiu	s2,s1,8275
 254:	392e3432 	xori	t6,t1,0x3432
 258:	80010030 	lb	at,48(zero)
 25c:	00000062 	0x62
 260:	00760002 	0x760002
 264:	01040000 	0x1040000
 268:	00000406 	0x406
 26c:	80002718 	lb	zero,10008(zero)
 270:	800027fc 	lb	zero,10236(zero)
 274:	6e72656b 	0x6e72656b
 278:	6974752f 	0x6974752f
 27c:	532e736c 	beql	t9,t6,1d030 <INITLOCATE-0x7ffe2fd0>
 280:	6f682f00 	0x6f682f00
 284:	612f656d 	0x612f656d
 288:	69616b6c 	0x69616b6c
 28c:	6f732f64 	0x6f732f64
 290:	65637275 	0x65637275
 294:	6f68502f 	0x6f68502f
 298:	78696e65 	st.h	$w25,210(t5)
 29c:	7365742f 	0x7365742f
 2a0:	732f7374 	q16sll	xr13,xr12,xr13,xr11,12
 2a4:	65747379 	0x65747379
 2a8:	656b2f6d 	0x656b2f6d
 2ac:	6c656e72 	0x6c656e72
 2b0:	554e4700 	bnel	t2,t6,11eb4 <INITLOCATE-0x7ffee14c>
 2b4:	20534120 	addi	s3,v0,16672
 2b8:	34322e32 	ori	s2,at,0x2e32
 2bc:	0030392e 	0x30392e
 2c0:	地址 0x00000000000002c0 越界。


Disassembly of section .debug_abbrev:

00000000 <.debug_abbrev>:
   0:	10001101 	b	4408 <INITLOCATE-0x7fffbbf8>
   4:	03065506 	0x3065506
   8:	25081b08 	addiu	t0,t0,6920
   c:	00051308 	0x51308
  10:	11010000 	beq	t0,at,14 <INITLOCATE-0x7fffffec>
  14:	11061000 	beq	t0,a2,4018 <INITLOCATE-0x7fffbfe8>
  18:	03011201 	0x3011201
  1c:	25081b08 	addiu	t0,t0,6920
  20:	00051308 	0x51308
  24:	11010000 	beq	t0,at,28 <INITLOCATE-0x7fffffd8>
  28:	11061000 	beq	t0,a2,402c <INITLOCATE-0x7fffbfd4>
  2c:	03011201 	0x3011201
  30:	25081b08 	addiu	t0,t0,6920
  34:	00051308 	0x51308
  38:	11010000 	beq	t0,at,3c <INITLOCATE-0x7fffffc4>
  3c:	11061000 	beq	t0,a2,4040 <INITLOCATE-0x7fffbfc0>
  40:	03011201 	0x3011201
  44:	25081b08 	addiu	t0,t0,6920
  48:	00051308 	0x51308
  4c:	11010000 	beq	t0,at,50 <INITLOCATE-0x7fffffb0>
  50:	11061000 	beq	t0,a2,4054 <INITLOCATE-0x7fffbfac>
  54:	03011201 	0x3011201
  58:	25081b08 	addiu	t0,t0,6920
  5c:	00051308 	0x51308
  60:	11010000 	beq	t0,at,64 <INITLOCATE-0x7fffff9c>
  64:	11061000 	beq	t0,a2,4068 <INITLOCATE-0x7fffbf98>
  68:	03011201 	0x3011201
  6c:	25081b08 	addiu	t0,t0,6920
  70:	00051308 	0x51308
  74:	11010000 	beq	t0,at,78 <INITLOCATE-0x7fffff88>
  78:	11061000 	beq	t0,a2,407c <INITLOCATE-0x7fffbf84>
  7c:	03011201 	0x3011201
  80:	25081b08 	addiu	t0,t0,6920
  84:	00051308 	0x51308
	...

Disassembly of section .debug_aranges:

00000000 <.debug_aranges>:
   0:	00000024 	and	zero,zero,zero
   4:	00000002 	srl	zero,zero,0x0
   8:	00040000 	sll	zero,a0,0x0
   c:	00000000 	nop
  10:	80000000 	lb	zero,0(zero)
  14:	00000390 	0x390
  18:	80001180 	lb	zero,4480(zero)
  1c:	00000010 	mfhi	zero
	...
  28:	0000001c 	0x1c
  2c:	00610002 	0x610002
  30:	00040000 	sll	zero,a0,0x0
  34:	00000000 	nop
  38:	80002000 	lb	zero,8192(zero)
  3c:	0000020c 	syscall	0x8
	...
  48:	0000001c 	0x1c
  4c:	00c60002 	0xc60002
  50:	00040000 	sll	zero,a0,0x0
  54:	00000000 	nop
  58:	8000220c 	lb	zero,8716(zero)
  5c:	00000038 	0x38
	...
  68:	0000001c 	0x1c
  6c:	012c0002 	0x12c0002
  70:	00040000 	sll	zero,a0,0x0
  74:	00000000 	nop
  78:	80002244 	lb	zero,8772(zero)
  7c:	000002dc 	0x2dc
	...
  88:	0000001c 	0x1c
  8c:	01920002 	0x1920002
  90:	00040000 	sll	zero,a0,0x0
  94:	00000000 	nop
  98:	80003000 	lb	zero,12288(zero)
  9c:	00000258 	0x258
	...
  a8:	0000001c 	0x1c
  ac:	01f70002 	0x1f70002
  b0:	00040000 	sll	zero,a0,0x0
  b4:	00000000 	nop
  b8:	80002520 	lb	zero,9504(zero)
  bc:	000001f8 	0x1f8
	...
  c8:	0000001c 	0x1c
  cc:	025c0002 	0x25c0002
  d0:	00040000 	sll	zero,a0,0x0
  d4:	00000000 	nop
  d8:	80002718 	lb	zero,10008(zero)
  dc:	000000e4 	0xe4
	...

Disassembly of section .debug_ranges:

00000000 <.debug_ranges>:
   0:	ffffffff 	sdc3	$31,-1(ra)
   4:	00000000 	nop
   8:	80000000 	lb	zero,0(zero)
   c:	80000390 	lb	zero,912(zero)
  10:	80001180 	lb	zero,4480(zero)
  14:	80001190 	lb	zero,4496(zero)
	...
