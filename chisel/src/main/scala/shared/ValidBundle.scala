// See README.md for license details.

package shared

import chisel3._
import cpu.core.Constants._

class ValidBundle(len: Int = dataLen) extends Bundle {
  val valid = Bool()
  val bits = UInt(len.W)

  override def cloneType: ValidBundle.this.type = new ValidBundle(len).asInstanceOf[this.type]
}
