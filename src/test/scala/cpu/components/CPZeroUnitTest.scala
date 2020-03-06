package cpu.components
import chisel3.iotesters.PeekPokeTester

class CPZeroUnitTest(cpZero: CPZero) extends PeekPokeTester(cpZero) {
  poke(cpZero.io.input.writeEnable, false)
  for (i <- 0 until 40) {
    poke(cpZero.io.input.readEnable, true)
    poke(cpZero.io.input.dst, 9)
    expect(cpZero.io.output.regVal, i)
    step(2)
  }
}
