package cpu.core.memory

import chisel3.Driver

object ControlMain extends App {
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  val outDir: String = "./out/" + toString.replaceAll("\\$@", "")
  Driver.execute(Array("-td", outDir),  () => new Control)
}
