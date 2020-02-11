package cpu.memory

import chisel3._
import chisel3.experimental.ChiselEnum

object MemOperations extends ChiselEnum {
  val Read, Write, ReadWrite = Value
}
