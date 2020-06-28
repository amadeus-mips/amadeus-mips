package cpu.pipelinedCache.memoryBanks

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.CPUConfig
import cpu.pipelinedCache.memoryBanks.memip.TDPRamIP

class TDPRamBundle(addrLen: Int, width: Int, byteAddressable: Boolean) extends Bundle {
  val portEnable = Input(Bool())
  val addr = Input(UInt(addrLen.W))
  val writeData = Input(UInt(width.W))
  val writeVector = Input(UInt((if (byteAddressable) width / 8 else 1).W))
  val readData = Output(UInt(width.W))
}

/**
  * true dual port ram from XPM 2019.2
  *
  * @param depth           how many lines are in this ram
  * @param width           how many bits per line
  * @param byteAddressable is it byte addressable or does it have a single write enable
  * @param cpuCfg          whether to generate XPM for FPGA or to generate chisel memory for simualtion
  */
@chiselName
class TrueDualPortRam(depth: Int, width: Int, byteAddressable: Boolean)(implicit
                                                                        cpuCfg: CPUConfig = CPUConfig.Build
) extends Module {
  require(isPow2(depth))
  require(
    width % 8 == 0 || !byteAddressable,
    "if memory is byte addressable, then the adderss width must be a multiple of 8"
  )
  val addrLen = log2Ceil(depth)

  val io = IO(new Bundle {
    val portA = new TDPRamBundle(addrLen, width, byteAddressable)
    val portB = new TDPRamBundle(addrLen, width, byteAddressable)
  })

  if (cpuCfg.build) {
    val memory = Module(
      new TDPRamIP(
        dataWidth = width,
        byteWriteWidth = if (byteAddressable) 8 else width,
        addrWidth = addrLen,
        numberOfLines = depth
      )
    )
    memory.io.clka := clock
    memory.io.clkb := clock
    memory.io.rsta := reset
    memory.io.rstb := reset

    memory.io.addra := io.portA.addr
    memory.io.dina := io.portA.writeData
    memory.io.ena := io.portA.portEnable
    memory.io.regcea := false.B
    memory.io.wea := io.portA.writeVector
    io.portA.readData := memory.io.douta

    memory.io.addrb := io.portB.addr
    memory.io.dinb := io.portB.writeData
    memory.io.enb := io.portB.portEnable
    memory.io.regceb := false.B
    memory.io.web := io.portB.writeVector
    io.portB.readData := memory.io.doutb
  } else {
    assert(
      io.portA.writeVector.orR || !io.portA.portEnable,
      "when write port enable is high, write vector cannot be all 0"
    )
    assert(
      !(io.portA.addr === io.portB.addr && io.portA.portEnable && io.portB.portEnable && (io.portA.writeVector.orR || io.portB.writeVector.orR)),
      "there has been an address collision"
    )
    if (byteAddressable) {
      val bank = SyncReadMem(depth, Vec(width / 8, UInt(8.W)))
      when(io.portA.portEnable) {
        when(io.portA.writeVector.orR) {
          bank.write(
            io.portA.addr,
            io.portA.writeData.asTypeOf(Vec(width / 8, UInt(8.W))),
            io.portA.writeVector.asBools()
          )
        }.otherwise {
          io.portA.readData := bank(io.portA.addr)
        }
      }
      when(io.portB.portEnable) {
        when(io.portB.writeVector.orR) {
          bank.write(
            io.portB.addr,
            io.portB.writeData.asTypeOf(Vec(width / 8, UInt(8.W))),
            io.portB.writeVector.asBools()
          )
        }.otherwise {
          io.portB.readData := bank(io.portB.addr)
        }
      }
    } else {
      val bank = SyncReadMem(depth, UInt(width.W))
      when(io.portA.portEnable) {
        when(io.portA.writeVector.asBool) {
          bank(io.portA.addr) := io.portA.writeData
        }.otherwise {
          io.portA.readData := bank(io.portA.addr)
        }
      }
      when(io.portB.portEnable) {
        when(io.portB.writeVector.asBool) {
          bank(io.portB.addr) := io.portB.writeData
        }.otherwise {
          io.portB.readData := bank(io.portB.addr)
        }
      }
    }
  }
}
