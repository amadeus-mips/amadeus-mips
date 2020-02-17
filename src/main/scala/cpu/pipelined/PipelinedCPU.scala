package cpu.pipelined

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.components.{ALU, BaseCPU, BypassUnit, Controller, HazardUnit, RegisterFile, StageRegister}

class PipelinedCPU(implicit val conf: CPUConfig) extends BaseCPU {

  /**
    * about connecting a := b, c := a
    * this will prolong the data path, but they'll be in the same cycle
    * in other words, this will prolong your cycle time
    */

  /**
    * wire or direct connection:
    * if only 1 connection: direct
    * num of connections >= 2: wire
    */

  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------
  //-------------------------------initialize the modules and hardware components---------------------------
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------

  // initialize the data path modules
  val regPc = RegInit(0.U(32.W))
  val controller = Module(new Controller)
  val regFile = Module(new RegisterFile)
  val alu = Module(new ALU)
  val bypass = Module(new BypassUnit)
  val hazard = Module(new HazardUnit)

  // initialize the pipeline stage registers
  // instruction fetch to instruction decode
  val ifToID = Module(new StageRegister(new IFIDBundle))
  // instruction decode to execute
  val idToEX = Module(new StageRegister(new IDEXBundle))
  // execute to memory access
  val exToMEM = Module(new StageRegister(new EXMEMBundle))
  // memory access to write back stage
  val memToWB = Module(new StageRegister(new MEMWBBundle))

  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------
  //-------------------------------instruction fetch stage---------------------------
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------

  io.imem.address := regPc
  io.imem.valid := (!hazard.io.output.ifIDStall)

  // make it a wire
  val pcPlusFourIF = Wire(UInt(32.W))
  pcPlusFourIF := (regPc + 4.U)

  // the branch target and jump target wires are connected from ID
  // target for branching and jump
  val brTarget = Wire(UInt(32.W))
  val jTarget = Wire(UInt(32.W))

  regPc := MuxLookup(hazard.io.output.pcWrite,pcPlusFourIF, Array(
    1.U -> brTarget,
    2.U -> regPc,
    4.U -> jTarget
  ))

  // status register's flush and valid
  ifToID.io.valid := (!hazard.io.output.ifIDStall)
  ifToID.io.flush := hazard.io.output.ifIDFlush

  //TODO: as we'are using combinational memory, every cycle is valid
  // change this to sync mem in the future
  ifToID.io.pipeIn.data.instruction := io.imem.instruction
  ifToID.io.pipeIn.data.nextPc := pcPlusFourIF

  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------
  // -----------------------------instruction decode--------------------
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------

  // wire for the instruction
  val instructionID = Wire(UInt(32.W))
  // wire for the next PC
  val pcPlusFourID = Wire(UInt(32.W))

  // assign the wires from the state registers
  instructionID := ifToID.io.pipeOut.data.instruction
  pcPlusFourID := ifToID.io.pipeOut.data.nextPc


  // set up all the immediate
  val rsAddressID = instructionID(25, 21)
  val rtAddressID = instructionID(20, 16)
  val rdAddressID = instructionID(15, 11)
  val immediateID = instructionID(15, 0)
  val addressID = instructionID(25, 0)

  //TODO: change this to opcode
  // feed the instruction into the controller
  controller.io.input.instr := instructionID

  // decide the next PC
  // the jump address has 4 upper bits taken from old PC, and the address shift 2 digits
  jTarget := Cat(pcPlusFourID(31, 28), addressID, Fill(2, 0.U))

  // branch target
  //TODO: move the adder into the ALU
  //ignored for now for simplicity
  val extendedImmediateAddrID = Cat(Fill(14, immediateID(15)), Cat(immediateID, Fill(2, 0.U)))


  // initialize the wire for write back data
  // this wire should be assigned at the write back stage
  val wbEnableToReg = Wire(Bool())
  val wbDataToReg = Wire(UInt(32.W))
  val wbAddrToReg = Wire(UInt(5.W))

  regFile.io.rs1Addr := rsAddressID
  regFile.io.rs2Addr := rtAddressID
  regFile.io.writeEnable := wbEnableToReg
  regFile.io.writeData := wbDataToReg
  regFile.io.writeAddr := wbAddrToReg

  //hazard control unit input here
  hazard.io.input.idRs := rsAddressID
  hazard.io.input.idRt := rtAddressID
  hazard.io.input.idIsJump := controller.io.output.pcIsJump

