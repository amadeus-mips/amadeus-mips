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
  //TODO: pass along
  // if to id status register instruction
  val idEXRs = Input(UInt(5.W))
  val idEXRt = Input(UInt(5.W))

  // the instruction between execute and memory stage
  // 's dst reg address and write back enable
  val exMemRegDst = Input(UInt(5.W))
  val exMemRegWriteEnable = Input(Bool())

  // the instruction between memory and write back stage
  //'s dst reg address and write back enable
  val memWBRegDst = Input(UInt(5.W))
  val memWBRegWriteEnable = Input(Bool())
  //----------------------------------------------------------------------------------------
  // memory bypass
  //----------------------------------------------------------------------------------------
}

class BypassUnitOut extends Bundle {
  //----------------------------------------------------------------------------------------
  // ALU bypass
  //----------------------------------------------------------------------------------------
  // the mux control signal at op A
  val forwardALUOpA = Output(UInt(2.W))
  // the mux control signal for Op B
  val forwardALUOpB = Output(UInt(2.W))
  //----------------------------------------------------------------------------------------
  // memory bypass
  //----------------------------------------------------------------------------------------
  val forwardMemWriteData = Output(Bool())
  //----------------------------------------------------------------------------------------
  // branching bypass
  //----------------------------------------------------------------------------------------
}

class BypassUnit extends Module {
  val io = IO(new Bundle() {
    val input = new BypassUnitIn
    val output = new BypassUnitOut
  })
  //----------------------------------------------------------------------------------------
  // ALU bypass and Branch Bypass
  //----------------------------------------------------------------------------------------
  // this is the case where the register rs is the same as the register to write to in the alu output
  // the forward the alu output to substitute register rs
  // the second case is where rs is the same as dst register passed from memory out
  // note: search in the result directly from exe-mem first
  when((io.input.idEXRs === io.input.exMemRegDst) && (io.input.exMemRegDst =/= 0.U) && io.input.exMemRegWriteEnable) {
    io.output.forwardALUOpA := 1.U
  }.elsewhen(
      (io.input.idEXRs === io.input.memWBRegDst) && (io.input.memWBRegDst =/= 0.U) && io.input.memWBRegWriteEnable
    ) {
      io.output.forwardALUOpA := 2.U
    }
    .otherwise {
      io.output.forwardALUOpA := 0.U
    }

  // note : search in the result directly from exe-mem first
  // you should also exclude register zero in the hazard detection unit
  when((io.input.idEXRt === io.input.exMemRegDst) && (io.input.exMemRegDst =/= 0.U) && io.input.exMemRegWriteEnable) {
    io.output.forwardALUOpB := 1.U
  }.elsewhen(
      (io.input.idEXRt === io.input.memWBRegDst) && (io.input.memWBRegDst =/= 0.U) && io.input.memWBRegWriteEnable
    ) {
      io.output.forwardALUOpB := 2.U
    }
    .otherwise {
      io.output.forwardALUOpB := 0.U
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
    (io.input.exMemRegDst === io.input.memWBRegDst) && (io.input.memWBRegDst =/= 0.U) && io.input.memWBRegWriteEnable
  ) {
    io.output.forwardMemWriteData := true.B
  }.otherwise {
    io.output.forwardMemWriteData := false.B
  }

}
