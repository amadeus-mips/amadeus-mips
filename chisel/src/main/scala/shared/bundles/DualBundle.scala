package shared.bundles

import chisel3._

class DualBundle[T <: Data](gen: T) extends Bundle {
  val first = gen
  val second = gen
}
object DualBundle {
  def apply[T <: Data](gen: T): DualBundle[T] = new DualBundle(gen)
}
