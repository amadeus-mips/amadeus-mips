// See README.md for license details.

package cpu.core

import chisel3._
import chisel3.util.Fill
import cpu.CPUConfig
import cpu.core.Constants._
import shared.DebugBundle

class Core_ls(implicit conf: CPUConfig) extends Core {
  val io_ls = IO(new Bundle {
    val debug = Output(new DebugBundle)
  })

  io_ls.debug.wbPC := mem2_wb.io.out.pc
  io_ls.debug.wbRegFileWEn := Fill(4, mem2_wb.io.out.write.enable)
  io_ls.debug.wbRegFileWNum := mem2_wb.io.out.write.address
  io_ls.debug.wbRegFileWData := mem2_wb.io.out.write.data
}