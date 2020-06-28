package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.pipelinedCache.memoryBanks.LUTRam
import firrtl.options.TargetDirAnnotation

class TagValidBundle extends Bundle {
  val tag = UInt(20.W)
  val valid = Bool()
}

class ReadOnlyPort(numOfSets: Int) extends Bundle {
  val addr = Input(UInt(log2Ceil(numOfSets).W))
  val data = Output(new TagValidBundle)
}

class ReadWritePort(numOfSets: Int) extends Bundle {
  val addr = Input(UInt(log2Ceil(numOfSets).W))
  val writeEnable = Input(Bool())
  val writeData = Input(new TagValidBundle)
  val readData = Output(new TagValidBundle)
}

/**
  * all the tag and valid data are in here, stored in LUTRam
  *
  * @param numOfSets how many sets are in the cache
  * @param numOfWays how many ways are in the cache
  */
@chiselName
class TagValidBanks(numOfSets: Int, numOfWays: Int = 4) extends Module {
  require(isPow2(numOfSets))
  val io = IO(new Bundle {
    // has multiple banks to write to, select before hand
    val way = Vec(
      numOfWays,
      new Bundle {
        val portA = new ReadOnlyPort(numOfSets)
        val portB = new ReadWritePort(numOfSets)
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
    bank.io.writeData := io.way(i).portB.writeData.asTypeOf(UInt(21.W))
    io.way(i).portB.readData := bank.io.writeOutput.asTypeOf(new TagValidBundle)
  }
}

object TagValidBanksElaborate extends App {
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new TagValidBanks(32, 4)), TargetDirAnnotation("generation"))
  )
}
