# amadeus-mips • [![action state](https://github.com/amadeus-mips/amadeus-mips/workflows/func-test/badge.svg)](https://github.com/amadeus-mips/amadeus-mips/actions)

Amadeus MIPS implements a large subset of MIPS32 Revision 1 in chisel,
with a few modifications of MIPS32 Revision 2. It is capable of booting
u-boot and booting linux to a [kernel
debugger](https://www.kernel.org/doc/html/latest/dev-tools/gdb-kernel-debugging.html).
If you would like to build it, please go to [1.2](#*elaborate).

## import into Intellij IDEA

**import chisel/ sub directory instead of importing this repo directly**

## elaborate

run the following code in the *chisel* directory to elaborate the
circuit ( into verilog )

``` bash
sbt 'runMain cpu.elaborateCPU'
```

the verilog can be found at *chisel/generation*. To build it, you need
to use the *divider.v* in that directory

## simulation

the simulation top file is at
*chisel/src/main/scala/soc/SocLiteTop.scala*. You could use it to run
function test and performance test

## tests

See 'soc.\*Test'

# Formal Verification and Symbiotic EDA Suite

We have met many difficulties during the design and validation of this
CPU. In particular, deeply pipelined caches are very hard to get right,
even without the cache invalidation support we added\! Unlike bugs in
the pipeline, which are relatively easy to trace; a bug in the cache
could take millions of cycles to trigger, let alone debug. During this
process, a trial version of [commercial symbiotic eda
suite](http://www.symbioticeda.com) helped us tremendously. It has
radically reduced the development and validation time of caches and
plays very nicely with chisel3. By using sva with symbiotic eda suite,
we formally verified a 2-stage pipelined instruction cache, 3-stage data
cache with support for early restart flushing and cache invalidation. We
also managed to verify a number of other elements in the pipeline that
are also fairly hard to debug.

# Cache organization

## advanced ideas applied in this cache

  - Deeply pipelined
  - multi-banked
  - early restart
  - with support for MIPS cache instructions ( cache flush )
  - optinal cache prefetching ( efficiency is low, still WIP )

## why the deep pipeline

Both caches has a very deep pipeline design. This is to improve the
frequency of our design. This did not workout, however, as our frequency
is severely limited by TLB. This is a bug we are still trying to find
out why.

# AXI4 Interface

Amadeus has 3 AXI ports, 2 ports for both caches, and one port for
uncached access. In order to improve efficiency, the uncached unit
supports multiple outstanding AXI transactions. As we are only allowed
to expose one AXI port, we also built AXI arbiter that supports multiple
outstanding transactions.

# building u-boot and linux for amadeus mips

We have had success with several tool-chains, including the one that
comes with buildroot 2020.7 and [codescape mips mti bare metal
toolchain](https://codescape.mips.com/components/toolchain/2020.06-01/downloads.html).

## Building U-boot for amadeus

Please clone [amadeus
uboot](https://github.com/amadeus-mips/amadeus-uboot) and type make,
then convert the binary file to coe file and put it inside the rom.

## Building Linux for amadeus

Please clone [amadeus
linux](https://github.com/amadeus-mips/amadeus-linux) and type make,
**and use a binary editor to delete from address 0x0400 to 0x1400**, use
u-boot to load it over tftp, and type **go \[load address\]** instead of
converting it into an uImage and using *bootm* in uboot. Help is very
much appreciated as to why this happens. This is very puzzling in deed.

# Roadmap

  - improve TLB frequency
  - Squash away the final few bugs and boot linux

# Credits

  - [non-trivial-mips](https://github.com/trivialmips/nontrivial-mips)
    for providing us with a good reference as to which parts of mips is
    required to implement in order to boot linux. They also provided a
    good example on how to port linux to a mips architecture ( along
    with digilent nexys )
  - [dinocpu](https://github.com/jlpteaching/dinocpu), which taught me
    how to write a CPU in chisel. There is still some of their code left
    in our tests to debug a CPU *the gdb way*

# License

Please see
[license](https://github.com/amadeus-mips/amadeus-mips/blob/master/LICENSE)
