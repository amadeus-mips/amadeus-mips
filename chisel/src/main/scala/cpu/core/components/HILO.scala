// See README.md for license details.

package cpu.core.components

import chisel3._
import cpu.CPUConfig
import cpu.core.bundles.{HILOBundle, HILOValidBundle}

class HILO(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(conf.decodeWidth, new HILOValidBundle))
    val out = Output(new HILOBundle)
  })

  val hi = RegInit(0.U(32.W))
  val lo = RegInit(0.U(32.W))

  when(io.in(0).hi.valid){ hi := io.in(0).hi.bits }
  when(io.in(1).hi.valid){ hi := io.in(1).hi.bits }
  when(io.in(0).lo.valid){ lo := io.in(0).lo.bits}
  when(io.in(1).lo.valid){ lo := io.in(1).lo.bits}

  io.out.hi := hi
  io.out.lo := lo
}
