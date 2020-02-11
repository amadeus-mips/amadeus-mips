package cpu.components
import chisel3._
trait OpConstants {

  // Y means yes, N means no, X means don't care

  // true/false mux
  val Y = true.B
  val N = false.B
  val X = false.B

  // whether write to rt or rd
  // to RegDst
  val RegDst_RT = 0.U
  val RegDst_RD = 1.U

  // ALU ops
  val ALU_NOP = 0.U
  val ALU_ADD = 1.U
  val ALU_SUB = 2.U
  val ALU_AND = 3.U
  val ALU_PASSTHROUGH = 4.U
  val ALU_X = 4.U
}
