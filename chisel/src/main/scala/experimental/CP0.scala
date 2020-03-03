// See README.md for license details.

package experimental

import _root_.experimental.Marcos._
import chisel3._
import chisel3.util._
import common.Util
import cpu.common.CP0Struct
import cpu.core.Constants._
import cpu.core.bundles.CPBundle
import cpu.core.components.CP0IO

class CP0 extends Module {
  val io = IO(new CP0IO)

  val regsInit = VecInit(Seq.fill(regAmount*8)(0.U(32.W)))
//  regsInit.updated(Status.index, Cat(0.U(9.W), 1.U(1.W), 0.U(22.W)))

  /** create 32*8 regs, 32 is the address, 8 is the sel */
  val regs = RegInit(regsInit)

  val tick = RegInit(false.B)
  tick := !tick
  /** increase 1 every two cycle */
  regs.get(con_Count) := regs.get(con_Count) + tick
  /** interrupt will modify the cause[15:10] */
  regs.get(con_Cause) := Util.subwordModify(regs.get(con_Cause), (15, 10), io.intr)

  def compareCP0(c: CPBundle, p: CP0Struct): Bool = {
    c.address === p.addr.U && c.sel === p.sel.U
  }
  when(io.cp0Write.enable){
    when(compareCP0(io.cp0Write, con_Count)){
      regs.get(con_Count) := io.cp0Write.data
    }.elsewhen(compareCP0(io.cp0Write, con_Status)){
      regs.get(con_Status) := io.cp0Write.data
    }
  }

  io.data := regs(Cat(io.addr, io.sel))

  io.status_o := regs.get(con_Status)
  io.cause_o := regs.get(con_Cause)
  io.EPC_o := regs.get(con_EPC)

  when(reset.asBool()){regs.get(con_Status) := Cat(0.U(9.W), 1.U(1.W), 0.U(22.W))}
}

object Marcos {
  implicit class VecEX(val regs: Seq[UInt]) {
    def get(cp0: CP0Struct): UInt = regs(cp0.index)
//    def get(cp0: CPControlBundle): UInt = regs(cp0.address)(cp0.sel)
  }
}
