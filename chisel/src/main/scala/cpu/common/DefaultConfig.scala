// See README.md for license details.

package cpu.common

import chisel3._
import scala.math.log

trait DefaultWireLength {
  val cpuStallLen = 6
  val addrLen = 32
  val dataLen = 32

  val regAmount = 32
  val regAddrLen = (log(regAmount) / log(2)).toInt   // 5

  val exceptionTypeAmount = 32
}

object DefaultConfig extends DefaultWireLength {

}
