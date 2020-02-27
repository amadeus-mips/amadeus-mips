package cpu.components

import chisel3.util._

object exceptions {
  val noException :: break :: syscall :: eret :: fetch :: decode :: overflow :: memory :: Nil = Enum(8)
}
