package cpu.core.pipeline

import chisel3._
import cpu.core.bundles.stages.Mem2WbBundle

@deprecated
class WbTop extends Module {
  val io = IO(new Bundle() {
    val in  = Input(new Mem2WbBundle)
    val out = Output(new Mem2WbBundle)
  })
  io.out := io.in
}
