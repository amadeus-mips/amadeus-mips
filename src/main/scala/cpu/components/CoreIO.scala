package cpu.components

import chisel3._
import cpu.memory.{DMemPortIO, IMemPortIO}

  class CoreIO extends Bundle{
    val imem = Flipped(new IMemPortIO)
    val dmem = Flipped(new DMemPortIO)
  }
