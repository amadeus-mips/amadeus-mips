// See README.md for license details.

package cpu.common

import chisel3._
import scala.math.log

trait DefaultWireLength {
  val cpuStallLen = 6
  val addressLen = 32
  val dataLen = 32

  val aluOpLen = 5
  val memOpLen = 3
  val aluSelLen = 3
  val registerNumber = 32
  val registerAddressLen = (log(registerNumber) / log(2)).toInt   // 5

  val exceptionTypeNumber = 32
}

object DefaultConfig extends DefaultWireLength {

}
