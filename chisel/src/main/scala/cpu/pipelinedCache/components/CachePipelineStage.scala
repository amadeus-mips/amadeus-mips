package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName

class CachePipelineStageIO[+T <: Data](gen: T) extends Bundle {
  val stall = Input(Bool())
  val in = Input(gen)
  val out = Output(gen)

  // flush is not required because there isn't `no-op` in cache pipeline
  override def cloneType: this.type = new CachePipelineStageIO(gen).asInstanceOf[this.type]
}

/**
  * stage register for the cache pipelining
  *
  * @param gen the data bundle for input/output
  * @tparam T type of the data bundle
  */
@chiselName
class CachePipelineStage[+T <: Data](gen: T) extends Module {
  val io = IO(new CachePipelineStageIO[T](gen))
  val pipelineReg = RegInit(0.U.asTypeOf(gen))

  when(!io.stall) {
    pipelineReg := io.in
  }

  io.out := pipelineReg
}
