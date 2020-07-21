// See README.md for license details.

package shared

import chisel3._
import chisel3.experimental.{DataMirror, requireIsChiselType}
import cpu.core.Constants._


class ValidBundle[T <: Data](gen: T)(implicit compileOptions: CompileOptions) extends Bundle {
  val genType = if (compileOptions.declaredTypeMustBeUnbound) {
    requireIsChiselType(gen)
    gen
  } else {
    if (DataMirror.internal.isSynthesizable(gen)) {
      chiselTypeOf(gen)
    } else {
      gen
    }
  }

  val valid = Bool()
  val bits  = genType

  override def cloneType: ValidBundle.this.type = new ValidBundle[T](gen).asInstanceOf[this.type]
}

object ValidBundle {
  def apply[T <: Data](valid: Bool, bits: T, len: Int = dataLen): ValidBundle[T] = {
    val that = Wire(ValidBundle(bits))
    that.valid := valid
    that.bits  := bits
    that
  }
  def apply[T <: Data](gen: T): ValidBundle[T] = new ValidBundle[T](gen)

  def apply(len: Int): ValidBundle[UInt] = new ValidBundle[UInt](UInt(len.W))
}
