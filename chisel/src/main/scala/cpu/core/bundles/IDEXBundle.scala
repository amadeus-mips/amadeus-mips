// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

class IDEXBundle extends Bundle {
  val aluOp = UInt(aluOpLen.W)
  val memOp = UInt(memOpLen.W)
  val aluSigned = Bool()
  val reg1 = UInt(dataLen.W)
  val reg2 = UInt(dataLen.W)
  val writeRegister = new WriteRegisterBundle(hasData = false)
  val inst = UInt(dataLen.W)
}
