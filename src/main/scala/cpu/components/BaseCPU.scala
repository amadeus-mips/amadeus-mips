package cpu.components

import chisel3._

abstract class BaseCPU extends Module {
  val io = IO(new CoreIO())
}
