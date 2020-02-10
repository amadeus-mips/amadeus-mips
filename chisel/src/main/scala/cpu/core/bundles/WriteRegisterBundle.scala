// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

class WriteRegisterBundle(hasData: Boolean = true) extends Bundle {
  val writeEnable = Bool() // write enable
  val writeTarget = UInt(registerAddressLen.W)
  val writeData = if(hasData) UInt(dataLen.W) else UInt(0.W)

  override def cloneType: WriteRegisterBundle.this.type = new WriteRegisterBundle(hasData).asInstanceOf[this.type]
}
