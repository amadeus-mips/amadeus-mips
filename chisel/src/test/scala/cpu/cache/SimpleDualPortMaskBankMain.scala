package cpu.cache

import chisel3.Driver

object SimpleDualPortMaskBankMain extends App {
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  val outDir: String = "./out/" + toString.replaceAll("\\$@", "")
  Driver.execute(Array("-td", outDir),  () => new SimpleDualPortMaskBank(depth = 128, maskN = 4, maskW = 8))
}
