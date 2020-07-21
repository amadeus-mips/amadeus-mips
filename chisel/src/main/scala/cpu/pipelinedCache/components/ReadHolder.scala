package cpu.pipelinedCache.components

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig

class ReadHolder(implicit CPUConfig: CPUConfig, cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {

    /** when input is valid, there is a valid/not ready scenario */
    val input = Flipped(Valid(Vec(CPUConfig.fetchAmount, UInt((cacheConfig.bankWidth * 8).W))))

    /** when output is valid, read from here */
    val output = Valid(Vec(CPUConfig.fetchAmount, UInt((cacheConfig.bankWidth * 8).W)))
  })

  val instruction = RegInit(0.U.asTypeOf(Vec(CPUConfig.fetchAmount, UInt((cacheConfig.bankWidth * 8).W))))
  val valid       = RegInit(false.B)

  instruction     := io.input.bits
  io.output.bits  := instruction
  valid           := io.input.valid
  io.output.valid := valid
}
