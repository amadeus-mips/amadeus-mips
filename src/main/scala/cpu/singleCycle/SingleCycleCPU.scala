package cpu.singleCycle

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.components.{BaseCPU, Controller, RegisterFile, _}

class SingleCycleCPU(implicit val conf: CPUConfig) extends BaseCPU {
  // initialize all the modules
  // sequentially
  val reg_pc = RegInit(0.U(32.W))
  val controller = Module(new Controller)
  val regFile = Module(new RegisterFile)
  val branchUnit = Module(new BranchUnit)
  val alu = Module(new ALU)

  //-------------------------------instruction fetch---------------------------
  // a decoupled interface
  io.imem.address := reg_pc
  io.imem.valid := true.B

  // fetch the instruction
  val instruction = io.imem.instruction

  // -----------------------------instruction decode--------------------

  // set up all the immediate
  val rs_address = instruction(25, 21)
  val rt_address = instruction(20, 16)
  val rd_address = instruction(15, 11)
  val immediate = instruction(15, 0)
  val address = instruction(25, 0)
  val extendedImmediateData = Cat(Fill(16, immediate(15)), immediate)
  val extendedImmediateAddr = Cat(Fill(14, immediate(15)), immediate, Fill(2, 0.U))

  // feed the instruction into the controller
  //TODO: optimized the bit pattern, reduce bandwidth
  controller.io.input.instr := instruction

  // initialize the wire for write back data
  val wb_data = Wire(UInt(32.W))
  regFile.io.input.rs1Addr := rs_address
  regFile.io.input.rs2Addr := rt_address
  regFile.io.input.writeAddr := Mux(controller.io.output.dstRegSelect, rd_address, rt_address)
  regFile.io.input.writeData := wb_data
  regFile.io.input.writeEnable := controller.io.output.wbEnable

  val valRS = Wire(UInt(32.W))
  val valRT = Wire(UInt(32.W))
  valRS := regFile.io.output.rs1Data
  valRT := regFile.io.output.rs2Data

  branchUnit.io.input.regRs := valRS
  branchUnit.io.input.regRt := valRT
//  temporary comment to test pipelined processor
//  TODO: remove this2
//  branchUnit.io.input.branchOp := controller.io.output.branchOp

  val br_target = Wire(UInt(32.W))
  val j_target = Wire(UInt(32.W))
  val is_Branch = Wire(Bool())
  val pc_plus_four = Wire(UInt(32.W))
  val pc_next = Wire(UInt(32.W))
  is_Branch := (controller.io.output.pcIsBranch & branchUnit.io.output.branchTake)

  pc_plus_four := reg_pc + 4.U
  // decide the next PC
  // the jump address has 4 upper bits taken from old PC, and the address shift 2 digits
  j_target := Cat(reg_pc(31, 28), address, Fill(2, 0.U))
  br_target := extendedImmediateAddr + pc_plus_four
  pc_next := MuxCase(
    pc_plus_four,
    Array(
      (is_Branch) -> br_target,
      (controller.io.output.pcIsJump) -> j_target
    )
  )
  reg_pc := pc_next
  // -----------------execute stage----------------------
  alu.io.input.inputA := valRS
  // if OpBSelect is true, select rb; otherwise select sign extended immediate
  alu.io.input.inputB := Mux(controller.io.output.opBSelect, valRT, extendedImmediateData)
  alu.io.input.aluOp := controller.io.output.aluOp

  val aluOutput = Wire(UInt(32.W))
  aluOutput := alu.io.output.aluOutput

  // -------------------------memory stage---------------------
  io.dmem.address := aluOutput
  io.dmem.writedata := valRT
  io.dmem.memread := controller.io.output.wbSelect
  io.dmem.memwrite := controller.io.output.memWriteEnable
  //TODO: a design choice:
  // if we are piping Op code through all stages, then there isn't any need for Masks
  // However, if we are not piping op code through, then we definitely need a control signal
  io.dmem.maskmode := controller.io.output.memMask
  io.dmem.sext := controller.io.output.memSext
  // a valid interface
  io.dmem.valid := true.B
  //TODO: add a big mux to avoid waiting, though we can avoid this
  // in pipeline?
  val readData = Wire(UInt(32.W))
  readData := io.dmem.readdata
  wb_data := Mux((controller.io.output.wbSelect), readData, aluOutput)
  //---------------write back stage-----------------------

}

/*
 * Object to make it easier to print information about the CPU
 */
object SingleCycleCPUInfo {
  def getModules(): List[String] = {
    List(
      "dmem",
      "imem",
      "controller",
      "regFile",
      "alu"
    )
  }
}
