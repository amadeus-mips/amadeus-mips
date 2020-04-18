package common

import chisel3._
import chisel3.util._

//TODO: rewrite this into a vec
class CircularShifter(vecLength: Int) extends Module {
  val io = IO(new Bundle {
    val initPosition = Input(ValidIO(UInt(log2Ceil(vecLength).W)))
    val shiftEnable = Input(Bool())
    val vector = Output(UInt(vecLength.W))
  })

  val internalVec = RegInit(0.U(vecLength.W))

  assert(!(io.initPosition.valid && io.shiftEnable), "cannot shift and init the vec in the same cycle")
  when(io.initPosition.valid) {
    assert(
      io.initPosition.bits < vecLength.U,
      "the init position of the vector is larger than the length of the internal vector"
    )
    internalVec := 1.U << io.initPosition.bits
  }

  when(io.shiftEnable) {
    internalVec := Cat(internalVec(vecLength - 2, 0), internalVec(vecLength - 1))
  }

  io.vector := internalVec
}

object CircularShifter {
  def apply(vecLength: Int): CircularShifter = {
    Module(CircularShifter(vecLength))
  }
}
