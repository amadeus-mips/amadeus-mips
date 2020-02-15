// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

class WriteBundle() extends Bundle {
  val control = new WriteControlBundle
  val data = UInt(dataLen.W)

//  override def cloneType: WriteRegfileBundle.this.type = new WriteRegfileBundle(hasData).asInstanceOf[this.type]
}

class WriteControlBundle() extends Bundle {
  val enable = Bool()
  val address = UInt(regAddrLen.W)
}
