package axi

import chisel3._

class AXIArbiter(sCount: Int = 3) extends Module{
  val io = IO(new Bundle() {
    val slaves = Vec(sCount, AXIIO.slave())
    val master = AXIIO.master()
  })


}
