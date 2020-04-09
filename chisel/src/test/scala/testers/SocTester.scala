package testers

import java.io.File

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, PeekPokeTester}
import cpu.TestTop

class SocTester extends ChiselFlatSpec {
  behavior of "Soc test"

  "running with --generate-vcd-output on" should "generate vcd files" in {
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on", "-td", "test_run_dir/soc/vcd", "--top-name", "soc"),
      () => new TestTop("./testMemFile/arith/add.txt")
    ) {
      c => new SocUnitTester(c)
    } should be(true)
  }
}

class SocUnitTester(c: TestTop) extends PeekPokeTester(c) {
  step(100)
}
