// See README.md for license details.

package shared

import chisel3._

class DebugBundle extends Bundle {
  val wbPC = UInt(32.W)
  val wbRegFileWEn = UInt(4.W)
  val wbRegFileWNum = UInt(5.W)
  val wbRegFileWData = UInt(32.W)
}
