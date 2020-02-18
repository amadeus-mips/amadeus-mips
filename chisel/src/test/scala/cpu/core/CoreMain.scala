package cpu.core

import chisel3.Driver

object CoreMain extends App {
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  val outDir: String = "./out/" + toString.replaceAll("\\$@.*", "")
  Driver.execute(Array("-td", outDir),  () => new Core)
}

object Core_lsMain extends App {
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  val outDir: String = "./out/" + toString.replaceAll("\\$@.*", "")
  Driver.execute(Array("-td", outDir),  () => new Core_ls)
  println(s"Generated to path: $outDir")
}
