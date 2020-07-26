package cpu.pipelinedCache.components.AXIPorts

import axi.AXIIO
import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

class AXIRPort(addrReqWidth : Int= 32,AXIID: UInt)(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** axi r channel */
    val r = Flipped(DecoupledIO(AXIIO.rChannel(cacheConfig.bankWidth * 8)))

    /** transfer data to [[cpu.pipelinedCache.components.ReFillBuffer]]
      * or [[cpu.pipelinedCache.components.MaskedRefillBuffer]]
      * ready means the refill buffer is ready to receive the data
      * i.e. not in a write back or evict
      * valid means the data transferred this cycle is valid
      * */
    val transferData = Decoupled(UInt((cacheConfig.bankWidth * 8).W))

    /** denotes the end of a transfer */
    val finishTransfer = Output(Bool())
  })

  io.finishTransfer     := io.r.bits.last && io.r.fire
  io.transferData.bits  := io.r.bits.data
  io.transferData.valid := io.r.fire
  io.r.ready            := io.transferData.ready
}
