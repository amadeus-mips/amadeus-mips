// See README.md for license details.

package shared

import chisel3._
import cpu.core.Constants._

class ValidBundle(len: Int = dataLen) extends Bundle {
  val valid = Bool()
  val bits  = UInt(len.W)

  override def cloneType: ValidBundle.this.type = new ValidBundle(len).asInstanceOf[this.type]
}

object ValidBundle {
  def apply(valid: Bool, bits: UInt, len: Int = dataLen): ValidBundle = {
    val that = Wire(new ValidBundle(len))
    that.valid := valid
    that.bits  := bits
    that
  }
}
