package axi

import chisel3.Driver

object AXIInterconnectTest extends App{
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  val outDir: String = "./out/" + toString.replaceAll("\\$@.*", "")
  Driver.execute(Array("-td", outDir),  () => new AXIInterconnect(AXIInterconnectConfig.loongson_func))
  println(s"Generated to path: $outDir")
}
