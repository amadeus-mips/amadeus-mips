package soc

import chisel3.Driver

object SocLiteTopMain {
  def main(args: Array[String]): Unit = {
    // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
    val outDir: String = "./out/" + toString.replaceAll("\\$@.*", "")
    Driver.execute(Array("-td", outDir),  () => new SocLiteTop(simulation = false, "./testMemFile/arith/add.txt"))
    println(s"Generated to path: $outDir")
  }
}
