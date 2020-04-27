package cpu.core.decode

import chisel3.Driver

/**
 * Only produce verilog file to ./out
 */
object DecodeMain extends App {
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  val outDir: String = "./out/" + toString.replaceAll("\\$@", "")
  println(outDir)
  Driver.execute(Array("-td", outDir),  () => new Decode)
}
