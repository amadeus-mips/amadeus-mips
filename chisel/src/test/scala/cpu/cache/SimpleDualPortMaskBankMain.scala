package cpu.cache

import chisel3._
import chisel3.iotesters._

import scala.util.Random

object SimpleDualPortMaskBankMain extends App {
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  val outDir: String = "./out/" + toString.replaceAll("\\$@", "")
  chisel3.Driver.execute(Array("-td", outDir),  () => new SimpleDualPortMaskBank(depth = 128, maskN = 4, maskW = 8))
}

class SimpleDualPortMaskBankUnitTest extends ChiselFlatSpec {
  behavior of "SimpleDualPortMaskBank"

  "test SimpleDualPortMaskBank" should "success" in {
    val depth = 128
    val maskW = 8
    val maskN = 4
    iotesters.Driver.execute(Array(), ()=> new SimpleDualPortMaskBank(depth, maskW, maskN)) {
      c => new SimpleDualPortMaskBankUnitTester(c, depth, maskW, maskN)
    } should be(true)
  }

  class SimpleDualPortMaskBankUnitTester(c: SimpleDualPortMaskBank, depth: Int, maskW: Int, maskN: Int) extends
    PeekPokeTester(c) {
    val seed = System.currentTimeMillis()
    println(s"seed: $seed")
    val r = new Random(seed)
    val data = BigInt(maskW * maskN, r)
    poke(c.io.we, true)
    poke(c.io.mask, BigInt(1) << maskN - 1)
    poke(c.io.wAddr, 0.U)
    poke(c.io.inData, data)
    step(1)
    poke(c.io.rAddr, 0.U)
    expect(c.io.outData, data, s"expect $data, get ${peek(c.io.outData)}")
    step(1)
  }

}
