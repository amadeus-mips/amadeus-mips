package cpu.core
{
  import chisel3._
  import chisel3.util._
  import cpu.common._
  import cpu.common.{InstrMem, PhoenixConfiguration}


  class DataToControlIO(implicit val config: PhoenixConfiguration) extends Bundle(){
    // the instruction to decode
    val instruction = Output(UInt(config.regLen.W))
    //TODO: an alternate way to do this is to add extra ports to the alu to make branch work
    val alu_branch_take = Output(Bool())
  }

  class DataPathIO(implicit val config: PhoenixConfiguration) extends Bundle(){
    val instrMem = new InstrMem(config.memAddressWidth,config.memDataWidth, config.memSize)
    val dataMem = new DataMem(config.memAddressWidth,config.memDataWidth, config.memSize)
    val control = Flipped(new ControlToDataIO())
    val data = new DataToControlIO()
  }

  class DataPath(implicit val config: PhoenixConfiguration, memSize: Int) extends Module {
    // // the pipeline registers
    // // Instruction Fetch
    // val instruFetch_PC = RegInit(0.U(config.regLen.W))

    // // decode
    // val decode_PC = RegInit(0.U(config.regLen.W))
    // //TODO: can this initialized to 0, should to no-op
    // val decode_Instruction = RegInit(0.U(config.regLen.W))

    // // execute stage
    // val exe_PC = RegInit(0.U(config.regLen.W))

    // // Memory stage


    // // write back stage


    //TODO: carve the following words into stones and put it before your desk
    //
    //    FIRST ROUND, WE ARE CONSTRUCTING A DATA PATH WITHOUT ANY PIPELINING
    //    THIS IS A HARD PROJECT, AND RUSHING IS NOT REALLY NECESSARY
    //
    // do the instruction fetch here
    // the PC that fetches the instruction

    // the PC register

    //-------------------------------------FETCHING-----------------------------------

    // now the actual implementation
    val io = IO(new DataPathIO())
    io := DontCare

    val pc_next = Wire(UInt(32.W))
    val pc_plus4 = Wire(UInt(32.W))
    val br_target = Wire(UInt(32.W))
    val j_target = Wire(UInt(32.W))

    // handle the pc register
    pc_next := MuxCase(pc_plus4, Array(
      (io.control.PC_isBranch === true.B) -> br_target,
      (io.control.PC_isJump === true.B) -> j_target
                       ))

    // TODO: fix the initial location of PC
    val reg_PC = RegInit(0.U(32.W))

    // calculate the pc_plus4
    pc_plus4 := reg_PC + 4.U(config.regLen)

    // read the instruction from instr mem
    io.instrMem.io.addr := reg_PC
    val instruction = io.instrMem.io.readData

    //start decoding
    //-----------------------------------------DECODING-----------------------------------------------------------

    // all the operands. Set them apart
    val Op = instruction(31,26)
    val rs_address = instruction(25,21)
    val rt_address = instruction(20,16)
    val rd_address = instruction(15,11)
    val shamt = instruction(10,6)
    val funct = instruction(5,0)
    val immediate = instruction(15,0)
    val address = instruction(25,0)

    val wb_data = Wire(UInt(config.regLen.W))

    val regFile = new RegisterFile()
    regFile.io.rs1Addr := rs_address
    regFile.io.rs2Addr := rt_address
    regFile.io.writeAddr := Mux(io.control.DstRegSelect, rt_address, rd_address)
    regFile.io.writeData := wb_data

    val extendedImmediate = Cat(Fill(16,immediate(15)),immediate)



    // decode state
  }
}
