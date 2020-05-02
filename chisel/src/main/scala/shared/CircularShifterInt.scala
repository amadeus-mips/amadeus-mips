package shared

import chisel3._
import chisel3.util._

//TODO: rewrite this into a vec
class CircularShifterInt(vecLength: Int) extends Module {
  val io = IO(new Bundle {
    val initPosition = Input(ValidIO(UInt(log2Ceil(vecLength).W)))
    val shiftEnable = Input(Bool())
    val vector = Output(UInt(log2Ceil(vecLength).W))
  })

  val internalVec = RegInit(0.U(log2Ceil(vecLength).W))

  assert(!(io.initPosition.valid && io.shiftEnable), "cannot shift and init the vec in the same cycle")
  when(io.initPosition.valid) {
    assert(
      io.initPosition.bits < vecLength.U,
      "the init position of the vector is larger than the length of the internal vector"
    )
    internalVec := io.initPosition.bits
  }

  when(io.shiftEnable) {
    internalVec := internalVec + 1.U
  }

  io.vector := internalVec
}

object CircularShifterInt {
  def apply(vecLength: Int): CircularShifterInt = {
    Module(CircularShifterInt(vecLength))
  }
}
