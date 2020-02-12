package cpu.components{


  import chisel3.iotesters.PeekPokeTester

  class MemoryUnitTester(c : InstrMem) extends PeekPokeTester(c)  {
    def read(addr: Int, data: Int) : Boolean = {
      poke(c.io.isWrite, false)
      poke(c.io.addr, addr)
      step(1)
      expect(c.io.readData, data)
    }

    def wr(addr: Int, data:Int) = {
      poke(c.io.isWrite, true)
      poke(c.io.addr, addr)
      poke(c.io.writeData, data)
      step(1)
    }

    val list = Array.fill(1024) {0}
    // initialize all the memory to 0
    for (k <- 0 until 1024) {
      wr(k,0)
    }
   // see if initialization is correct
    for (k <- 0 until 1024) {
      read(k,0)
    }

    // put random values into them
    for ( k <- 0 until 1024) {
      val wrAddr = rnd.nextInt(1023)
      val wrData = rnd.nextInt(1<<32 - 2) + 1
      wr(wrAddr,wrData)
      read(wrAddr,wrData)
    }

    // for (i <- 1 until 100) {
    //   poke(c.io.isWrite, true)
    //   poke(c.io.addr, i << 5)
    //   poke(c.io.writeData, i)
    //   step(1)

    //   poke(c.io.isWrite, false)
    //   poke(c.io.addr, i<< 5)
    //   expect(c.io.readData, i)
    // }

}
}
