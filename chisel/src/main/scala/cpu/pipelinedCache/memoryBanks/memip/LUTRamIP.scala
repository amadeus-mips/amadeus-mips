package cpu.pipelinedCache.memoryBanks.memip

import chisel3._
import chisel3.util.log2Ceil

/**
  * XPM 2019.2 XPM_MEMORY_DPDISTRAM, at page 124 of UG953(2019.2)
  * by default, this is initialized to all 0
  *
  * @param dataWidth      : the size of the data to store in each line, in bits
  * @param addrWidth      : the width of address
  * @param byteWriteWidth : addressable size of write
  * @param numberOfLines  : how many **bits** there are in the memory
  */
class LUTRamIP(dataWidth: Int, addrWidth: Int, byteWriteWidth: Int, numberOfLines: Int)
  extends BlackBox(
    Map(
      "MEMORY_SIZE" -> numberOfLines * dataWidth,
      "WRITE_DATA_WIDTH_A" -> dataWidth,
      "WRITE_DATA_WIDTH_B" -> dataWidth,
      "READ_DATA_WIDTH_A" -> dataWidth,
      "READ_DATA_WIDTH_B" -> dataWidth,
      "BYTE_WRITE_WIDTH_A" -> byteWriteWidth,
      "READ_LATENCY_A" -> 1,
      "READ_LATENCY_B" -> 1,
      "READ_RESET_VALUE_A" -> 0,
      "READ_RESET_VALUE_B" -> 0,
      "CLOCKING_MODE" -> "common_clock"
    )
  ) {
  require(addrWidth == log2Ceil(numberOfLines), "address width should be log 2 of number of lines to address all")
  require(
    dataWidth - (dataWidth / byteWriteWidth) * byteWriteWidth == 0,
    "data width should be a multiple of byte write width"
  )
  require(addrWidth <= 20, "address width should be 1 to 20")
  val io = IO(new Bundle {
    val clka = Input(Clock())
    val clkb = Input(Clock())
    val rsta = Input(Reset())
    val rstb = Input(Reset())

    val ena = Input(Bool())
    val enb = Input(Bool())
    val regcea = Input(Bool())
    val regceb = Input(Bool())

    val dina = Input(UInt(dataWidth.W))
    val addra = Input(UInt(addrWidth.W))
    val addrb = Input(UInt(addrWidth.W))

    val wea = Input(UInt((dataWidth / byteWriteWidth).W))

    val douta = Output(UInt(dataWidth.W))
    val doutb = Output(UInt(dataWidth.W))
  })
}
