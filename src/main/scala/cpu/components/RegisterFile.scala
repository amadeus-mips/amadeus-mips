package cpu.components

import chisel3._

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

  // the actual register file module
  class RegisterFile extends Module
  {
    val io = IO(new RegisterFileIO())

    val registerFile = Mem(32,UInt(32.W))

    when (io.writeEnable && (io.writeAddr =/= 0.U))
    {
      registerFile(io.writeAddr) := io.writeData
    }

    io.rs1Data := Mux((io.rs1Addr =/= 0.U), registerFile(io.rs1Addr), 0.U)
    io.rs2Data := Mux((io.rs2Addr =/= 0.U), registerFile(io.rs2Addr), 0.U)

  }
