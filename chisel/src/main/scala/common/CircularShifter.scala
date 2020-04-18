package common

import chisel3._
import chisel3.util._

//TODO: rewrite this into a vec
//TODO: make the valid of the output optional
class CircularShifter(vecLength: Int) extends Module {
  val io = IO(new Bundle {
    val initPosition = Input(ValidIO(UInt(log2Ceil(vecLength).W)))
    //    def initShift(pos: UInt): Unit = {
    //      this.initPosition.valid := true.B
    //      this.initPosition.bits := pos
    //    }
    val vector = Output(ValidIO(UInt(vecLength.W)))
  })

  val internalVec = RegInit(0.U(vecLength.W))

  val checkPos = Reg(UInt(log2Ceil(vecLength).W))

  val c = Counter(32)
  c.inc()
  val sIdle :: sStep :: Nil = Enum(2)

  val state = RegInit(sIdle)

  switch(state) {
    is(sIdle) {
      when(io.initPosition.valid) {
        assert(
          io.initPosition.bits < vecLength.U,
          "the init position of the vector is larger than the length of the internal vector"
        )
        assert(state === sIdle, "the state of the circular shifter must be idle during a shift operation")
        state := sStep
        checkPos := Mux(io.initPosition.bits.orR, io.initPosition.bits - 1.U, (vecLength - 1).U)
        internalVec := 1.U << io.initPosition.bits
        state := sStep
      }
    }
    is(sStep) {
      internalVec := Cat(internalVec(vecLength - 2, 0), internalVec(vecLength - 1))
      when(internalVec(checkPos)) {
        state := sIdle
      }
    }
  }
  io.vector.bits := internalVec
  io.vector.valid := state === sStep
}

object CircularShifter {
  def apply(vecLength: Int): CircularShifter = {
    Module(CircularShifter(vecLength))
  }
}
