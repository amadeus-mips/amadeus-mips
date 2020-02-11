package cpu.singleCycle {

  import chisel3.iotesters._
  class DecodeUnitTester(c : Decode) extends PeekPokeTester(c) {
    // TODO: add a software decode to make sure everything is working
    // TODO: this poke function now only works with add
    /**
      * peek poke test with instruction instr
      * @param instr: the instruction to test with
      * @return whether it succeeded or not
      */
    def pokeWithIntruction(instr: Int): Boolean = {
      poke(c.io.instru, instr)
      step(1)
      expect(c.io.decodeOut.rsAddress, 1)
      expect(c.io.decodeOut.rtAddress, 2)
      expect(c.io.decodeOut.rdAddress,3)
    }

    // this should be a add instruction
    pokeWithIntruction(2234400)
  }

}
