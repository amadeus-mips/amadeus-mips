package cpu.core

import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import cpu.CPUConfig
import firrtl.options.TargetDirAnnotation

object CoreMain extends App {
  implicit val conf = new CPUConfig(build = true)
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  val outDir: String = "./out/" + toString.replaceAll("\\$@.*", "")
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new Core),
      TargetDirAnnotation(outDir))
  )
  println(s"Generated to path: $outDir")
}

object Core_lsMain extends App {
  // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
  implicit val conf = new CPUConfig(build = true)
  val outDir: String = "./out/" + toString.replaceAll("\\$@.*", "")
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new Core),
      TargetDirAnnotation(outDir))
  )
  println(s"Generated to path: $outDir")
}
