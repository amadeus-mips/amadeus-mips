package cpu.components

import chisel3._
import chisel3.util._

// there are a lot of bypassing possibilies
// like bypassing for the branch comparison
// bypassing for store in data memory

class BypassUnitIn extends Bundle {
  //----------------------------------------------------------------------------------------
  // ALU bypass
  //----------------------------------------------------------------------------------------

  // id to ex rs and rt
  val idEXRs = Input(UInt(5.W))
  val idEXRt = Input(UInt(5.W))

  // the instruction in the execution stage ( ALU output )
  val idEXRegDst = Input(UInt(5.W))
  val idEXRegWriteEnable = Input(Bool())

  // WB data in memory stage
  // the instruction between execute and memory stage
  // 's dst reg address and write back enable
  val exMemRegDst = Input(UInt(5.W))
  val exMemRegWriteEnable = Input(Bool())

  // WB data in write back stage
  // the instruction between memory and write back stage
  //'s dst reg address and write back enable
  val memWBRegDst = Input(UInt(5.W))
  val memWBRegWriteEnable = Input(Bool())
}

class BypassUnitOut extends Bundle {
  //----------------------------------------------------------------------------------------
  // ALU bypass, jump bypass and branch bypass ( in exe stage )
  //----------------------------------------------------------------------------------------
  // the mux control signal at op A
  val forwardRs = Output(UInt(2.W))
  // the mux control signal for Op B
  val forwardRt = Output(UInt(2.W))

  //----------------------------------------------------------------------------------------
  // memory bypass
  //----------------------------------------------------------------------------------------
  val forwardMemWriteData = Output(Bool())

}

class BypassUnit extends Module {
  val io = IO(new Bundle() {
    val input = new BypassUnitIn
    val output = new BypassUnitOut
  })
  //----------------------------------------------------------------------------------------
  // ALU bypass and Branch Bypass
  //----------------------------------------------------------------------------------------
  //TODO: how bypass play along with hazard detection:
  // on a lw -> add, the value gets bypassed AND NOT UPDATED, because it got stalled

  // this is the case where the register rs is the same as the register to write to in the alu output
  // the forward the alu output to substitute register rs
  // the second case is where rs is the same as dst register passed from memory out
  // note: search in the result directly from exe-mem first
  when((io.input.idEXRs === io.input.exMemRegDst) && io.input.exMemRegDst.orR && io.input.exMemRegWriteEnable) {
    io.output.forwardRs := 1.U
  }.elsewhen(
      (io.input.idEXRs === io.input.memWBRegDst) && io.input.memWBRegDst.orR && io.input.memWBRegWriteEnable
    ) {
      io.output.forwardRs := 2.U
    }
    .otherwise {
      io.output.forwardRs := 0.U
    }

  // note : search in the result directly from exe-mem first
  // you should also exclude register zero in the hazard detection unit
  when((io.input.idEXRt === io.input.exMemRegDst) && io.input.exMemRegDst.orR && io.input.exMemRegWriteEnable) {
    io.output.forwardRt := 1.U
  }.elsewhen(
      (io.input.idEXRt === io.input.memWBRegDst) && io.input.memWBRegDst.orR && io.input.memWBRegWriteEnable
    ) {
      io.output.forwardRt := 2.U
    }
    .otherwise {
      io.output.forwardRt := 0.U
    }

  //----------------------------------------------------------------------------------------
  // Memory bypass
  //----------------------------------------------------------------------------------------
  // lw $t1, 0($t0)
  // sw $t1, 0($t0)
  // or
  // add $t1, $t2, $t3
  // sw $t1, 0($t0)
  when(
    (io.input.exMemRegDst === io.input.memWBRegDst) && io.input.memWBRegDst.orR && io.input.memWBRegWriteEnable
  ) {
    io.output.forwardMemWriteData := true.B
  }.otherwise {
    io.output.forwardMemWriteData := false.B
  }

}
