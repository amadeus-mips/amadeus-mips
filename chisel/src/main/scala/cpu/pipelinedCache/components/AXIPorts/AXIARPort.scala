package cpu.pipelinedCache.components.AXIPorts

import axi.AXIIO
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import firrtl.options.TargetDirAnnotation

class AXIARPort(addrReqWidth: Int = 32, AXIID: UInt)(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** address req should be connected to [[cpu.pipelinedCache.components.MSHR]]
      * a ar transaction is initiated with addrReq.handshake
      * valid signal means if there is a new hit
      * ready signal means if the axi ar port is ready for another request
      * bits denotes address */
    val addrReq = Flipped(Decoupled(UInt(addrReqWidth.W)))

    /** axi ar channel */
    val ar = new DecoupledIO(AXIIO.arChannel())

    val arCommit = Output(Bool())
  })

  val arIdle :: arWait :: Nil = Enum(2)
  val arState                 = RegInit(arIdle)

  // axi signals
  io.ar.bits.id    := AXIID
  io.ar.bits.addr  := io.addrReq.bits
  io.ar.bits.len   := (cacheConfig.numOfBanks - 1).U(4.W)
  io.ar.bits.size  := "b010".U(3.W) // always 4 bytes
  io.ar.bits.burst := "b10".U(2.W) // axi wrap burst

  io.ar.bits.lock  := 0.U
  io.ar.bits.cache := 0.U
  io.ar.bits.prot  := 0.U

  io.addrReq.ready := arState === arIdle
  io.arCommit      := arState === arWait && io.ar.fire
  io.ar.valid      := arState === arWait || io.addrReq.fire

  switch(arState) {
    is(arIdle) {
      when(io.addrReq.fire) {
        arState := arWait
      }
    }
    is(arWait) {
      when(io.ar.fire) {
        arState := arIdle
      }
    }
  }
}

object AXIARPortElaborate extends App {
  implicit val cacheConfig: CacheConfig = new CacheConfig
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new AXIARPort(addrReqWidth= 32,AXIID = 0.U(3.W))), TargetDirAnnotation("verification"))
  )
}
