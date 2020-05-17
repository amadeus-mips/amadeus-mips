package shared
import chisel3._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import shared.LRU.PseudoLRUTree

class TreePLRUTest extends ChiselFlatSpec {
  behavior.of("tree plru")
  it should "success" in {
    Driver.execute(
      Array(),
      () => new PseudoLRUTree(4, 4)
    ) { dut =>
      new TreePLRUShifterUnitTester(dut, 4, 4)
    } should be(true)
  }
}
class TreePLRUShifterUnitTester(dut: PseudoLRUTree, numOfWay: Int, numOfSets: Int) extends PeekPokeTester(dut) {
  reset(3)
  poke(dut.io.accessEnable, true)
  poke(dut.io.accessSet, 2)
  def pokeWay(way: Int): Unit = {
    poke(dut.io.accessWay, way)
    step(1)
  }
  pokeWay(0)
  pokeWay(1)
  pokeWay(2)
  pokeWay(3)
  expect(dut.io.lruLine, 0)
  pokeWay(0)
  pokeWay(2)
  pokeWay(3)
  pokeWay(1)
  expect(dut.io.lruLine, 2)
  pokeWay(3)
  pokeWay(1)
  pokeWay(0)
  pokeWay(2)
  expect(dut.io.lruLine, 1)

}
