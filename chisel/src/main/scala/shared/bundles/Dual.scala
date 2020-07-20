package shared.bundles

import chisel3._

class Dual[T <: Data](gen: T) extends Bundle {
  val first  = gen
  val second = gen

  override def cloneType: Dual.this.type = new Dual(gen).asInstanceOf[this.type]
}
object Dual {
  def apply[T <: Data](gen:   T): Dual[T] = new Dual(gen)
  def apply[T <: Data](first: T, second: T): Dual[T] = {
    val dual = Wire(new Dual(first))
    dual.first  := first
    dual.second := second
    dual
  }
}
