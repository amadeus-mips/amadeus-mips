// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.common.DefaultWireLength

class IFIDBundle extends Bundle with DefaultWireLength{
  val pc = UInt(addressLen.W)
  val inst = UInt(dataLen.W)
  val instFetchExcept = Bool()
}
