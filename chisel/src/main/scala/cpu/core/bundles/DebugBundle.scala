// See README.md for license details.

package cpu.core.bundles

import chisel3._
import cpu.core.Constants._

class DebugBundle extends Bundle {
  val wbPC = UInt(addrLen.W)
  val wbRegFileWEn = UInt(4.W)
  val wbRegFileWNum = UInt(regAddrLen.W)
  val wbRegFileWData = UInt(dataLen.W)
}
