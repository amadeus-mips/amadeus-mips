package shared

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import shared.LRU.{LRUIO, TrueLRUNM}

class LRUWrapper(numOfSet: Int, numOfWay: Int) extends Module {
  val io = IO(new LRUIO(numOfSet, numOfWay))

  val internal = TrueLRUNM(numOfSet, numOfWay)

  when(io.accessEnable) {
    internal.update(io.accessSet, io.accessWay)
  }
  io.lruLine := internal.getLRU(io.accessSet)
}

class TrueLRUNMTest extends ChiselFlatSpec {
  behavior of ("true lru in an object")
  it should "success" in {
    Driver.execute(
      Array("--backend-name", "treadle", "--generate-vcd-output", "on"),
      () => new LRUWrapper(4, 2)
    ) { dut =>
      new TrueLRUNMUnitTester(dut)
    } should be(true)
  }
}

class TrueLRUNMUnitTester(dut: LRUWrapper) extends PeekPokeTester(dut) {
  reset(1)
  poke(dut.io.accessEnable, true)
  poke(dut.io.accessSet, 2)

  def pokeWay(way: Int): Unit = {
    require(way == 0 || way == 1, "this is a 2 way LRU")
    poke(dut.io.accessWay, way)
    step(1)
  }

  pokeWay(0)
  pokeWay(1)
  expect(dut.io.lruLine, 1)
  pokeWay(0)
  expect(dut.io.lruLine, 0)
  step(1)
  poke(dut.io.accessSet, 1)
  step(1)
  pokeWay(1)
  step(1)
  expect(dut.io.lruLine, 1)
  poke(dut.io.accessSet, 2)
  step(1)
  // ensure lru works across different sets
  expect(dut.io.lruLine, 1)
}
