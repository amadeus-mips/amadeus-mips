package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.memoryBanks.LUTRam

class TagValidBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val tag = UInt(cacheConfig.tagLen.W)
  val valid = Bool()

  override def cloneType: this.type = (new TagValidBundle).asInstanceOf[this.type]
}

class ReadOnlyPort[+T <: Data](gen: T)(implicit cacheConfig: CacheConfig) extends Bundle {
  val addr = Input(UInt(log2Ceil(cacheConfig.numOfSets).W))
  val data = Output(gen)

  override def cloneType = (new ReadOnlyPort(gen)).asInstanceOf[this.type]
}

class ReadWritePort[+T <: Data](gen: T)(implicit cacheConfig: CacheConfig) extends Bundle {
  val addr = Input(UInt(log2Ceil(cacheConfig.numOfSets).W))
  val writeEnable = Input(Bool())
  val writeData = Input(gen)
  val readData = Output(gen)

  override def cloneType = (new ReadWritePort(gen)).asInstanceOf[this.type]
}

/**
  * all the tag and valid data are in here, stored in LUTRam
  *
  */
@chiselName
class TagValidBanks(implicit CacheC: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val numOfSets: Int = CacheC.numOfSets
  val numOfWays: Int = CacheC.numOfWays
  require(isPow2(numOfSets))
  val io = IO(new Bundle {
    // has multiple banks to write to, select before hand
    val way = Vec(
      numOfWays,
      new Bundle {
        val portA = new ReadOnlyPort(new TagValidBundle)
        val portB = new ReadWritePort(new TagValidBundle)
      }
    )
  })
  val tagBanks = for (i <- 0 until numOfWays) yield {
    val bank = Module(new LUTRam(depth = numOfSets, width = (new TagValidBundle).getWidth))
    bank.suggestName(s"tag_valid_bank_way_$i")
    bank.io.readAddr := io.way(i).portA.addr
    io.way(i).portA.data := bank.io.readData.asTypeOf(new TagValidBundle)

    bank.io.writeAddr := io.way(i).portB.addr
    bank.io.writeEnable := io.way(i).portB.writeEnable
    bank.io.writeData := io.way(i).portB.writeData.asTypeOf(bank.io.writeData)
    io.way(i).portB.readData := bank.io.writeOutput.asTypeOf(new TagValidBundle)
  }
}
