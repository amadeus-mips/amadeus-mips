package cpu.cache.dataCache

import chisel3.iotesters.{ChiselFlatSpec, Driver}
import cpu.cache.PerfectMemory
import org.scalatest.Matchers

class BasicDCacheReadTest(dut: VeriDCache, goldenMem: PerfectMemory) extends PipelinedDcacheBaseTester(dut, goldenMem) {
  init()
  for (i <- 0 until 512) {
    requestQueue.enqueue(new request(i << 2, false, List(0, 0, 0, 0), List(false, false, false, false)))
  }
  while (next()) {}
}



class BasicTestManager extends ChiselFlatSpec with Matchers {
  behavior.of("dcache simple read")
  val reference = new PerfectMemory(8192)
  reference.dumpToDisk()
  it should "success" in {
    Driver.execute(
      Array("--generate-vcd-output", "on", "--backend-name", "verilator"),
      () => new VeriDCache
    ) { dut =>
      new BasicDCacheReadTest(dut, reference)
    } should be(true)
  }

}
