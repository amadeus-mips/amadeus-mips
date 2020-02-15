package cpu.components
import chisel3._

class StageRegisterIO[+T <: Data](gen: T) extends Bundle {
  // the input are the values that get passed in
  // from the last stage
  val pipeIn = Input(gen)
  // flush signal will flush the stage register
  val flush = Input(Bool())
  // is the data valid
  val valid = Input(Bool())
  // the output are the values that get passed out
  // to the next stage
  val pipeOut = Output(gen)

  // this line is here because stage register is
  // essentially a generator for stage register
  override def cloneType: this.type = StageRegisterIO (gen).asInstanceOf[this.type]
}
// this is the stage register for pipelined CPU
object StageRegisterIO {
  def apply[T <: Data](gen: T): StageRegisterIO[T] = new StageRegisterIO(gen)
}

class StageRegister[+T <: Data](private val gen: T) extends Module {
  val io = IO(new StageRegisterIO[T](gen))
  // intentional Dontcare:
  // They will be connected
  io := DontCare

  // initialze the register to 0
  val register = RegInit(0.U.asTypeOf(gen))
  // pipe the register to the output regardless
  io.pipeIn := register

  // when the input io is valid, register becomes io.valIn
  // when not, register holds its value
  when (io.valid) {
    register := io.pipeOut
  }
  // when flush, reset the register value back to 0
  // note: flush precedes valid signal
  // also note: they'are both high on a single cycle is defined behaviour
  when (io.flush) {
    register := 0.U.asTypeOf(gen)
  }
}