  idToEX.io.valid := true.B
  idToEX.io.flush := hazard.io.output.idEXFlush
  // here are the data values
  idToEX.io.pipeIn.data.valRs := regFile.io.rs1Data
  idToEX.io.pipeIn.data.valRt := regFile.io.rs2Data
  idToEX.io.pipeIn.data.immediate := immediateID
  idToEX.io.pipeIn.data.regDst := Mux(controller.io.output.dstRegSelect, rdAddressID, rtAddressID)
  idToEX.io.pipeIn.data.regRs  := rsAddressID
  idToEX.io.pipeIn.data.regRt  := rtAddressID
  idToEX.io.pipeIn.data.branchTarget := pcPlusFourID + extendedImmediateAddrID

  // here are the control signals
  // -----------------------------execute stage--------------------------
  idToEX.io.pipeIn.control.opBSelect := controller.io.output.opBSelect
  idToEX.io.pipeIn.control.aluOp := controller.io.output.aluOp
  idToEX.io.pipeIn.control.isBranch := controller.io.output.pcIsBranch
  // -----------------------------memory stage--------------------------
  idToEX.io.pipeIn.control.memMask := controller.io.output.memMask
  idToEX.io.pipeIn.control.memSext := controller.io.output.memSext
  idToEX.io.pipeIn.control.memWriteEnable := controller.io.output.memWriteEnable
  idToEX.io.pipeIn.control.wbSelect := controller.io.output.wbSelect
  // -----------------------------write back stage--------------------------
  idToEX.io.pipeIn.control.wbEnable := controller.io.output.wbEnable

  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------
  // -----------------execute stage----------------------
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------

  // create the wires for reading from last pipeline registers
  val valRsEX = Wire(UInt(32.W))
  val valRtEX = Wire(UInt(32.W))
  val immediateEX = Wire(UInt(16.W))
  val opBSelectEx = Wire(Bool())
  val aluOpEX = Wire(UInt(4.W))
  val regRsEX = Wire(UInt(5.W))
  val regRtEX = Wire(UInt(5.W))
  val dstRegEX = Wire(UInt(5.W))
  val memReadEX = Wire(Bool())

  // inherit the data from ID stage
  valRsEX := idToEX.io.pipeOut.data.valRs
  valRtEX := idToEX.io.pipeOut.data.valRt
  immediateEX := idToEX.io.pipeOut.data.immediate
  regRsEX := idToEX.io.pipeOut.data.regRs
  regRtEX := idToEX.io.pipeOut.data.regRt
  dstRegEX := idToEX.io.pipeOut.data.regDst

  // bypassing unit
  bypass.io.input.idEXRs := regRsEX
  bypass.io.input.idEXRt := regRtEX

  // get the control signals passed in
  opBSelectEx := idToEX.io.pipeOut.control.opBSelect
  aluOpEX := idToEX.io.pipeOut.control.aluOp
  // true is mem read
  memReadEX := idToEX.io.pipeOut.control.wbSelect


  // get the immediate reday
  val extendedImmediateData = Cat(Fill(16, immediateEX(15)), immediateEX)



  alu.io.input.inputA := MuxCase(valRsEX, Array(
    (bypass.io.output.forwardALUOpA === 1.U) -> exToMEM.io.pipeOut.data.aluOutput ,
    (bypass.io.output.forwardALUOpA === 2.U) -> wbDataToReg
  ))
  // if OpBSelect is true, select rt; otherwise select sign extended immediate
  alu.io.input.inputB := MuxCase(Mux(opBSelectEx, valRtEX, extendedImmediateData), Array (
    (bypass.io.output.forwardALUOpB === 1.U) -> exToMEM.io.pipeOut.data.aluOutput,
    (bypass.io.output.forwardALUOpA === 2.U) -> wbDataToReg
  ))

  alu.io.input.aluOp := aluOpEX

  val aluOutputEx = Wire(UInt(32.W))
  val isBranchEx = Wire(Bool())

  aluOutputEx := alu.io.output.aluOutput
  isBranchEx := idToEX.io.pipeOut.control.isBranch

  hazard.io.input.idEXDstReg := dstRegEX
  hazard.io.input.idEXMemread := memReadEX

  // pipe in to the pipeline registers
  exToMEM.io.valid := true.B
  exToMEM.io.flush := hazard.io.output.exMemFlush

