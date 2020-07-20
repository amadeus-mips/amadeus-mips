package shared.bundles

import chisel3._

class Dual[T <: Data](gen: T) extends Bundle {
  val first = gen
  val second = gen
}
object Dual {
  def apply[T <: Data](gen: T): Dual[T] = new Dual(gen)
}
