// See README.md for license details.

package cpu.core

import chisel3._
import chisel3.util.Fill
import cpu.core.Constants._
import cpu.core.bundles.DebugBundle

class Core_ls extends Core {
  val io_ls = IO(new Bundle {
    val ex_addr = Output(UInt(addrLen.W))
    val cached_trans = Output(Bool())

    val flush = Output(Bool())

    val debug = Output(new DebugBundle)
  })

  io_ls.ex_addr := executeTop.io.out.memAddr
  io_ls.cached_trans := memoryTop.io.addr(31, 29) =/= "b101".U(3.W)

  io_ls.flush := ctrl.io.flush

  io_ls.debug.wbPC := mem_wb.io.out.pc
  io_ls.debug.wbRegFileWEn := Fill(4, mem_wb.io.out.write.control.enable)
  io_ls.debug.wbRegFileWNum := mem_wb.io.out.write.control.address
  io_ls.debug.wbRegFileWData := mem_wb.io.out.write.data
}