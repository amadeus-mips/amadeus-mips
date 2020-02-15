package cpu.core.pipeline

import chisel3.Driver

/**
 * Only produce verilog file to ./out
 */
object DecodeTopMain extends App {
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  val outDir: String = "./out/" + toString.replaceAll("\\$@", "")
  Driver.execute(Array("-td", outDir),  () => new DecodeTop)
}
