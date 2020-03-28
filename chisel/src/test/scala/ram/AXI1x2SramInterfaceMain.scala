package ram

import chisel3.Driver

object AXI1x2SramInterfaceMain {
  def main(args: Array[String]): Unit = {
    // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
    val outDir: String = "./out/" + toString.replaceAll("\\$@.*", "")
    Driver.execute(Array("-td", outDir),  () => new AXI1x2SramInterface)
    println(s"Generated to path: $outDir")
  }
}
