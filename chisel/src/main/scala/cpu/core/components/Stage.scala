// See README.md for license details.

package cpu.core.components

import chisel3._
import cpu.core.Constants.cpuStallLen

class StageIO[+T <: Data] (gen: T) extends Bundle {
  val stall = Input(UInt(cpuStallLen.W))
  val flush = Input(Bool())
  val in = Input(gen)
  val out = Output(gen)
}

object StageIO {
  def apply[T](gen: T): StageIO[T] = new StageIO(gen)
}

/**
 * Create a stage in pipeline.
 *
 * If want to change logic, you need to extend this class.
 * If want to modify IOBundle, you need to extend the StageIO and override the Stage.io.
 * For more details,
 * @see [[cpu.core.pipeline.IDEXE]]
 * @param stageNumber the serial number of this stage.
 * @param gen the instance of the bundle
 * @tparam T the input and output bundle type.
 */
class Stage[+T <: Data](stageNumber: Int, gen: T) extends Module {
  val io = IO(StageIO(gen))

  val pipeReg = RegInit(0.U.asTypeOf(gen))

  val stallEnd = io.stall(stageNumber) && !io.stall(stageNumber+1)
  val notStalled = !io.stall(stageNumber)

  def execute(): Unit = {
    when(io.flush || stallEnd) {
      pipeReg := 0.U.asTypeOf(gen)
    }.elsewhen(notStalled) {
      pipeReg <> io.in
    }.otherwise {
      // stalled, do nothing
    }
    io.out <> pipeReg
  }

  execute()
}
