//**************************************************************************
// RISCV Processor Register File
//--------------------------------------------------------------------------
//

package cpu.core
{

  import cpu.common.PhoenixConfiguration

  import chisel3._

  // this is only here because of sodor, I don't have the remotest idea why this is here
  // the io of the register file
  class RegisterFileIO(implicit val conf: PhoenixConfiguration) extends Bundle()
  {
    // register file serial port 1 address
    val rs1Addr = Input(UInt(5.W))
    // register file serial port 1 data
    val rs1Data = Output(UInt(conf.regLen.W))
    // register file serial port 2 address
    val rs2Addr = Input(UInt(5.W))
    // register file serial port 2 data
    val rs2Data = Output(UInt(conf.regLen.W))

    // register file write to address
    val writeAddr = Input(UInt(5.W))
    // register file write to data
    val writeData = Input(UInt(conf.regLen.W))
    // register file write enable signal
    val writeEnable= Input(Bool())
  }

  // the actual register file module
  class RegisterFile(implicit val conf: PhoenixConfiguration) extends Module
  {
    val io = IO(new RegisterFileIO())

    val registerFile = Mem(32,UInt(conf.regLen.W))

    when (io.writeEnable && (io.writeAddr =/= 0.U))
    {
      registerFile(io.writeAddr) := io.writeData
    }

    io.rs1Data := Mux((io.rs1Addr =/= 0.U), registerFile(io.rs1Addr), 0.U)
    io.rs2Data := Mux((io.rs2Addr =/= 0.U), registerFile(io.rs2Addr), 0.U)

  }
}
