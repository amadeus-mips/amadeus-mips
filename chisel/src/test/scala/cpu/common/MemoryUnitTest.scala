package cpu.common{


  import chisel3.iotesters
  import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

  class MemoryUnitTester(c : InstrMem) extends PeekPokeTester(c)  {
    for (i <- 1 until 100) {
      poke(c.io.isWrite, true)
      poke(c.io.addr, i << 5)
      poke(c.io.writeData(i))
      step(1)

      poke(c.io.isWrite, false)
      poke(c.io.addr, i<< 5)
      expect(c.io.readData, i)
    }

}
}
