package cpu.pipelinedCache.dataCache

import chisel3._
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.metaBanks.MaskedReadWritePort
import cpu.pipelinedCache.memoryBanks.SinglePortBRam
import cpu.pipelinedCache.veri.VerificationBRam

class DataBanks(implicit cacheConfig: CacheConfig, CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val way_bank = Vec(cacheConfig.numOfWays, Vec(cacheConfig.numOfBanks, new MaskedReadWritePort(UInt(32.W))))
  })
  for {
    i <- 0 until cacheConfig.numOfWays
    k <- 0 until cacheConfig.numOfBanks
  } yield {
    if(CPUConfig.verification) {
      val bank = Module(new VerificationBRam(cacheConfig.numOfSets, 32, way = i, bankIndex = k))
      bank.suggestName(s"data_cache_bank_way_${i}_bankOffset_${k}")
      bank.io.en                 := true.B
      bank.io.addr               := io.way_bank(i)(k).addr
      bank.io.writeVector        := io.way_bank(i)(k).writeMask
      bank.io.inData             := io.way_bank(i)(k).writeData
      io.way_bank(i)(k).readData := bank.io.outData
    } else {
      val bank = Module(new SinglePortBRam(cacheConfig.numOfSets, 32, true))
      bank.suggestName(s"data_cache_bank_way_${i}_bankOffset_${k}")
      bank.io.en                 := true.B
      bank.io.addr               := io.way_bank(i)(k).addr
      bank.io.writeVector        := io.way_bank(i)(k).writeMask
      bank.io.inData             := io.way_bank(i)(k).writeData
      io.way_bank(i)(k).readData := bank.io.outData
    }

  }
}
