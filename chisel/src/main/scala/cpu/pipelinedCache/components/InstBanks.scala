package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.metaBanks.ReadWritePort
import cpu.pipelinedCache.memoryBanks.SinglePortBRam

/**
  * this bank is for instruction cache, so at most 1 write port is required.
  * The IP is repurposed to fulfill this request ( 1 write port always disabled )
  */
@chiselName
class InstBanks(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val numOfWays: Int = cacheConfig.numOfWays
  val numOfSets: Int = cacheConfig.numOfSets
  val numOfBanks: Int = cacheConfig.numOfBanks
  val io = IO(new Bundle {
    val way_bank = Vec(
      numOfWays,
      Vec(
        numOfBanks,
        new ReadWritePort(UInt(32.W))
      )
    )
  })
  for {
    i <- 0 until numOfWays
    j <- 0 until numOfBanks
  } yield {
    val bank = Module(new SinglePortBRam(numOfSets, 32, false))
    bank.suggestName(s"instruction_bank_way_${i}_bankoffset_${j}")
    // Single Port BRam
    bank.io.en := true.B
    bank.io.addr := io.way_bank(i)(j).addr
    bank.io.inData := io.way_bank(i)(j).writeData
    bank.io.writeVector := io.way_bank(i)(j).writeEnable
    io.way_bank(i)(j).readData := bank.io.outData
  }
}
