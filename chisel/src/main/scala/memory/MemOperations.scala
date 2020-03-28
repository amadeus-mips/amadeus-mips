package memory

import chisel3.experimental.ChiselEnum

object MemOperations extends ChiselEnum {
  //TODO: what is readwrite
  val Read, Write, ReadWrite = Value
}
