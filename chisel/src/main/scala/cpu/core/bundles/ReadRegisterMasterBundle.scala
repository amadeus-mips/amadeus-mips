// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

/**
 * 带IO的Bundle，slave端需要翻转信号
 */
class ReadRegisterMasterBundle extends Bundle {
  val readEnable = Output(Bool())
  val readTarget = Output(UInt(registerAddressLen.W))
  val readData = Input(UInt(dataLen.W))
}
