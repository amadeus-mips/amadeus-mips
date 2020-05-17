package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver}

class FuncNoVcdTest extends ChiselFlatSpec {
  /** soc config */
  implicit val socCfg = SocConfig.funcConfig(simulation = false, performanceMonitor = true)
  /** test config */
  implicit val tcfg = new TestConfig(trace = true, performanceMonitorEnable = true)

  "func test" should "use verilator without vcd file with performance metrics enabled" in {
    Driver.execute(
      Array("--backend-name", "verilator", "--generate-vcd-output", "off"),
      () => new SocLiteTop
    ) { c =>
      new SocLiteTopUnitTester(c)
    } should be(true)
  }
}

class FuncWithVcdTest extends ChiselFlatSpec {
  /** soc config */
  implicit val socCfg = SocConfig.funcConfig(simulation = false)
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
