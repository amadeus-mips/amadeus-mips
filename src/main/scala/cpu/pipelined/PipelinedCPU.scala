package cpu.pipelined

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.components.{ALU, BaseCPU, Controller, RegisterFile, StageRegister}

class PipelinedCPU(implicit val conf: CPUConfig) extends BaseCPU {
  // initialize the data path modules
  val reg_pc = RegInit(0.U(32.W))
  val controller = Module(new Controller)
  val regFile = Module(new RegisterFile)
  val alu = Module(new ALU)

  // initialize the pipeline stage registers
  val if_id = Module(new StageRegister(new IFIDBundle))
  val id_ex = Module(new StageRegister(new IDEXBundle))
  val ex_mem = Module(new StageRegister(new EXMEMBundle))
  val mem_wb = Module(new StageRegister(new MEMWBBundle))
  //---------------------------------------------------------------------------------
  //-------------------------------instruction fetch stage---------------------------
  //---------------------------------------------------------------------------------

  // fetch the instruction from here
  io.imem.address := reg_pc
  io.imem.valid := true.B

  val if_instruction = Wire(UInt(32.W))
  if_instruction := io.imem.instruction
  // fetch the instruction
  if_id.io.pipeIn.data.instruction := if_instruction

  // make it valid
  //TODO: add support for stall
  if_id.io.valid := true.B

  reg_pc := MuxCase((reg_pc + 4.U), Array(
    (controller.io.output.PC_isBranch) -> br_target,
    (controller.io.output.PC_isJump) -> j_target
  ))
  //---------------------------------------------------------------------------------
  // -----------------------------instruction decode--------------------
  //---------------------------------------------------------------------------------

  val id_instruction = Wire(UInt(32.W))

  id_instruction := if_id.io.pipeOut.data.instruction
  // set up all the immediate
  //TODO: delete the unused ones
  val rs_address = id_instruction(25,21)
  val rt_address = id_instruction(20,16)
  val rd_address = id_instruction(15,11)
  val immediate = id_instruction(15,0)
  val address = id_instruction(25,0)
  val extendedImmediate = Cat(Fill(16,immediate(15)),immediate)

  controller.io.input.instr := id_instruction
    
  val br_target = Wire(UInt(32.W))
  val j_target = Wire(UInt(32.W))


  // decide the next PC
  // the jump address has 4 upper bits taken from old PC, and the address shift 2 digits
  j_target := Cat(reg_pc(31,28),address,Fill(2,0.U))
  br_target := extendedImmediate + reg_pc + 4.U

  // initialize the wire for write back data
  val wb_data = Wire(UInt(32.W))
  regFile.io.rs1Addr := rs_address
  regFile.io.rs2Addr := rt_address
  regFile.io.writeAddr := Mux(controller.io.output.DstRegSelect, rd_address, rt_address)
  regFile.io.writeData := wb_data
  regFile.io.writeEnable := controller.io.output.WBEnable

  val valRS = Wire(UInt(32.W))
  val valRT = Wire(UInt(32.W))
  valRS := regFile.io.rs1Data
  valRT := regFile.io.rs2Data

  //---------------------------------------------------------------------------------
  // -----------------execute stage----------------------
  //---------------------------------------------------------------------------------
  alu.io.input.inputA := valRS
  // if OpBSelect is true, select rb; otherwise select sign extended immediate
  alu.io.input.inputB := Mux(controller.io.output.OpBSelect, valRT, extendedImmediate)
  alu.io.input.controlSignal := controller.io.output.AluOp

  val aluOutput = Wire(UInt(32.W))
  aluOutput := alu.io.output.aluOutput
  controller.io.input.alu_branch_take := alu.io.output.branchTake

  //---------------------------------------------------------------------------------
  // -------------------------memory stage---------------------
  //---------------------------------------------------------------------------------
  io.dmem.address := aluOutput
  io.dmem.writedata := valRT
  io.dmem.memread := controller.io.output.WBSelect
  io.dmem.memwrite := controller.io.output.MemWriteEnable
  //TODO: a design choice:
  // if we are piping Op code through all stages, then there isn't any need for Masks
  // However, if we are not piping op code through, then we definitely need a control signal
  io.dmem.maskmode := controller.io.output.MemMask
  io.dmem.sext := controller.io.output.MemSext
  // a valid interface
  io.dmem.valid := true.B
  //TODO: add a big mux to avoid waiting, though we can avoid this
  // in pipeline?
  val readData = Wire(UInt(32.W))
  readData := io.dmem.readdata
  wb_data := Mux((controller.io.output.WBSelect),readData, aluOutput)
  //---------------------------------------------------------------------------------
  //---------------write back stage-----------------------
  //---------------------------------------------------------------------------------


}
