package cpu.pipelinedCache.memoryBanks.memip

import chisel3._
import chisel3.util.log2Ceil

/**
  * simple dual port ram
  *
  * @param dataWidth       : width of every data line
  * @param byteWriteWidth  : how many bits to write per mask
  * @param numberOfLines   : how many lines of data are in the ram
  * @param addrWidth       : how wide is the address (to cover all lines)
  * @param memoryPrimitive : should I use auto, block ram or distributed ram
  */
class SDPRamIP(
                dataWidth: Int = 32,
                byteWriteWidth: Int = 8,
                numberOfLines: Int,
                addrWidth: Int,
                memoryPrimitive: String = "auto"
              ) extends BlackBox(
  Map(
    "ADDR_WIDTH_A" -> addrWidth,
    "ADDR_WIDTH_B" -> addrWidth,
    "WRITE_DATA_WIDTH_A" -> dataWidth,
    "READ_DATA_WIDTH_B" -> dataWidth,
    "BYTE_WRITE_WIDTH_A" -> byteWriteWidth,
    "CLOCKING_MODE" -> "common_clock",
    "READ_LATENCY_B" -> 1,
    "MEMORY_SIZE" -> numberOfLines * dataWidth,
    "MEMORY_PRIMITIVE" -> memoryPrimitive,
    "WRITE_MODE_A" -> "write_first"
  )
) {
  override def desiredName: String = "XPM_MEMORY_SDPRAM"
  require(addrWidth <= 20, "address width should be 1 to 20")
  require(
    dataWidth - (dataWidth / byteWriteWidth) * byteWriteWidth == 0,
    "data width should be a multiple of byte write width"
  )
  require(
    List("auto", "block", "distributed", "ultra").contains(memoryPrimitive),
    "memory primitive should be auto, block ram, dist ram or ultra ram"
  )
  require(addrWidth == log2Ceil(numberOfLines), "address width should be log 2 of number of lines to address all")
  val io = IO(new Bundle {
    // clock and reset
    val clka = Input(Clock())
    val clkb = Input(Clock())
    val rsta = Input(Reset())
    val rstb = Input(Reset())

    val addra = Input(UInt(addrWidth.W))
    val dina = Input(UInt(dataWidth.W))
    val ena = Input(Bool())
    val wea = Input(UInt((dataWidth / byteWriteWidth).W))

    val addrb = Input(UInt(addrWidth.W))
    val enb = Input(Bool())
    val regceb = Input(Bool())
    val doutb = Output(UInt(dataWidth.W))
  })
}
