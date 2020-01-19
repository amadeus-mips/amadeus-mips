// See README.md for license details.

package cpu.common

import chisel3._

trait DefaultWireLength {
  val cpuStallLen = 6
  val addressLen = 32
  val dataLen = 32
}
