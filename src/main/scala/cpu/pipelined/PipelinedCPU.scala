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
  val ifToId = Module(new StageRegister(new IFIDBundle))
  // instruction decode to execute
  val idToEx = Module(new StageRegister(new IDEXBundle))
  // execute to memory access
  val exToMem = Module(new StageRegister(new EXMEMBundle))
  // memory access to write back stage
  val memToWb = Module(new StageRegister(new MEMWBBundle))

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
    ifToId.io.valid := true.B
    ifToId.io.pipeIn.data.instruction := io.imem.instruction
    ifToId.io.pipeIn.data.nextPc := pcPlusFourIF
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
  instructionID := ifToId.io.pipeOut.data.instruction
  pcPlusFourID := ifToId.io.pipeOut.data.nextPc

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
  branchUnit.io.input.branchOp := controller.io.output.BranchOp

  isBranchToPC :=  (controller.io.output.PC_isBranch & branchUnit.io.output.branchTake)
  isJumpToPC := controller.io.output.PC_isJump

  // pipe the values to the state registers
  //TODO: when is this not valid


  idToEx.io.valid := true.B

  // here are the data values
  idToEx.io.pipeIn.data.valRs := regFile.io.rs1Data
  idToEx.io.pipeIn.data.valRt := regFile.io.rs2Data
  idToEx.io.pipeIn.data.immediate := immediateID
  idToEx.io.pipeIn.data.regWriteAddr := Mux(controller.io.output.DstRegSelect, rdAddressID, rtAddressID)


  // here are the control signals
  // -----------------------------execute stage--------------------------
  idToEx.io.pipeIn.control.opBSelect := controller.io.output.OpBSelect
  idToEx.io.pipeIn.control.aluOp := controller.io.output.AluOp
  // -----------------------------memory stage--------------------------
  idToEx.io.pipeIn.control.memMask := controller.io.output.MemMask
  idToEx.io.pipeIn.control.memSext := controller.io.output.MemSext
  idToEx.io.pipeIn.control.memWriteEnable := controller.io.output.MemWriteEnable
  idToEx.io.pipeIn.control.wBSelect := controller.io.output.WBSelect
  // -----------------------------write back stage--------------------------
  idToEx.io.pipeIn.control.wBEnable := controller.io.output.WBEnable

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
  valRsEX := idToEx.io.pipeOut.data.valRs
  valRtEX := idToEx.io.pipeOut.data.valRt
  immediateEX := idToEx.io.pipeOut.data.immediate

  // get the control signals passed in
  opBSelectEx := idToEx.io.pipeOut.control.opBSelect
  aluOpEx := idToEx.io.pipeOut.control.aluOp

  // get the immediate reday
  val extendedImmediateData = Cat(Fill(16, immediateEX(15)), immediateEX)

  alu.io.input.inputA := valRsEX
  // if OpBSelect is true, select rb; otherwise select sign extended immediate
  alu.io.input.inputB := Mux(opBSelectEx, valRtEX, extendedImmediateData)
  alu.io.input.aluOp := aluOpEx

  val aluOutputEx = Wire(UInt(32.W))
  aluOutputEx := alu.io.output.aluOutput


  // pipe in to the pipeline registers
  exToMem.io.valid := true.B

  // here are the data values to pipe in
  exToMem.io.pipeIn.data.aluOutput := aluOutputEx
  exToMem.io.pipeIn.data.writeData := valRtEX
  exToMem.io.pipeIn.data.regWriteAddr := idToEx.io.pipeOut.data.regWriteAddr

  // pass along the control signals
  //--------------------------memory stage--------------------------
  exToMem.io.pipeIn.control.memMask := idToEx.io.pipeOut.control.memMask
  exToMem.io.pipeIn.control.memSext := idToEx.io.pipeOut.control.memSext
  exToMem.io.pipeIn.control.memWriteEnable := idToEx.io.pipeOut.control.memWriteEnable
  exToMem.io.pipeIn.control.wBSelect := idToEx.io.pipeOut.control.wBSelect
  //--------------------------write back stage--------------------------
  exToMem.io.pipeIn.control.wBEnable := idToEx.io.pipeOut.control.wBEnable

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
  val wBSelect = Wire(Bool())

  // pass the signals and values
  aluOutputMem := exToMem.io.pipeOut.data.aluOutput
  writeDataMem := exToMem.io.pipeOut.data.writeData
  memMask := exToMem.io.pipeOut.control.memMask
  memSext := exToMem.io.pipeOut.control.memSext
  memWriteEnable := exToMem.io.pipeOut.control.memWriteEnable
  wBSelect := exToMem.io.pipeOut.control.wBSelect

  io.dmem.address := aluOutputMem
  io.dmem.writedata := writeDataMem
  // when I am selecting the mem read value to perform
  // a write back, I'm doing a write back
  // when I am selecting alu output to perform a write back
  // then I'm not writing back, as I can't do anything with
  // it
  io.dmem.memread := wBSelect
  io.dmem.memwrite := memWriteEnable
  io.dmem.maskmode := memMask
  io.dmem.sext := memSext

  // make it valid if I'm either reading or writing
  io.dmem.valid := (memWriteEnable | wBSelect)

  //TODO: make this more efficient, don't pass in values when
  // neither reading or writing
  val wbDataMem = Wire(UInt(32.W))
  when(wBSelect && io.dmem.good) {
    // If I'm doing a read, write back memory
    wbDataMem := io.dmem.readdata
  }.otherwise {
    // otherwise, write back alu
    wbDataMem := aluOutputMem
  }

  // pass the signals and data to the pipeline registers
  memToWb.io.valid := true.B

  // pipe in the data values
  memToWb.io.pipeIn.data.wbData := wbDataMem
  memToWb.io.pipeIn.data.regWriteAddr := exToMem.io.pipeOut.data.regWriteAddr


  // pipe in the control signals
  memToWb.io.pipeIn.control.wBEnable := exToMem.io.pipeOut.control.wBEnable

  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------
  //---------------write back stage-----------------------
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------

  wbEnableToReg := memToWb.io.pipeOut.control.wBEnable
  wbDataToReg := memToWb.io.pipeOut.data.wbData
  wbAddrToReg := memToWb.io.pipeOut.data.regWriteAddr

}
