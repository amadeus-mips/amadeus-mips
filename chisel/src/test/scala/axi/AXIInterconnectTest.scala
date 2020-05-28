package axi

import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import firrtl.options.TargetDirAnnotation

object AXIInterconnectTest extends App{
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  val outDir: String = "./out/" + toString.replaceAll("\\$@.*", "")
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new AXIInterconnect(AXIInterconnectConfig.loongson_func())),
      TargetDirAnnotation(outDir))
  )
  println(s"Generated to path: $outDir")
}
