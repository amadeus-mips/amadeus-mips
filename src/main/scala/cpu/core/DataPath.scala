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
    // maybe add a mux to the signals, and let the controller control the mux?
    val alu_branch_take = Output(Bool())
  }

  class DataPathIO(implicit val config: PhoenixConfiguration) extends Bundle(){

    val control = Flipped(new ControlToDataIO())
    val data = new DataToControlIO()
  }

  class DataPath(implicit val config: PhoenixConfiguration) extends Module {
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
    val instrMem = Module(new InstrMem(config.memAddressWidth,config.memDataWidth, config.memSize))
    val dataMem = Module(new DataMem(config.memAddressWidth,config.memDataWidth, config.memSize))
    // now the actual implementation
    val io = IO(new DataPathIO())
    io := DontCare

    // wire the next PC
    val pc_next = Wire(UInt(32.W))
    val pc_plus4 = Wire(UInt(32.W))
    val br_target = Wire(UInt(32.W))
    val j_target = Wire(UInt(32.W))

    // handle the pc register
    pc_next := MuxCase(pc_plus4, Array(
      (io.control.PC_isBranch  === true.B) -> br_target,
      (io.control.PC_isJump === true.B) -> j_target
                       ))

    // TODO: fix the initial location of PC
    val reg_PC = RegInit(0.U(32.W))

    // calculate the pc_plus4
    pc_plus4 := reg_PC + 4.U

    // read the instruction from instr mem
    instrMem.io.addr := reg_PC
    val instruction = instrMem.io.readData

    //start decoding
    //-----------------------------------------DECODING-----------------------------------------------------------

    // all the operands. Set them apart
    val rs_address = instruction(25,21)
    val rt_address = instruction(20,16)
    val rd_address = instruction(15,11)
    val shamt = instruction(10,6)
    val immediate = instruction(15,0)
    val address = instruction(25,0)

    // extend stuff we need to extend
    val extendedImmediate = Cat(Fill(16,immediate(15)),immediate)
    // the jump address has 4 upper bits taken from old PC, and the address shift 2 digits
    j_target := Cat(reg_PC(31,28),address,Fill(2,0.U))
    br_target := extendedImmediate + pc_plus4

    val wb_data = Wire(UInt(config.regLen.W))

    val regFile = Module(new RegisterFile())
    regFile.io.rs1Addr := rs_address
    regFile.io.rs2Addr := rt_address
    regFile.io.writeAddr := Mux(io.control.DstRegSelect, rt_address, rd_address)
    regFile.io.writeData := wb_data
    regFile.io.writeEnable := io.control.WBEnable

    // make the value of rs register a wire
    val valRS = Wire(UInt(config.regLen.W))
    val valRT = Wire(UInt(config.regLen.W))

    valRS := regFile.io.rs1Data
    valRT := regFile.io.rs2Data

    val alu = Module(new ALU())
    alu.io.input.inputA := valRS
    // if OpBSelect is true, select rb; otherwise select sign extended immediate
    alu.io.input.inputB := Mux(io.control.OpBSelect, valRT, extendedImmediate)
    alu.io.input.controlSignal := io.control.AluOp

    val isBranchTaken = Wire(Bool())
    val aluOutput = Wire(UInt(config.regLen.W))

    isBranchTaken := alu.io.output.branchTake
    aluOutput := alu.io.output.aluOutput

    // now the memory stage
    //TODO: CSR
    //------------------------------Mem Stage----------------------------------
    dataMem.io.addr := aluOutput
    dataMem.io.writeData := valRT
    dataMem.io.isWrite := io.control.MemWriteEnable

    //data output from data memory
    val readData = Wire(UInt(config.regLen.W))
    readData := dataMem.io.readData
    // drive back to write back stage
    wb_data := Mux(io.control.WBSelect, aluOutput, readData)

    // Write back stage is automatically done in early sections
    reg_PC := pc_next
    // output to the control
    io.data.alu_branch_take := isBranchTaken
    io.data.instruction := instruction


  }
}
