// See README.md for license details.

package cpu.core.components

import chisel3._
import cpu.core.Constants.cpuStallLen

class StageIO[+T <: Data](gen: T) extends Bundle {
  val stall = Input(Bool())
  val flush = Input(Bool())
  val in    = Input(gen)
  val out   = Output(gen)

  override def cloneType: this.type = new StageIO(gen).asInstanceOf[this.type]
}

/**
  * Create a stage in pipeline.
  *
  * If want to change logic, you need to extend this class.
  * If want to modify IOBundle, you need to extend the StageIO and override the Stage.io.
  *
  * @param gen         the instance of the bundle
  * @tparam T the input and output bundle type.
  */
class Stage[+T <: Data](gen: T) extends MultiIOModule {
  val io = IO(new StageIO[T](gen))

  val pipeReg = RegInit(0.U.asTypeOf(gen))

  when(io.flush) {
    pipeReg := 0.U.asTypeOf(gen)
  }.elsewhen(!io.stall) {
      pipeReg := io.in
    }
    .otherwise {
      // stalled, do nothing
    }
  io.out := pipeReg

}
