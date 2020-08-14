// See README.md for license details.

package cpu.core

import chisel3._
import chisel3.util.Fill
import cpu.CPUConfig
import cpu.core.Constants._
import shared.DebugBundle

class Core_ls(implicit conf: CPUConfig) extends Core {
  val io_ls = IO(new Bundle {
    val debug = Output(Vec(conf.decodeWidth, new DebugBundle))
  })

  io_ls.debug.zip(mem2_wb.io.out).foreach{
    case (debug, writeBack) =>
      debug.wbPC := writeBack.pc
      debug.wbRegFileWEn := Fill(4, writeBack.write.enable)
      debug.wbRegFileWNum := writeBack.write.address
      debug.wbRegFileWData := writeBack.write.data
  }

}