package cpu.core {

  import chisel3._
import cpu.common._


  class ControlToDataIO extends Bundle(){
    // whether the PC takes the branch
    // true is take the branch, false is don't take the branch
    val PC_isBranch = Output(Bool())
    // whether the PC takes the jump
    // true is take the jump, false is don't take the jump
    // case when isJump and isBranch is undefined, should not happen
    val PC_isJump = Output(Bool())
    // which is the dst register, rd or rt
    // false is rt, true is rd
    val DstRegSelect = Output(Bool())
    // whether write to the regfile
    // true is write back, false is don't write back
    val WBEnable = Output(Bool())
    // select the operand B
    // true is Reg(rt), false is sign extended offset(16bit)
    val OpBSelect = Output(Bool())
    // what is the ALU OP
    val AluOp = Output(UInt(4.W))
    // enable write to data memory
    val MemWrite = Output(Bool())
    // select write back from read mem and alu output
    // if true, select from alu; if false, select from memory
    val WBSelect = Output(Bool())
  }


  class ControlPathIO(implicit val conf: PhoenixConfiguration ) extends Bundle() {
    val instrMem = new InstrMem(conf.memAddressWidth, conf.memDataWidth, conf.memSize)
    val dataMem = new DataMem(conf.memAddressWidth, conf.memDataWidth, conf.memSize)
    val data = Flipped(new DataToControlIO())
    val control = new ControlToDataIO()
  }

  class ControlPath {

  }

}