  // here are the data values to pipe in
  exToMEM.io.pipeIn.data.aluOutput := aluOutputEx
  exToMEM.io.pipeIn.data.writeData := valRtEX
  exToMEM.io.pipeIn.data.regDst := dstRegEX
  exToMEM.io.pipeIn.data.branchTarget := idToEX.io.pipeOut.data.branchTarget

  // pass along the control signals
  //--------------------------memory stage--------------------------
  exToMEM.io.pipeIn.control.branchTake := (isBranchEx & alu.io.output.branchTake)
  exToMEM.io.pipeIn.control.memMask := idToEX.io.pipeOut.control.memMask
  exToMEM.io.pipeIn.control.memSext := idToEX.io.pipeOut.control.memSext
  exToMEM.io.pipeIn.control.memWriteEnable := idToEX.io.pipeOut.control.memWriteEnable
  exToMEM.io.pipeIn.control.wbSelect := memReadEX
  //--------------------------write back stage--------------------------
  exToMEM.io.pipeIn.control.wbEnable := idToEX.io.pipeOut.control.wbEnable

  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------
  // -------------------------memory stage---------------------
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------

  // initialze the wires
  // data wire
  val aluOutputMem = Wire(UInt(32.W))
  val writeDataMem = Wire(UInt(32.W))
  // control signal wires
  val memMask = Wire(UInt(2.W))
  val memSext = Wire(Bool())
  val memWriteEnable = Wire(Bool())
  val wbSelect = Wire(Bool())
  val regDstMem = Wire(UInt(5.W))
  val regWriteEnableMem = Wire(Bool())

  // pass the signals and values
  aluOutputMem := exToMEM.io.pipeOut.data.aluOutput
  writeDataMem := exToMEM.io.pipeOut.data.writeData
  memMask := exToMEM.io.pipeOut.control.memMask
  memSext := exToMEM.io.pipeOut.control.memSext
  memWriteEnable := exToMEM.io.pipeOut.control.memWriteEnable
  wbSelect := exToMEM.io.pipeOut.control.wbSelect
  regDstMem := exToMEM.io.pipeOut.data.regDst
  regWriteEnableMem := exToMEM.io.pipeOut.control.wbEnable

  bypass.io.input.exMemRegDst := regDstMem
  bypass.io.input.exMemRegWriteEnable := regWriteEnableMem

  hazard.io.input.exMemBranchTake := exToMEM.io.pipeOut.control.branchTake


  brTarget := exToMEM.io.pipeOut.data.branchTarget

  io.dmem.address := aluOutputMem
  // if data memory has a forward
  io.dmem.writedata := Mux(bypass.io.output.forwardMemWriteData,wbDataToReg,writeDataMem)
  // when I am selecting the mem read value to perform
  // a write back, I'm doing a write back
  // when I am selecting alu output to perform a write back
  // then I'm not writing back, as I can't do anything with
  // it
  io.dmem.memread := wbSelect
  io.dmem.memwrite := memWriteEnable
  io.dmem.maskmode := memMask
  io.dmem.sext := memSext

  // make it valid if I'm either reading or writing
  io.dmem.valid := (memWriteEnable | wbSelect)

  //TODO:
  // this is not working correctly
  // Only supports combinational memory for now
  val wbDataMem = Wire(UInt(32.W))
  when(wbSelect && io.dmem.good) {
    // If I'm doing a read, write back memory
    wbDataMem := io.dmem.readdata
  }.otherwise {
    // otherwise, write back alu
    wbDataMem := aluOutputMem
  }

  // pass the signals and data to the pipeline registers
  memToWB.io.valid := true.B

  // pipe in the data values
  memToWB.io.pipeIn.data.wbData := wbDataMem
  memToWB.io.pipeIn.data.regDst := regDstMem


  // pipe in the control signals
  memToWB.io.pipeIn.control.wbEnable := regWriteEnableMem

  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------
  //---------------write back stage-----------------------
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------

  // setup the bypass signal
  bypass.io.input.memWBRegDst := wbAddrToReg
  bypass.io.input.memWBRegWriteEnable := wbEnableToReg

  wbEnableToReg := memToWB.io.pipeOut.control.wbEnable
  wbDataToReg := memToWB.io.pipeOut.data.wbData
  wbAddrToReg := memToWB.io.pipeOut.data.regDst

}
