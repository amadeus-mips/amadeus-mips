// See README.md for license details.

package shared

import chisel3._

class ValidBundle(len: Int = 32)(implicit compileOptions: CompileOptions) extends Bundle {

  val valid = Bool()
  val bits  = UInt(len.W)

  override def cloneType: ValidBundle.this.type = new ValidBundle(len).asInstanceOf[this.type]
}

object ValidBundle {
  def apply(valid: Bool, bits: UInt, len: Int = 32): ValidBundle = {
    val that = Wire(ValidBundle(len))
    that.valid := valid
    that.bits  := bits
    that
  }

  def apply(len: Int): ValidBundle = new ValidBundle(len)
}
