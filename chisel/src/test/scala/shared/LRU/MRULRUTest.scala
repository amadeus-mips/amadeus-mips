package shared.LRU

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class MRULRUTest extends ChiselFlatSpec {
  behavior.of("tree plru")
  it should "success" in {
    Driver.execute(
      Array("--backend-name","verilator","--generate-vcd-output", "on"),
      () => new PseudoLRUMRU(4, 4)
    ) { dut =>
      new MRUPLRUShifterUnitTester(dut, 4, 4)
    } should be(true)
  }
}

class MRUPLRUShifterUnitTester(dut: PseudoLRUMRU, numOfWay: Int, numOfSets: Int) extends PeekPokeTester(dut) {
  reset(3)
  poke(dut.io.accessEnable, true)
  poke(dut.io.accessSet, 2)

  def pokeWay(way: Int): Unit = {
    poke(dut.io.accessWay, way)
    step(1)
  }

  pokeWay(0)
  expect(dut.io.lruLine, 1)
  pokeWay(1)
  expect(dut.io.lruLine, 2)
  pokeWay(2)
  expect(dut.io.lruLine, 3)
  pokeWay(3)
  expect(dut.io.lruLine, 0)
  pokeWay(2)
  expect(dut.io.lruLine, 0)
  pokeWay(0)
  expect(dut.io.lruLine, 1)
  pokeWay(3)
  expect(dut.io.lruLine, 1)
  pokeWay(1)
  expect(dut.io.lruLine, 0)
  pokeWay(0)
  expect(dut.io.lruLine, 2)
}

