package confreg

import chisel3._

class NumMonitorBundle extends Bundle{
  val data = UInt(32.W)
  val monitor = Bool()
}
