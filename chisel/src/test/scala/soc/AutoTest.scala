package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver}

/** For Github Action CI */
class AutoTest extends ChiselFlatSpec {
  /** soc config */
  implicit val socCfg = SocConfig.funcConfig(simulation = true)
  /** test config */
  implicit val tcfg = new TestConfig(banLog = true, needAssert = true, trace = true)

  "pc" should "run to 0xbfc00100" in {
    Driver.execute(
      Array("--backend-name", "verilator", "--generate-vcd-output", "off"),
      () => new SocLiteTop
    ) { c =>
      new SocLiteTopUnitTester(c)
    } should be(true)
  }
}
