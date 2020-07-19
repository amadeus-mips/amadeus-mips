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

  override def cloneType = new ReadOnlyPort(gen).asInstanceOf[this.type]
}

class ReadWritePort[+T <: Data](gen: T)(implicit cacheConfig: CacheConfig) extends Bundle {
  val addr = Input(UInt(log2Ceil(cacheConfig.numOfSets).W))
  val writeEnable = Input(Bool())
  val writeData = Input(gen)
  val readData = Output(gen)

  override def cloneType = new ReadWritePort(gen).asInstanceOf[this.type]
}

/**
  * all the tag and valid data are in here, stored in LUTRam
  *
  */
@chiselName
class TagValidBanks(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val numOfSets: Int = cacheConfig.numOfSets
  val numOfWays: Int = cacheConfig.numOfWays
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

  for (i <- 0 until numOfWays) {
    val tagBank = Module(
      new LUTRam(
        depth = numOfSets,
        width = cacheConfig.tagLen
      )
    )
    val validBank = RegInit(VecInit(Seq.fill(numOfSets)(false.B)))
    tagBank.suggestName(s"tag_bank_way_$i")
    validBank.suggestName(s"valid_bank_way_$i")

    tagBank.io.readAddr        := io.way(i).portA.addr
    io.way(i).portA.data.tag   := tagBank.io.readData
    io.way(i).portA.data.valid := validBank(io.way(i).portA.addr)

    // ignore the port b read
    io.way(i).portB.readData := DontCare
    tagBank.io.writeAddr     := io.way(i).portB.addr
    tagBank.io.writeEnable   := io.way(i).portB.writeEnable
    tagBank.io.writeData     := io.way(i).portB.writeData.tag
    when(io.way(i).portB.writeEnable) {
      validBank(io.way(i).portB.addr) := io.way(i).portB.writeData.valid
    }

  }
}

