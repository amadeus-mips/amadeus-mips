// See README.md for license details.
package axi

import chisel3._
import chisel3.util.log2Ceil

class EncoderBundle(WIDTH: Int) extends Bundle {
  val bits = UInt(WIDTH.W)
  val valid = Bool()
  val encoded = UInt(log2Ceil(WIDTH).W)

  override def cloneType: EncoderBundle.this.type = new EncoderBundle(WIDTH).asInstanceOf[this.type]
}
