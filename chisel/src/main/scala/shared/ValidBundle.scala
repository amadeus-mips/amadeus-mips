// See README.md for license details.

package shared

import chisel3._
import cpu.core.Constants._

class ValidBundle[T <: Data](gen: T = UInt(32.W)) extends Bundle {
  val valid = Bool()
  val bits  = gen

  override def cloneType: ValidBundle.this.type = new ValidBundle[T](gen).asInstanceOf[this.type]
}

object ValidBundle {
  def apply[T <: Data](valid: Bool, gen: T, len: Int = dataLen): ValidBundle[T] = {
    val that = Wire(new ValidBundle(gen))
    that.valid := valid
    that.bits  := gen
    that
  }
  def apply[T <: Data](gen: T = UInt(32.W)): ValidBundle[T] = new ValidBundle[T](gen)
}
