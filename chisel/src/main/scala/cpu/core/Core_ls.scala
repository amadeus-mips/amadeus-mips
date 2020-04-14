// See README.md for license details.

package cpu.core

import chisel3._
import chisel3.util.Fill
import common.DebugBundle
import cpu.core.Constants._

class Core_ls extends Core {
  val io_ls = IO(new Bundle {
    val ex_addr = Output(UInt(addrLen.W))
    val debug = Output(new DebugBundle)
  })

  io_ls.ex_addr := executeTop.io.out.memAddr

  io_ls.debug.wbPC := mem_wb.io.out.pc
  io_ls.debug.wbRegFileWEn := Fill(4, mem_wb.io.out.write.enable)
  io_ls.debug.wbRegFileWNum := mem_wb.io.out.write.address
  io_ls.debug.wbRegFileWData := mem_wb.io.out.write.data
}