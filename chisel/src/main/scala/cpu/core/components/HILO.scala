// See README.md for license details.

package cpu.core.components

import chisel3._
import cpu.core.bundles.{HILOBundle, HILOValidBundle}

class HILO extends Module {
  val io = IO(new Bundle {
    val in = Input(new HILOValidBundle)
    val out = Output(new HILOBundle)
  })

  val hi = RegInit(0.U(32.W))
  val lo = RegInit(0.U(32.W))

  when(io.in.hi.valid){ hi := io.in.hi.bits }
  when(io.in.lo.valid){ lo := io.in.lo.bits}

  io.out.hi := hi
  io.out.lo := lo
}
