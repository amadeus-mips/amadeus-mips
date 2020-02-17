package cpu.pipelined

import chisel3._


//TODO: rewrite this to use a generator and enum

// io bundle from instruction fetch stage
// to instruction decode stage
class IFIDBundle extends Bundle {
  val data = new IFIDDataBundle
}

// io bundle from instruction decode stage
// to execute stage
class IDEXBundle extends Bundle {
  val data = new IDEXDataBundle
  val control = new IDEXControlBundle
}

// io bundle from execute stage to memory
// stage
class EXMEMBundle extends Bundle {
  val data = new EXMEMDataBundle
  val control = new EXMEMControlBundle
}

// io bundle from memory stage to write
// back stage
class MEMWBBundle extends Bundle {
  val data = new MEMWBDataBundle
  val control = new MEMWBControlBundle
}


// the data bundle for data path from instruction
// fetch stage to instruction decode stage
class IFIDDataBundle extends Bundle {
  val instruction = UInt(32.W)
  val nextPc = UInt(32.W)
}

// io bundle for data path from instruction decode stage
// to execute stage
class IDEXDataBundle extends Bundle {

  // the values from RS and RT
  val valRs = UInt(32.W)
  val valRt = UInt(32.W)
  // don't pass through the extended immediate
  val immediate = UInt(16.W)

  // register write address
  val regDst = UInt(5.W)

  val regRs = UInt(5.W)
  val regRt = UInt(5.W)
  // jumping and branching should finish at this stage
  // so the address and comparison doesn't pass on
}

// io bundle for data path from execute stage to memory
// stage
class EXMEMDataBundle extends Bundle {
  val aluOutput = UInt(32.W)
  val writeData = UInt(32.W)
  // register write address
  val regDst = UInt(5.W)
}

// io bundle for data path from memory stage to write
// back stage
class MEMWBDataBundle extends Bundle {
  // actually, you can select which data to write at the memory stage
  val wbData = UInt(32.W)
  // register write address
  val regDst = UInt(5.W)
}

// io bundle from instruction decode stage
// to execute stage
class IDEXControlBundle extends Bundle {

  // -------------------------------execute stage----------------------

  // select the operand B
  // true is Reg(rt), false is sign extended offset(16bit)
  val opBSelect = Output(Bool())

  // what is the ALU OP
  val aluOp = Output(UInt(4.W))

  // merge branch unit into ALU for managing complexity ( or bypassing wires go crazy )
  val isBranch = Output(Bool())

  // -----------------------------memory stage--------------------------

  // memory mask mode
  val memMask = Output(UInt(2.W))

  // memory sign extension
  val memSext = Output(Bool())

  // enable write to data memory
  val memWriteEnable = Output(Bool())

  // select write back from read mem and alu output
  // if true, select from alu; if false, select from memory
  val wbSelect = Output(Bool())

  //--------------------------WB stage----------------------------

  // whether write to the regfile
  // true is write back, false is don't write back
  val wbEnable = Output(Bool())

}

// io bundle for control path from execute stage to memory
// stage
class EXMEMControlBundle extends Bundle {
  // -----------------------------memory stage--------------------------

  // memory mask mode
  val memMask = Output(UInt(2.W))

  // memory sign extension
  val memSext = Output(Bool())

  // enable write to data memory
  val memWriteEnable = Output(Bool())

  // select write back from read mem and alu output
  // if true, select from alu; if false, select from memory
  val wbSelect = Output(Bool())

  //--------------------------WB stage----------------------------


  // whether write to the regfile
  // true is write back, false is don't write back
  val wbEnable = Output(Bool())

}

// io bundle for control path from memory stage to write
// back stage
class MEMWBControlBundle extends Bundle {

  //--------------------------WB stage----------------------------

  // whether write to the regfile
  // true is write back, false is don't write back
  val wbEnable = Output(Bool())

}

