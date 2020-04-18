package common

import chisel3._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class CircularShifterTest extends ChiselFlatSpec {
  behavior.of("circular shifter")
  it should "success" in {
    //    val vecLeng = 13
    for (vecLeng <- 2 until 17) {
      Driver.execute(
        Array("--generate-vcd-output", "on"),
        //      Array(),
        () => new CircularShifter(vecLeng)
      ) { dut =>
        new CircularShifterUnitTester(dut, vecLeng)
      } should be(true)
    }
  }
}

class CircularShifterUnitTester(dut: CircularShifter, vecLeng: Int) extends PeekPokeTester(dut) {
  reset(3)
  for (temp <- 0 until vecLeng) {
    poke(dut.io.initPosition.valid, true)
    poke(dut.io.initPosition.bits, temp)
    expect(dut.io.vector.valid, false)
    for (i <- 0 until vecLeng) {
      step(1)
      poke(dut.io.initPosition.valid, false)
      expect(dut.io.vector.valid, true)
      if (i + temp < vecLeng) {
        expect(dut.io.vector.bits, 1 << (i + temp))
      } else {
        expect(dut.io.vector.bits, (1 << (i + temp - vecLeng)))
      }
    }
    step(1)
    expect(dut.io.vector.valid, false)
  }
}
