package cpu.pipelinedCache.veri

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import firrtl.options.TargetDirAnnotation

class GoldenICache extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(32.W))
    val data = Output(UInt(32.W))
  })
  val mem = RegInit(VecInit(Seq.tabulate(64)(i => i.U(32.W))))
  dontTouch(mem)
  io.data := mem((io.addr(31, 2)))
}

object GoldenICacheElaborate extends App {
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new GoldenICache), TargetDirAnnotation("verification"))
  )
}
