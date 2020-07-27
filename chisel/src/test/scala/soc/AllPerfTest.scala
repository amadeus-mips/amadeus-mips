package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver}
class AllPerfTest extends ChiselFlatSpec {
  behavior of ("soc run perf test")
  // soc config
  implicit val socCfg = SocConfig.perfConfig(simulation = false)
  // test config
  implicit val tcfg = new TestConfig(banLog = true, runAllPerf = true, vcdOn = false)

  "log" should "be dumped" in {
    Driver.execute(
      Array("--backend-name", "verilator", "--generate-vcd-output", "off"),
      () => new SocLiteTop()
    ) { c =>
      new SocLiteTopUnitTester(c)
    } should be(true)
  }
}
