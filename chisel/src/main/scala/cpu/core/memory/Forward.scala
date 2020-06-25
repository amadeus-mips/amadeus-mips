// See README.md for license details.

package cpu.core.memory

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.common.{CauseBundle, EntryHiBundle, EntryLoBundle, IndexBundle, PageMaskBundle, StatusBundle}
import cpu.core.Constants._
import cpu.core.bundles.CPBundle
import cpu.core.components.{ExceptionHandleBundle, TLBHandleBundle}

class Forward(implicit cfg: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val inExceptionCP0  = Input(new ExceptionHandleBundle)
    val inTLBCP0        = Input(new TLBHandleBundle(cfg.tlbSize))
    val wbCP0           = Input(new CPBundle)
    val outExceptionCP0 = Output(new ExceptionHandleBundle)
    val outTLBCP0       = Output(new TLBHandleBundle(cfg.tlbSize))
  })

  io.outExceptionCP0 := io.inExceptionCP0
  io.outTLBCP0       := io.inTLBCP0
  when(io.wbCP0.enable) {
    switch(io.wbCP0.addr) {
      is(con_Status.addr.U) {
        io.outExceptionCP0.status := io.wbCP0.data.asTypeOf(new StatusBundle)
      }
      is(con_Cause.addr.U) {
        io.outExceptionCP0.cause := io.wbCP0.data.asTypeOf(new CauseBundle)
      }
      is(con_EPC.addr.U) {
        io.outExceptionCP0.EPC := io.wbCP0.data
      }
      is(con_Index.addr.U) {
        io.outTLBCP0.index := io.wbCP0.data.asTypeOf(new IndexBundle(cfg.tlbSize))
      }
      is(con_EntryHi.addr.U) {
        io.outTLBCP0.entryHi := io.wbCP0.data.asTypeOf(new EntryHiBundle)
      }
      is(con_PageMask.addr.U) {
        io.outTLBCP0.pageMask := io.wbCP0.data.asTypeOf(new PageMaskBundle)
      }
      is(con_EntryLo0.addr.U) {
        io.outTLBCP0.entryLo0 := io.wbCP0.data.asTypeOf(new EntryLoBundle)
      }
      is(con_EntryLo1.addr.U) {
        io.outTLBCP0.entryLo1 := io.wbCP0.data.asTypeOf(new EntryLoBundle)
      }
    }
  }

}
