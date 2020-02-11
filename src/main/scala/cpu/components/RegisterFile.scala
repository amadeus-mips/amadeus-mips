package cpu.components

import chisel3._
import cpu.CPUConfig

  class RegisterFileIO extends Bundle()
  {
    // register file serial port 1 address
    val rs1Addr = Input(UInt(5.W))
    // register file serial port 1 data
    val rs1Data = Output(UInt(32.W))
    // register file serial port 2 address
    val rs2Addr = Input(UInt(5.W))
    // register file serial port 2 data
    val rs2Data = Output(UInt(32.W))

    // register file write to address
    val writeAddr = Input(UInt(5.W))
    // register file write to data
    val writeData = Input(UInt(32.W))
    // register file write enable signal
    val writeEnable= Input(Bool())
  }


class RegisterFile(implicit val conf: CPUConfig) extends Module {
  val io = IO(new RegisterFileIO)

  val regs = Reg(Vec(32, UInt(32.W)))

  // When the write enable is high, write the data
  when (io.writeEnable) {
    regs(io.writeAddr) := io.writeData
  }

  // *Always* read the data. This is required for the single cycle CPU since in a single cycle it
  // might both read and write the registers (e.g., an add)
  io.rs1Data := regs(io.rs1Addr)
  io.rs2Data := regs(io.rs2Addr)
//
//  if (conf.cpuType != "single-cycle") {
//    // For the five-cycle and pipelined CPU forward the data through the register file
//    when (io.readreg1 === io.writereg && io.wen) {
//      io.readdata1 := io.writedata
//    } .elsewhen (io.readreg2 === io.writereg && io.wen) {
//      io.readdata2 := io.writedata
//    }
//  }
}

