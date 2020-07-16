package cpu.pipelinedCache.components.metaBanks

import chisel3._
import chisel3.util._
import cpu.pipelinedCache.CacheConfig

class ReadOnlyPort[+T <: Data](gen: T)(implicit cacheConfig: CacheConfig) extends Bundle {
  val addr = Input(UInt(log2Ceil(cacheConfig.numOfSets).W))
  val data = Output(gen)

  override def cloneType = (new ReadOnlyPort(gen)).asInstanceOf[this.type]
}

class ReadWritePort[+T <: Data](gen: T)(implicit cacheConfig: CacheConfig) extends Bundle {
  val addr        = Input(UInt(log2Ceil(cacheConfig.numOfSets).W))
  val writeEnable = Input(Bool())
  val writeData   = Input(gen)
  val readData    = Output(gen)

  override def cloneType = (new ReadWritePort(gen)).asInstanceOf[this.type]
}

