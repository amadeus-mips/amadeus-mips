package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver}

//TODO: implicit parameter passing
class FuncTest extends ChiselFlatSpec {
  behavior.of("func_test")
  val funcFile = "./src/test/resources/loongson/func/inst_ram.coe"

  it should "use verilator without vcd file with performance metrics enabled" in {
    Driver.execute(
      Array("--backend-name", "verilator", "--generate-vcd-output", "off"),
      () => new SocLiteTop(simulation = true, memFile = funcFile, performanceMonitorEnable = true)
    ) { c =>
      new SocLiteTopUnitTester(c, trace = true, performanceMonitorEnable = true)
    } should be(true)
  }

  it should "use verilator to generate vcd file" in {
    Driver.execute(
      Array("--backend-name", "verilator"),
      () => new SocLiteTop(simulation = true, memFile = funcFile)
    ) { c =>
      new SocLiteTopUnitTester(c, trace = true)
    } should be(true)
  }
}
