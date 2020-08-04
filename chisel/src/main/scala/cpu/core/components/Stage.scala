// See README.md for license details.

package cpu.core.components

import chisel3._
import cpu.core.Constants.cpuStallLen

class StageIO[+T <: Data](gen: T) extends Bundle {
  val stall   = Input(UInt(cpuStallLen.W))
  val flush   = Input(Bool())
  val in      = Input(gen)
  val out     = Output(gen)
  val stalled = Output(Bool())

  override def cloneType: this.type = new StageIO(gen).asInstanceOf[this.type]
}

/**
  * Create a stage in pipeline.
  *
  * If want to change logic, you need to extend this class.
  * If want to modify IOBundle, you need to extend the StageIO and override the Stage.io.
  *
  * @param stageNumber the serial number of this stage.
  * @param gen         the instance of the bundle
  * @tparam T the input and output bundle type.
  */
class Stage[+T <: Data](stageNumber: Int, gen: T) extends MultiIOModule {
  val io = IO(new StageIO[T](gen))

  val pipeReg = RegInit(0.U.asTypeOf(gen))
  val stalled = RegInit(false.B)

  def stallEnd:   Bool = io.stall(stageNumber) && !io.stall(stageNumber + 1)
  def notStalled: Bool = !io.stall(stageNumber)

  when(io.flush || stallEnd) {
    pipeReg := 0.U.asTypeOf(gen)
    stalled := false.B
  }.elsewhen(notStalled) {
      pipeReg := io.in
      stalled := false.B
    }
    .otherwise {
      // stalled, do nothing
      stalled := true.B
    }
  io.out     := pipeReg
  io.stalled := stalled

}
