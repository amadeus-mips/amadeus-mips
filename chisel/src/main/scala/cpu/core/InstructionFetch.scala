package cpu.core

import chisel3._
import cpu.common._

/**
  * the instruction fetch circuit
  * @param conf: the phoenix config
  */
class InstructionFetch(implicit conf: PhoenixConfiguration) extends Module{
  val io = IO(new Bundle() {
    val instructionAddress = Input(UInt(conf.memWidth.W))
    val instruction = Output(UInt(conf.regLen.W))
  })
}
