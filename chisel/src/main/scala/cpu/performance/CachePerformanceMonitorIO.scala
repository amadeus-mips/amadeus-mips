package cpu.performance

import chisel3._

class CachePerformanceMonitorIO extends Bundle {
  val hitCycles = Output(UInt(32.W))
  val missCycles = Output(UInt(32.W))
}
