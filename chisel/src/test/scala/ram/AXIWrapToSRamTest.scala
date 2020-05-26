package ram
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, PeekPokeTester, Driver}
import chisel3.util._
import memory.axi.AXIWrapToSRAM
import memory.physicalMem.DualPortedCombinMemory
import shared.Constants

class AXIWrapToSRamTest extends ChiselFlatSpec {
  behavior.of("axi sram wrap")
  it should "success" in {
//    Driver.execute(
//      Array("--generate-vcd-output", "on"),
//      () => new AXIWrapToSRAM(Constants.DATA_ID)
//    ) { dut =>
//      new AXIWrapToSRamUnitTest(dut)
//    } should be(true)
    Driver.execute(
      Array("--generate-vcd-output", "on"),
      //      Array(),
      () => new AXIWrapToSRAM(Constants.DATA_ID)
    ) { dut =>
      new AXIWrapToSRamUnitTest(dut)
    } should be(true)
  }
}

class AXIWrapToSRamUnitTest(dut: AXIWrapToSRAM) extends PeekPokeTester(dut) {
  reset(3)
  poke(dut.io.bus.aw.valid, true)
  poke(dut.io.bus.aw.bits.addr, 0)
  step(3)
  for (i <- 0 until 16) {
    poke(dut.io.bus.w.bits.data, i)
    poke(dut.io.bus.w.valid, true)
    expect(dut.io.bus.w.ready, true)
    expect(dut.io.ram.write.enable, true)
    expect(dut.io.ram.write.data, i)
    expect(dut.io.ram.write.addr, i*4)
    step(1)
  }

}
