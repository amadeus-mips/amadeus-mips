package cpu.singleCycle

import chisel3._
import chisel3.util._
import cpu.components.{BaseCPU, Controller, RegisterFile}
import cpu.components._

class SingleCycleCPU() extends BaseCPU{
  //TODO: which mem address to initialize it to
  val reg_pc = RegInit(0.U(32.W))
  val controller = Module(new Controller)
  val regFile = Module(new RegisterFile)
  val alu = Module(new ALU)

  //-------------------------------instruction fetch---------------------------
  // a decoupled interface
  io.imem.address := reg_pc
  io.imem.valid := true.B

  // fetch the instruction
  val instruction = io.imem.instruction

  // -----------------------------instruction decode--------------------

  // set up all the immediate
  //TODO: delete the unused ones
  val rs_address = instruction(25,21)
  val rt_address = instruction(20,16)
  val rd_address = instruction(15,11)
  val immediate = instruction(15,0)
  val address = instruction(25,0)
  val extendedImmediate = Cat(Fill(16,immediate(15)),immediate)

  // feed the instruction into the controller
  //TODO: optimized the bit pattern, reduce bandwidth
  controller.io.input.instr := instruction

  val pc_plus_four = Wire(UInt(32.W))
  val pc_next = Wire(UInt(32.W))
  val br_target = Wire(UInt(32.W))
  val j_target = Wire(UInt(32.W))
  pc_plus_four := reg_pc + 4.U
  // decide the next PC
  // the jump address has 4 upper bits taken from old PC, and the address shift 2 digits
  j_target := Cat(reg_pc(31,28),address,Fill(2,0.U))
  br_target := extendedImmediate + pc_plus_four
  pc_next := MuxCase(pc_plus_four, Array(
    (controller.io.output.PC_isBranch) -> br_target,
    (controller.io.output.PC_isJump) -> j_target
  ))
  // initialize the wire for write back data
  val wb_data = Wire(UInt(32.W))
  regFile.io.rs1Addr := rs_address
  regFile.io.rs2Addr := rt_address
  regFile.io.writeAddr := Mux(controller.io.output.DstRegSelect, rt_address, rd_address)
  regFile.io.writeData := wb_data
  regFile.io.writeEnable := controller.io.output.WBEnable

  val valRS = Wire(UInt(32.W))
  val valRT = Wire(UInt(32.W))
  valRS := regFile.io.rs1Data
  valRT := regFile.io.rs2Data

  // -----------------execute stage----------------------
  alu.io.input.inputA := valRS
  // if OpBSelect is true, select rb; otherwise select sign extended immediate
  alu.io.input.inputB := Mux(controller.io.output.OpBSelect, valRT, extendedImmediate)
  alu.io.input.controlSignal := controller.io.output.AluOp

  val aluOutput = Wire(UInt(32.W))
  aluOutput := alu.io.output.aluOutput
  controller.io.input.alu_branch_take := alu.io.output.branchTake

  // -------------------------memory stage---------------------
  io.dmem.address := aluOutput
  io.dmem.writedata := valRT
  io.dmem.memread := controller.io.output.MemReadEnable
  io.dmem.memwrite := controller.io.output.MemWriteEnable
  // a valid interface
  io.dmem.valid := true.B
  //TODO: add a big mux to avoid waiting, though we can avoid this
  // in pipeline?
  val readData = Wire(UInt(32.W))
  readData := io.dmem.readdata
  wb_data := Mux(controller.io.output.WBSelect, aluOutput, readData)
  //---------------write back stage-----------------------
  reg_pc := pc_next

}
