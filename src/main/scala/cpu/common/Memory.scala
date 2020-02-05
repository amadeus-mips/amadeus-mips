package cpu.common{

  import chisel3._

  // most simple memory


  /**
    * the instruction memory
    * @param addrWidth the address width of the memory
    * @param dataWidth the data width of the memory
    * @param memSize the size of the memory ( word )
    */
  class InstrMem( addrWidth : Int, dataWidth : Int, memSize : Int) extends Module() {
    val io = IO(new Bundle {
      // select read or write, true is write, false is read
      // the address for write/ read
      val addr = Input(UInt(addrWidth.W))
      // the data to write
      // the data to read
      val readData = Output(UInt(dataWidth.W))
    })
    val mem = SyncReadMem(memSize, UInt(addrWidth.W))
    io.readData := mem.read(io.addr, true.B)

  }


  /**
    * the data  memory
    * @param addrWidth the address width of the memory
    * @param dataWidth the data width of the memory
    * @param memSize the size of the memory ( word )
    */
  class DataMem( addrWidth : Int, dataWidth : Int, memSize : Int) extends Module() {
    val io = IO(new Bundle {
      // select read or write, true is write, false is read
      val isWrite = Input(Bool())
      // the address for write/ read
      val addr = Input(UInt(addrWidth.W))
      // the data to write
      val writeData = Input(UInt(dataWidth.W))
      // the data to read
      val readData = Output(UInt(dataWidth.W))
    })
    val mem = SyncReadMem(memSize, UInt(addrWidth.W))
    when (io.isWrite) {
      mem.write(io.addr,io.writeData);
      io.readData := DontCare
    }.otherwise {
      // second argument is for enabling the memory
      io.readData := mem.read(io.addr, true.B)
    }
  }
}
