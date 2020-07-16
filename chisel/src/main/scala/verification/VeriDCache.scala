package verification

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.cache.newDCache
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import firrtl.options.TargetDirAnnotation

class VeriDCache extends Module {
  val io = IO(new Bundle {
    val rChannel = Flipped(new NiseSramReadIO)
    val wChannel = Flipped(new NiseSramWriteIO)
  })

  val dcache = Module(new newDCache)
  val axiMem = Module(new VeriAXIRam)

  dcache.io.axi      <> axiMem.io.axi
  dcache.io.rChannel <> io.rChannel
  dcache.io.wChannel <> io.wChannel

  val mem           = Mem(BigInt("8000", 16), UInt(32.W))
  val readValueWire = WireDefault(mem(io.rChannel.addr))

  when(io.wChannel.enable) {
    mem(io.rChannel.addr) := Cat(
      (3 to 0 by -1).map(i =>
        Mux(io.wChannel.sel(i), io.wChannel.data(7 + 8 * i, 8 * i), readValueWire(7 + 8 * i, 8 * i))
      )
    )
  }

}

object VeriDCacheElaborate extends App {
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new VeriDCache), TargetDirAnnotation("generation"))
  )
}
