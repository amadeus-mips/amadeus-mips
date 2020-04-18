package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver}

class AutoTest extends ChiselFlatSpec {
  behavior.of("Soc")
  val instFile = "./src/test/resources/loongson/func/inst_ram.coe"
  "pc" should "run to 0xbfc00100" in {
    Driver.execute(
      Array("--backend-name", "verilator", "--generate-vcd-output", "off"),
      () => new SocLiteTop(simulation = true, memFile = instFile)
    ) { c =>
      new SocLiteTopUnitTester(c, trace = true, needAssert = true, banLog = true)
    } should be(true)
  }
}
