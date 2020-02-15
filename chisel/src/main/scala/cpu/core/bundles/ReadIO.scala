// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

/**
 * 带IO的Bundle，slave端需要翻转信号
 */
class ReadIO extends Bundle {
  val enable = Output(Bool())
  val address = Output(UInt(regAddrLen.W))
  val data = Input(UInt(dataLen.W))
}
