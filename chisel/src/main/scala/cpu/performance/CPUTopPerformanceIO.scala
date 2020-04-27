package cpu.performance

import chisel3._
import chisel3.util._

class CPUTopPerformanceIO extends Bundle {
  val cache = new CachePerformanceMonitorIO
}
