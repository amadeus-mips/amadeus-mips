package cpu.cache

import chisel3._
import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, PeekPokeTester}

import scala.util.Random

class SDPMBankUnitTest extends ChiselFlatSpec {
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
    val data2 = BigInt(maskW * maskN, r)
    write(0, (BigInt(1) << maskN) - 1, data)
    poke(c.io.rAddr, 0.U)
    expect(c.io.outData, data)

    step(1)
    write(1, (BigInt(1) << maskN) - 1, data2)
    poke(c.io.rAddr, 0.U)
    expect(c.io.outData, data)
    step(1)
    poke(c.io.mask, 0.U)
    poke(c.io.rAddr, 1.U)
    expect(c.io.outData, data2)

    def write(addr: BigInt, mask: BigInt, data: BigInt): Unit = {
      poke(c.io.we, true)
      poke(c.io.mask, mask)
      poke(c.io.wAddr, addr)
      poke(c.io.inData, data)
    }
  }

}
