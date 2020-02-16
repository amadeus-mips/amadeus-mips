package cpu.pipelined

import chisel3._


//TODO: rewrite this to use a generator and enum

// io bundle from instruction fetch stage
// to instruction decode stage
// TODO: does this exist?
class IFIDBundle extends Bundle {
  val data = new IFIDDataBundle
  val control = new IFIDControlBundle
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
  val contrl = new MEMWBControlBundle
}


// the data bundle for data path from instruction
// fetch stage to instruction decode stage
class IFIDDataBundle extends Bundle {
  val instruction = UInt(32.W)
  val pc = UInt(32.W)
}

// io bundle for data path from instruction decode stage
// to execute stage
class IDEXDataBundle extends Bundle {

  // the values from RS and RT
  val valRs = UInt(32.W)
  val valRt = UInt(32.W)
  // don't pass through the extended immediate
  val Immediate = UInt(16.W)
  val address = UInt(26.W)

}

// io bundle for data path from execute stage to memory
// stage
class EXMEMDataBundle extends Bundle {
  val aluOutput = UInt(32.W)
}

// io bundle for data path from memory stage to write
// back stage
class MEMWBDataBundle extends Bundle {

}

// io bundle from instruction fetch stage
// to instruction decode stage
// TODO: does this exist?
class IFIDControlBundle extends Bundle {

}

// io bundle from instruction decode stage
// to execute stage
class IDEXControlBundle extends Bundle {

}

// io bundle for control path from execute stage to memory
// stage
class EXMEMControlBundle extends Bundle {

}

// io bundle for control path from memory stage to write
// back stage
class MEMWBControlBundle extends Bundle {

}

