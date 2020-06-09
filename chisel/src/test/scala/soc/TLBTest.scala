package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver}

class TLBTest extends ChiselFlatSpec {
  /** soc config */
  implicit val socCfg = SocConfig.tlbConfig(simulation = false)
  /** test config */
  implicit val tcfg = new TestConfig(vcdOn = true, tlbTest = true)

  "tlb test" should "use verilator without vcd file" in {
    Driver.execute(
      Array("--backend-name", "verilator"),
      () => new SocLiteTop()
    ) {
      c => new SocLiteTopUnitTester(c)
    } should be(true)
  }
}
