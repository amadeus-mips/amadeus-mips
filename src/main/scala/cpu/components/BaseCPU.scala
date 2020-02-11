package cpu.components

import chisel3._
import chisel3.util._

abstract class BaseCPU extends Module {
  val io = IO(new CoreIO())
}
