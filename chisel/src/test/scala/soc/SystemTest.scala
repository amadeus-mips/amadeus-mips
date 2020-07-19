package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver}

class SystemTest extends ChiselFlatSpec {
  implicit val socCfg = SocUpTopConfig()
  implicit val tcfg   = SystemTestConfig()
  val vcdOff          = false
  val args =
    if (vcdOff)
      Array("--backend-name", "verilator", "--generate-vcd-output", "off")
    else
      Array("--backend-name", "verilator")
  "system test" should "behavior correct" in {
    Driver.execute(
      args,
      () => new SocUpTop()
    ) { c =>
      new SystemTester(c)
    } should be(true)
  }
}
