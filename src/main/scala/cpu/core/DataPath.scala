package cpu.core
{
  import chisel3._
  import chisel3.util._
  import cpu.common._
  import cpu.common.{PhoenixConfiguration,InstrMem}
import scala.reflect.internal.Mode

  class dataToControl(implicit val config: PhoenixConfiguration) {
    // the instruction to decode
    val decode = Output(UInt(config.regLen.W))
    val exe_branch_eq = Output(Bool())
    val exe_branch_code = Output(UInt(2.W))

    val mem_control_dmem = Output(Bool())
  }

  class DataPathIO(implicit val config: PhoenixConfiguration) {
    val instructionMem = new InstrMem(config.memAddressWidth,config.memDataWidth, 1024)
    val dataMem = new DataMem(config.memAddressWidth,config.memDataWidth, 1024)
    val fromControl = new controlToData()
    val toControl = new dataToControl()
  }

  class DataPath(implicit val config: PhoenixConfiguration, memSize: Int) extends Bundle {
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


    //
    //    FIRST ROUND, WE ARE CONSTRUCTING A DATA PATH WITHOUT ANY PIPELINING
    //    THIS IS A HARD PROJECT, AND RUSHING IS NOT REALLY NECESSARY
    //
    // do the instruction fetch here
    // the PC that fetches the instruction

    // the PC register
    val pcReg = RegInit(0.U(config.regLen.W))

    //PC for instruction fetch stage
    val pc_IF = Wire(pcReg)

    // instantiate the instruction memory
    val instructionMem = Module(new InstrMem(config.memAddressWidth, config.memDataWidth,memSize))


    // read from the instructionMem
    instructionMem.io.addr := pc_IF
    instructionMem.io.isWrite := false.B
    val instruction = instructionMem.io.readData

    // decode state
  }
}
