package cpu.pipelinedCache.veri

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import firrtl.options.TargetDirAnnotation

class GoldenDCache extends Module {
  val io = IO(new Bundle {
    val addr        = Input(UInt(32.W))
    val writeEnable = Input(Bool())
    val writeMask   = Input(UInt(4.W))
    val writeData   = Input(UInt(32.W))
    val data        = Output(UInt(32.W))
  })

  val mem = RegInit(VecInit(Seq.tabulate(64)(i => i.U(32.W))))
  dontTouch(mem)
  when(io.writeEnable) {
    mem(io.addr(31, 2)) := Cat(
      (3 to 0 by -1).map(i =>
        Mux(io.writeMask(i), io.writeData(8 * i + 7, 8 * i), mem(io.addr(31, 2))(8 * i + 7, 8 * i))
      )
    )
  }
  io.data := mem(io.addr(31, 2))
}

object GoldenDCacheElaborate extends App {
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new GoldenDCache), TargetDirAnnotation("verification"))
  )
}
