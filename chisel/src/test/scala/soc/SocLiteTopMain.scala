package soc

import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import firrtl.options.TargetDirAnnotation

object SocLiteTopMain {
  def main(args: Array[String]): Unit = {
    implicit val socCfg = SocConfig.funcConfig(simulation = false)
    // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
    val outDir: String = "./out/" + toString.replaceAll("\\$@.*", "")
    (new ChiselStage).execute(
      Array(),
      Seq(ChiselGeneratorAnnotation(() => new SocLiteTop()),
        TargetDirAnnotation(outDir))
    )
    println(s"Generated to path: $outDir")
  }
}
