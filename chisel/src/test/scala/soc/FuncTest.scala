package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver}

class FuncNoVcdTest extends ChiselFlatSpec {
  /** soc config */
  implicit val socCfg = SocConfig.funcConfig(simulation = false)
  /** test config */
  implicit val tcfg = new TestConfig(trace = true, performanceMonitorEnable =false)

  "func test" should "use verilator without vcd file with performance metrics enabled" in {
    Driver.execute(
      Array("--backend-name", "verilator", "--generate-vcd-output", "off"),
      () => new SocLiteTop
    ) { c =>
      new SocLiteTopUnitTester(c)
    } should be(true)
  }
}

/** If you want to test the data dependence or hazard, it is suggested to turn on the simulation option.
  * The simulation option will disable the random delay of ram and so the instructions will be execute more crowded. */
class FuncWithVcdTest extends ChiselFlatSpec {
  /** soc config */
  implicit val socCfg = SocConfig.funcConfig(simulation = true)
  /** test config */
  implicit val tcfg = new TestConfig(trace = true, vcdOn = true)

  "func test" should "use verilator to generate vcd file" in {
    Driver.execute(
      Array("--backend-name", "verilator"),
      () => new SocLiteTop
    ) { c =>
      new SocLiteTopUnitTester(c)
    } should be(true)
  }
}

/**
  * There is no golden trace!!!
  */
class OurFuncWithVcdTest extends ChiselFlatSpec {
  implicit val socCfg = SocConfig.funcConfig(simulation = true, memFile = "./src/test/resources/loongson/func/inst_ram_p.coe")
  implicit val tcfg = new TestConfig(trace = false, vcdOn = true)

  "func test" should "use verilator to generate vcd file" in {
    Driver.execute(
      Array("--backend-name", "verilator"),
      () => new SocLiteTop
    ) { c =>
      new SocLiteTopUnitTester(c)
    } should be(true)
  }
}
