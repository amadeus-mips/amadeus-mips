package cpu.components

import chisel3._
import cpu.CPUConfig

class RegisterFileIn extends Bundle() {
  // register file serial port 1 address
  val rs1Addr = Input(UInt(5.W))
  // register file serial port 2 address
  val rs2Addr = Input(UInt(5.W))
  // register file write to address
  val writeAddr = Input(UInt(5.W))
  // register file write to data
  val writeData = Input(UInt(32.W))
  // register file write enable signal
  val writeEnable = Input(Bool())
}

class RegisterFileOut extends Bundle {
  // register file serial port 1 data
  val rs1Data = Output(UInt(32.W))
  // register file serial port 2 data
  val rs2Data = Output(UInt(32.W))
}

class RegisterFile(implicit val conf: CPUConfig) extends Module {
  val io = IO(new Bundle() {
    val input = new RegisterFileIn
    val output = new RegisterFileOut
  })

  val regs = Reg(Vec(32, UInt(32.W)))

  // When the write enable is high, write the data
  when(io.input.writeEnable) {
    regs(io.input.writeAddr) := io.input.writeData
  }

  // *Always* read the data. This is required for the single cycle CPU since in a single cycle it
  // might both read and write the registers (e.g., an add)
  io.output.rs1Data := regs(io.input.rs1Addr)
  io.output.rs2Data := regs(io.input.rs2Addr)

  if (conf.cpuType != "single-cycle") {
    // For the five-cycle and pipelined CPU forward the data through the register file
    when((io.input.rs1Addr === io.input.writeAddr) && io.input.writeEnable) {
      io.output.rs1Data := io.input.writeData
    }.elsewhen((io.input.rs2Addr === io.input.writeAddr) && io.input.writeEnable) {
      io.output.rs2Data := io.input.writeData
    }
  }
}
