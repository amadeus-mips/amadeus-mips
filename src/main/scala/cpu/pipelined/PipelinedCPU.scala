package cpu.pipelined

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.components.{
  ALU,
  AddressUnit,
  BaseCPU,
  BranchUnit,
  BypassUnit,
  Controller,
  HazardUnit,
  RegisterFile,
  StageRegister
}

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
  val regPC = RegInit(0.U(32.W))
  val controller = Module(new Controller)
  val regFile = Module(new RegisterFile)
  val alu = Module(new ALU)
  val branch = Module(new BranchUnit)
  val brAddress = Module(new AddressUnit)
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

  io.imem.address := regPC
  io.imem.valid := (!hazard.io.output.ifIDStall)

  // make it a wire
  val pcPlusFourIF = Wire(UInt(32.W))
  pcPlusFourIF := (regPC + 4.U)

  // the branch target and jump target wires are connected from ID
  // target for branching and jump
  val brTarget = Wire(UInt(32.W))
  val jTarget = Wire(UInt(32.W))

  regPC := MuxLookup(
    hazard.io.output.pcWrite,
    pcPlusFourIF,
    Array(
      1.U -> brTarget,
      2.U -> regPC,
      3.U -> jTarget
    )
  )

  // status register's flush and valid
  ifToID.io.valid := (!hazard.io.output.ifIDStall)
  ifToID.io.flush := hazard.io.output.ifIDFlush

  //TODO: as we'are using combinational memory, every cycle is valid
  // change this to sync mem in the future
  ifToID.io.pipeIn.data.instruction := io.imem.instruction
  ifToID.io.pipeIn.data.pcPlusFour := pcPlusFourIF

  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------
  // -----------------------------instruction decode--------------------
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------

  // wire for the instruction
  val instructionID = Wire(UInt(32.W))

  // assign the wires from the state registers
  instructionID := ifToID.io.pipeOut.data.instruction

  // set up all the immediate
  val rsAddressID = instructionID(25, 21)
  val rtAddressID = instructionID(20, 16)
  val rdAddressID = instructionID(15, 11)
  val immediateID = instructionID(15, 0)
  val addressID = instructionID(25, 0)

  //hazard control unit input here
  hazard.io.input.idRs := rsAddressID
  hazard.io.input.idRt := rtAddressID

  //TODO: change this to opcode
  controller.io.input.instr := instructionID

  // initialize the wire for write back data
  // this wire should be assigned at the write back stage
  val wbEnableToReg = Wire(Bool())
  val wbDataToReg = Wire(UInt(32.W))
  val wbAddrToReg = Wire(UInt(5.W))

  // register file
  regFile.io.input.rs1Addr := rsAddressID
  regFile.io.input.rs2Addr := rtAddressID
  regFile.io.input.writeEnable := wbEnableToReg
  regFile.io.input.writeData := wbDataToReg
  regFile.io.input.writeAddr := wbAddrToReg

  // pass along to pipeline registers
  idToEX.io.valid := true.B
  idToEX.io.flush := hazard.io.output.idEXFlush
  // here are the data values
  idToEX.io.pipeIn.data.valRs := regFile.io.output.rs1Data
  idToEX.io.pipeIn.data.valRt := regFile.io.output.rs2Data
  idToEX.io.pipeIn.data.immediate := immediateID
  idToEX.io.pipeIn.data.regDst := MuxLookup(
    controller.io.output.dstRegSelect,
    rtAddressID,
    Array(
      1.U -> rdAddressID,
      2.U -> 31.U(5.W)
    )
  )
  idToEX.io.pipeIn.data.regRs := rsAddressID
  idToEX.io.pipeIn.data.regRt := rtAddressID
  idToEX.io.pipeIn.data.pcPlusFour := ifToID.io.pipeOut.data.pcPlusFour
  idToEX.io.pipeIn.data.jAddress := addressID

  // here are the control signals
  // -----------------------------execute stage--------------------------
  idToEX.io.pipeIn.control.opBSelect := controller.io.output.opBSelect
  idToEX.io.pipeIn.control.aluOp := controller.io.output.aluOp
  idToEX.io.pipeIn.control.isBranch := controller.io.output.isBranch
  idToEX.io.pipeIn.control.branchOp := controller.io.output.branchOp
  idToEX.io.pipeIn.control.isJump := controller.io.output.isJump
  idToEX.io.pipeIn.control.jumpOp := controller.io.output.jumpOp
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
  val regRsEX = Wire(UInt(5.W))
  val regRtEX = Wire(UInt(5.W))
  val dstRegEX = Wire(UInt(5.W))
  val memReadEX = Wire(Bool())
  val aluOutputEX = Wire(UInt(32.W))
  val pcPlusFourEX = Wire(UInt(32.W))

  // inherit the data from ID stage
  valRsEX := idToEX.io.pipeOut.data.valRs
  valRtEX := idToEX.io.pipeOut.data.valRt
  immediateEX := idToEX.io.pipeOut.data.immediate
  regRsEX := idToEX.io.pipeOut.data.regRs
  regRtEX := idToEX.io.pipeOut.data.regRt
  dstRegEX := idToEX.io.pipeOut.data.regDst
  pcPlusFourEX := idToEX.io.pipeOut.data.pcPlusFour

  // get the control signals passed in
  // true is mem read
  memReadEX := idToEX.io.pipeOut.control.wbSelect

  // hazard unit
  hazard.io.input.idEXDstReg := dstRegEX
  hazard.io.input.idEXMemread := memReadEX
  hazard.io.input.exBranchTake := (idToEX.io.pipeOut.control.isBranch & branch.io.output.branchTake)
  hazard.io.input.exIsJump := idToEX.io.pipeOut.control.isJump

  // bypassing unit
  bypass.io.input.idEXRs := regRsEX
  bypass.io.input.idEXRt := regRtEX
  bypass.io.input.idEXRegWriteEnable := idToEX.io.pipeOut.control.memWriteEnable
  bypass.io.input.idEXRegDst := dstRegEX

  // stage at memory stage, put here to omit warnings
  val wbDataMem = Wire(UInt(32.W))
  //ALU unit
  alu.io.input.aluOp := idToEX.io.pipeOut.control.aluOp

  alu.io.input.shamt := immediateEX(10, 6)

  // input A is always rs ( forwarded or not )
  alu.io.input.inputA := MuxLookup(
    bypass.io.output.forwardRs,
    valRsEX,
    Array(
      1.U -> wbDataMem,
      2.U -> wbDataToReg
    )
  )

  alu.io.input.inputB := MuxLookup(
    idToEX.io.pipeOut.control.opBSelect,
    Cat(Fill(16, immediateEX(15)), immediateEX),
    Array(
      1.U -> MuxLookup(
        bypass.io.output.forwardRt,
        valRtEX,
        Array(
          1.U -> wbDataMem,
          2.U -> wbDataToReg
        )
      ),
      2.U -> pcPlusFourEX
    )
  )

  aluOutputEX := alu.io.output.aluOutput

  // the branch unit
  // branch rs bypassing
  branch.io.input.regRs := MuxLookup(
    bypass.io.output.forwardRs,
    valRsEX,
    Array(
      1.U -> wbDataMem,
      2.U -> wbDataToReg
    )
  )

  // branch rt bypassing
  branch.io.input.regRt := MuxLookup(
    bypass.io.output.forwardRt,
    valRtEX,
    Array(
      1.U -> wbDataMem,
      2.U -> wbDataToReg
    )
  )

  // branch op
  branch.io.input.branchOp := idToEX.io.pipeOut.control.branchOp

  // jump target calculation
  jTarget := Mux(
    idToEX.io.pipeOut.control.jumpOp,
    Cat(pcPlusFourEX(31, 28), idToEX.io.pipeOut.data.jAddress, Fill(2, 0.U)),
    MuxLookup(
      bypass.io.output.forwardRs,
      valRsEX,
      Array(
        1.U -> wbDataMem,
        2.U -> wbDataToReg
      )
    )
  )

  // branch address calculator
  brAddress.io.input.branchOffSet := immediateEX
  brAddress.io.input.pcPlusFour := pcPlusFourEX
  brTarget := brAddress.io.output.branchTarget

  // pipe in to the pipeline registers
  exToMEM.io.valid := true.B
  exToMEM.io.flush := hazard.io.output.exMemFlush

  // here are the data values to pipe in
  exToMEM.io.pipeIn.data.aluOutput := aluOutputEX
  exToMEM.io.pipeIn.data.writeData := valRtEX
  exToMEM.io.pipeIn.data.regDst := dstRegEX

  // pass along the control signals
  //--------------------------memory stage--------------------------
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

  io.dmem.address := aluOutputMem
  // if data memory has a forward
  io.dmem.writedata := Mux(bypass.io.output.forwardMemWriteData, wbDataToReg, writeDataMem)
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

  wbDataMem := Mux(wbSelect, io.dmem.readdata, aluOutputMem)

  // pass the signals and data to the pipeline registers
  memToWB.io.valid := true.B
  memToWB.io.flush := false.B

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

  // preventing writing to register zero
  wbEnableToReg := (memToWB.io.pipeOut.control.wbEnable && wbAddrToReg.orR)
  wbDataToReg := memToWB.io.pipeOut.data.wbData
  wbAddrToReg := memToWB.io.pipeOut.data.regDst

  // setup the bypass signal
  bypass.io.input.memWBRegDst := wbAddrToReg
  bypass.io.input.memWBRegWriteEnable := wbEnableToReg

}

object PipelinedCPUInfo {
  def getModules(): List[String] = {
    List(
      "imem",
      "dmem",
      "controller",
      "regFile",
      "alu",
      "bypass",
      "hazard",
      "branch",
      "brAddress"
    )
  }

  def getPipelineRegs(): List[String] = {
    List(
      "ifToID",
      "idToEX",
      "exToMEM",
      "memToWB"
    )
  }
}
