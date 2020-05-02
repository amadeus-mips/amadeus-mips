package cpu.performance

import chisel3._

class CachePerformanceMonitorIO extends Bundle {
  val hitCycles = Output(UInt(64.W))
  val missCycles = Output(UInt(64.W))
  val idleCycles = Output(UInt(64.W))
}
