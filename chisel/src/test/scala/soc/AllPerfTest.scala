package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver}
class AllPerfTest extends ChiselFlatSpec {
  behavior of ("soc run perf test")
  val perfFile = "./src/test/resources/loongson/perf/axi_ram.coe"

  "log" should "be dumped" in {
    Driver.execute(
      Array("--backend-name", "verilator", "--generate-vcd-output", "off"),
      () => new SocLiteTop(simulation = true, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, banLog = true, runAllPerf = true)
    } should be(true)
  }
}
