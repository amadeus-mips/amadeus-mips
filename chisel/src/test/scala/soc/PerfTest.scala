package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver}

/**
  * Default to take off the generation of vcd file
  */
class AllTest extends ChiselFlatSpec {
  val perfFile = "./src/test/resources/loongson/perf/axi_ram.coe"

  implicit val tcfg = new TestConfig(banLog = true, runAllPerf = true, vcdOn = false)
  val option =
    if (tcfg.vcdOn) Array("--backend-name", "verilator")
    else Array("--backend-name", "verilator", "--generate-vcd-output", "off")

  "All perf test" should "pass" in {
    Driver.execute(
      option,
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c)
    } should be(true)
  }
}

class PerfTest extends ChiselFlatSpec {
  val perfFile = "./src/test/resources/loongson/perf/axi_ram.coe"

  implicit val tcfg = new TestConfig(banLog = true, vcdOn = false)
  val option =
    if (tcfg.vcdOn) Array("--backend-name", "verilator")
    else Array("--backend-name", "verilator", "--generate-vcd-output", "off")

  "bit count" should "pass" in {
    Driver.execute(
      option,
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, perfNumber = 1)
    } should be(true)
  }

  "bubble sort" should "pass" in {
    Driver.execute(
      option,
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, perfNumber = 2)
    } should be(true)
  }

  "coremark" should "pass" in {
    Driver.execute(
      option,
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, perfNumber = 3)
    } should be(true)
  }

  "crc32" should "pass" in {
    Driver.execute(
      option,
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, perfNumber = 4)
    } should be(true)
  }

  "dhrystone" should "pass" in {
    Driver.execute(
      option,
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, perfNumber = 5)
    } should be(true)
  }

  "quick sort" should "pass" in {
    Driver.execute(
      option,
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, perfNumber = 6)
    } should be(true)
  }

  "select sort" should "pass" in {
    Driver.execute(
      option,
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, perfNumber = 7)
    } should be(true)
  }

  "sha" should "pass" in {
    Driver.execute(
      option,
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, perfNumber = 8)
    } should be(true)
  }

  "stream copy" should "pass" in {
    Driver.execute(
      option,
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, perfNumber = 9)
    } should be(true)
  }

  "string search" should "pass" in {
    Driver.execute(
      option,
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, perfNumber = 10)
    } should be(true)
  }

}
