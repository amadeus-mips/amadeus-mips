package cpu.core {

  import chisel3._
  class controlToData extends Bundle(){
    // decode stage
    val dec_stall = Output(Bool())
    // execute stage
    // stall
    val exe_stall = Output(Bool())
    // PC select
    val exe_PC_select = Output(UInt(2.W))

  }
  class ControlPath {

  }

}
