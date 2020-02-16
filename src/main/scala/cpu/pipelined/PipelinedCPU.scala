package cpu.pipelined

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.components.{ALU, BaseCPU, BranchUnit, Controller, RegisterFile, StageRegister}

class PipelinedCPU(implicit val conf: CPUConfig) extends BaseCPU {
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------
  //-------------------------------initialize the modules and hardware components---------------------------
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------

  // initialize the data path modules
  val regPc = RegInit(0.U(32.W))
  val controller = Module(new Controller)
  val regFile = Module(new RegisterFile)
  val branchUnit = Module(new BranchUnit)
  val alu = Module(new ALU)

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

  // fetch the instruction from here
  io.imem.address := regPc
  io.imem.valid := true.B

  val pcPlusFourIF = Wire(UInt(32.W))
  pcPlusFourIF := (regPc + 4.U)

  // when the result from instruction memory is good
  // pipe the instruction into the pipeline status register
  //TODO: add support for stall
  when(io.imem.good) {
    ifToID.io.valid := true.B
    ifToID.io.pipeIn.data.instruction := io.imem.instruction
    ifToID.io.pipeIn.data.nextPc := pcPlusFourIF
  }

  //TODO: how does the PC register change?

  // the branch target and jump target wires are connected from ID
  // target for branching and jump
  val brTarget = Wire(UInt(32.W))
  val jTarget = Wire(UInt(32.W))
  val isBranchToPC = Wire(Bool())
  val isJumpToPC = Wire(Bool())

  regPc := MuxCase(pcPlusFourIF, Array(
    (isBranchToPC) -> brTarget,
    (isJumpToPC) -> jTarget
  ))
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
  // the immediate for an ALU operation
  // extend the address for a branch operation
  val extendedImmediateAddrID = Cat(Fill(14, immediateID(15)), Cat(immediateID, Fill(2, 0.U)))

  // feed the instruction into the controller
  controller.io.input.instr := instructionID

  // decide the next PC
  // the jump address has 4 upper bits taken from old PC, and the address shift 2 digits
  jTarget := Cat(pcPlusFourID(31, 28), addressID, Fill(2, 0.U))
  brTarget := pcPlusFourID + extendedImmediateAddrID

  // initialize the wire for write back data
  // this wire should be assigned at the write back stage
  val wbEnableToReg = Wire(Bool())
  val wbDataToReg = Wire(UInt(32.W))
  val wbAddrToReg = Wire(UInt(5.W))
  val rtAddrToReg = Wire(UInt(5.W))
  val rdAddrToReg = Wire(UInt(5.W))
  regFile.io.rs1Addr := rsAddressID
  regFile.io.rs2Addr := rtAddressID
  regFile.io.writeEnable := wbEnableToReg
  regFile.io.writeData := wbDataToReg
  regFile.io.writeAddr := wbAddrToReg

  branchUnit.io.input.regRs := regFile.io.rs1Data
  branchUnit.io.input.regRt := regFile.io.rs2Data
  branchUnit.io.input.branchOp := controller.io.output.branchOp

  isBranchToPC :=  (controller.io.output.pcIsBranch & branchUnit.io.output.branchTake)
  isJumpToPC := controller.io.output.pcIsJump

  // pipe the values to the state registers
  //TODO: when is this not valid


  idToEX.io.valid := true.B

  // here are the data values
  idToEX.io.pipeIn.data.valRs := regFile.io.rs1Data
  idToEX.io.pipeIn.data.valRt := regFile.io.rs2Data
  idToEX.io.pipeIn.data.immediate := immediateID
  idToEX.io.pipeIn.data.regWriteAddr := Mux(controller.io.output.dstRegSelect, rdAddressID, rtAddressID)


  // here are the control signals
  // -----------------------------execute stage--------------------------
  idToEX.io.pipeIn.control.opBSelect := controller.io.output.opBSelect
  idToEX.io.pipeIn.control.aluOp := controller.io.output.aluOp
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
  val aluOpEx = Wire(UInt(3.W))

  // inherit the data from ID stage
  valRsEX := idToEX.io.pipeOut.data.valRs
  valRtEX := idToEX.io.pipeOut.data.valRt
  immediateEX := idToEX.io.pipeOut.data.immediate

  // get the control signals passed in
  opBSelectEx := idToEX.io.pipeOut.control.opBSelect
  aluOpEx := idToEX.io.pipeOut.control.aluOp

  // get the immediate reday
  val extendedImmediateData = Cat(Fill(16, immediateEX(15)), immediateEX)

  alu.io.input.inputA := valRsEX
  // if OpBSelect is true, select rb; otherwise select sign extended immediate
  alu.io.input.inputB := Mux(opBSelectEx, valRtEX, extendedImmediateData)
  alu.io.input.aluOp := aluOpEx

  val aluOutputEx = Wire(UInt(32.W))
  aluOutputEx := alu.io.output.aluOutput


  // pipe in to the pipeline registers
  exToMEM.io.valid := true.B

  // here are the data values to pipe in
  exToMEM.io.pipeIn.data.aluOutput := aluOutputEx
  exToMEM.io.pipeIn.data.writeData := valRtEX
  exToMEM.io.pipeIn.data.regWriteAddr := idToEX.io.pipeOut.data.regWriteAddr

  // pass along the control signals
  //--------------------------memory stage--------------------------
  exToMEM.io.pipeIn.control.memMask := idToEX.io.pipeOut.control.memMask
  exToMEM.io.pipeIn.control.memSext := idToEX.io.pipeOut.control.memSext
  exToMEM.io.pipeIn.control.memWriteEnable := idToEX.io.pipeOut.control.memWriteEnable
  exToMEM.io.pipeIn.control.wbSelect := idToEX.io.pipeOut.control.wbSelect
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

  // pass the signals and values
  aluOutputMem := exToMEM.io.pipeOut.data.aluOutput
  writeDataMem := exToMEM.io.pipeOut.data.writeData
  memMask := exToMEM.io.pipeOut.control.memMask
  memSext := exToMEM.io.pipeOut.control.memSext
  memWriteEnable := exToMEM.io.pipeOut.control.memWriteEnable
  wbSelect := exToMEM.io.pipeOut.control.wbSelect

  io.dmem.address := aluOutputMem
  io.dmem.writedata := writeDataMem
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
  memToWB.io.pipeIn.data.regWriteAddr := exToMEM.io.pipeOut.data.regWriteAddr


  // pipe in the control signals
  memToWB.io.pipeIn.control.wbEnable := exToMEM.io.pipeOut.control.wbEnable

  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------
  //---------------write back stage-----------------------
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------

  wbEnableToReg := memToWB.io.pipeOut.control.wbEnable
  wbDataToReg := memToWB.io.pipeOut.data.wbData
  wbAddrToReg := memToWB.io.pipeOut.data.regWriteAddr

}
