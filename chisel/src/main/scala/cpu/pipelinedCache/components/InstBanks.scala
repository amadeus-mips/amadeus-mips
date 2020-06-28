package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import cpu.pipelinedCache.memoryBanks.TrueDualPortRam

/**
  * this bank is for instruction cache, so at most 1 write port is required.
  * The IP is repurposed to fulfill this request ( 1 write port always disabled )
  *
  * @param numOfSets  how many sets there are in the cache
  * @param numOfWays  how many ways there are in the cache
  * @param numOfBanks how many banks there are in the cache
  */
@chiselName
class InstBanks(numOfSets: Int, numOfWays: Int, numOfBanks: Int) extends Module {
  val io = IO(new Bundle {
    val way_bank = Vec(
      numOfWays,
      Vec(
        numOfBanks,
        new Bundle {
          val portA = new ReadOnlyPort(numOfSets)
          val portB = new ReadWritePort(numOfSets)
        }
      )
    )
  })
  for {
    i <- 0 until numOfWays
    j <- 0 until numOfBanks
  } yield {
    val bank = Module(new TrueDualPortRam(numOfSets, 32, false))
    bank.suggestName(s"instruction_bank_way_${i}_bankoffset_${j}")
    // port A is always read only
    bank.io.portA.portEnable := true.B
    bank.io.portA.addr := io.way_bank(i)(j).portA.addr
    bank.io.portA.writeData := 0.U.asTypeOf(bank.io.portA.writeData)
    bank.io.portA.writeVector := 0.U.asTypeOf(bank.io.portA.writeVector)
    io.way_bank(i)(j).portA.data := bank.io.portA.readData

    bank.io.portB.portEnable := true.B
    bank.io.portB.addr := io.way_bank(i)(j).portB.addr
    bank.io.portB.writeData := io.way_bank(i)(j).portB.writeData
    bank.io.portB.writeVector := io.way_bank(i)(j).portB.writeEnable
    io.way_bank(i)(j).portB.readData := bank.io.portB.readData

  }
}
