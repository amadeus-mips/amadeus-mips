// See README.md for license details.

package cpu.core

import chisel3._

/**
 * Only produce verilog file to ./out
 */
object InstFetchMain extends App {
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  val outDir: String = "./out/" + toString.replaceAll("\\$@", "")
  Driver.execute(Array("-td", outDir),  () => new InstFetch)
}
