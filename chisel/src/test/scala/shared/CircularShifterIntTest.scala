package shared

import chisel3._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class CircularShifterIntTest extends ChiselFlatSpec {
  behavior.of("circular shifter")
  it should "success" in {
    //    val vecLeng = 13
    for (vecLeng <- 2 until 17) {
      Driver.execute(
        Array("--generate-vcd-output", "on"),
        //      Array(),
        () => new CircularShifterInt(vecLeng)
      ) { dut =>
        new CircularShifterIntUnitTester(dut, vecLeng)
      } should be(true)
    }
  }
}

class CircularShifterIntUnitTester(dut: CircularShifterInt, vecLeng: Int) extends PeekPokeTester(dut) {
  reset(3)
  for (temp <- 0 until vecLeng) {
    poke(dut.io.initPosition.valid, true)
    poke(dut.io.shiftEnable, false)
    poke(dut.io.initPosition.bits, temp)
    for (i <- 0 until vecLeng) {
      step(1)
      poke(dut.io.shiftEnable, true)
      poke(dut.io.initPosition.valid, false)
      if (i + temp < vecLeng) {
        expect(dut.io.vector, i + temp)
      } else {
        expect(dut.io.vector, i + temp - vecLeng)
      }
    }
  }
}
