// See README.md for license details.
package shared

import chisel3._

class GPIO extends Bundle {
  val led = Output(UInt(16.W))
  val led_rg0 = Output(UInt(2.W))
  val led_rg1 = Output(UInt(2.W))
  val num_csn = Output(UInt(8.W))
  val num_a_g = Output(UInt(7.W))
  val switch = Input(UInt(8.W))
  val btn_key_col = Output(UInt(4.W))
  val btn_key_row = Input(UInt(4.W))
  val btn_step = Input(UInt(2.W))
}
