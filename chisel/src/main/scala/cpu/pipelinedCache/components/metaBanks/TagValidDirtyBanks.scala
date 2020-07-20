package cpu.pipelinedCache.components.metaBanks

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.memoryBanks.LUTRam

class TagValidDirtyBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val tag   = UInt(cacheConfig.tagLen.W)
  val valid = Bool()
  val dirty = Bool()

  override def cloneType: this.type = (new TagValidDirtyBundle).asInstanceOf[this.type]
}

/**
  * I have decided to move the dirty banks to the 2nd stage, please don't use this
  * @param cacheConfig
  * @param CPUConfig
  */
@deprecated
@chiselName
class TagValidDirtyBanks(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val numOfSets: Int = cacheConfig.numOfSets
  val numOfWays: Int = cacheConfig.numOfWays
  require(isPow2(numOfSets))
  val io = IO(new Bundle {
    val way = Vec(
      numOfWays,
      new Bundle {
        val portA = new ReadOnlyPort(new TagValidDirtyBundle)
        val portB = new ReadWritePort(new TagValidDirtyBundle)
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
    val dirtyBank = RegInit(VecInit(Seq.fill(numOfSets)(false.B)))
    tagBank.suggestName(s"dcache_tag_bank_way_$i")
    validBank.suggestName(s"dcache_valid_bank_way_$i")
    dirtyBank.suggestName(s"dcache_dirty_bank_way_$i")

    val readAddr = WireDefault(io.way(i).portA.addr)
    tagBank.io.readAddr        := readAddr
    io.way(i).portA.data.tag   := tagBank.io.readData
    io.way(i).portA.data.valid := validBank(readAddr)
    io.way(i).portA.data.dirty := dirtyBank(readAddr)

    val readWriteAddress = WireDefault(io.way(i).portB.addr)
    val writeEnable = WireDefault(io.way(i).portB.writeEnable)

    tagBank.io.writeAddr := readWriteAddress
    tagBank.io.writeEnable := writeEnable
    tagBank.io.writeData := io.way(i).portB.writeData.tag
    io.way(i).portB.readData.tag := tagBank.io.writeOutput

    when (!writeEnable) {
      io.way(i).portB.readData.valid := validBank(readWriteAddress)
    } otherwise {
      io.way(i).portB.readData := DontCare
    }
  }
}
