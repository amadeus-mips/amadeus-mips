package cpu.performance

import chisel3._
import chisel3.util._

class SocPerformanceIO extends Bundle {
  val cpu = new CPUTopPerformanceIO
}
