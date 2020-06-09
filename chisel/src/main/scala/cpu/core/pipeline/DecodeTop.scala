// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.Cat
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.WriteBundle
import cpu.core.bundles.stages.{IdExeBundle, IfIdBundle}
import cpu.core.decode.AlwaysTakenPredictor
import shared.{Util, ValidBundle}

class DecodeTop(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val in   = Input(new IfIdBundle)
    val inst = Input(UInt(dataLen.W))

    val exeWR = Input(new WriteBundle)
    val memWR = Input(new WriteBundle)
    val wbWR  = Input(new WriteBundle)

    val rsData = Input(UInt(dataLen.W)) // from register-file
    val rtData = Input(UInt(dataLen.W)) // ^

    val predictorUpdate = Input(Bool())
    val predictorTaken  = Input(Bool())

    val out      = Output(new IdExeBundle)
    val stallReq = Output(Bool()) // to pipeLine control

    val predict = Output(new ValidBundle)

    val nextInstInDelaySlot = Output(Bool()) // to Fetch
  })

  val inst = Mux(io.in.except.asUInt().andR() || !io.in.instValid, 0.U, io.inst)

  val hazard    = Module(new cpu.core.decode.Hazard)
  val control   = Module(new cpu.core.decode.Control)
  val decode    = Module(new cpu.core.decode.Decode)
  val predictor = Module(new AlwaysTakenPredictor)

  val rs    = inst(25, 21)
  val rt    = inst(20, 16)
  val rd    = inst(15, 11)
  val sa    = inst(10, 6)
  val imm16 = inst(15, 0)
  val imm26 = inst(25, 0)

  hazard.io.wrs(0) <> io.exeWR
  hazard.io.wrs(1) <> io.memWR
  hazard.io.wrs(2) <> io.wbWR

  hazard.io.ops(0).addr   := rs
  hazard.io.ops(0).inData := io.rsData
  hazard.io.ops(0).typ    := control.io.out.op1Type

  hazard.io.ops(1).addr   := rt
  hazard.io.ops(1).inData := io.rtData
  hazard.io.ops(1).typ    := control.io.out.op2Type

  /** 根据指令解码获取控制信号 */
  control.io.inst := inst

  decode.io.inst.rt    := rt
  decode.io.inst.rd    := rd
  decode.io.inst.sa    := sa
  decode.io.inst.imm16 := imm16
  decode.io.inst.sel   := inst(2, 0)

  decode.io.signal   := control.io.out
  decode.io.inExcept := io.in.except
  decode.io.rsData   := hazard.io.ops(0).outData
  decode.io.rtData   := hazard.io.ops(1).outData

  predictor.io.pc     := io.in.pc
  predictor.io.update := io.predictorUpdate
  predictor.io.taken  := io.predictorTaken

  val isJR = Util.listHasElement(Seq(BR_JR, BR_JALR), control.io.out.operation)
  val isJ  = Util.listHasElement(Seq(BR_J, BR_JAL), control.io.out.operation)

  io.out.instType  := control.io.out.instType
  io.out.operation := control.io.out.operation

  io.out.op1    := decode.io.op1
  io.out.op2    := decode.io.op2
  io.out.write  := decode.io.write
  io.out.cp0    := decode.io.cp0
  io.out.except := decode.io.except

  io.out.pc          := io.in.pc
  io.out.imm26       := imm26
  io.out.inDelaySlot := io.in.inDelaySlot
  io.out.brPredicted := predictor.io.prediction && !isJR && control.io.out.instType === INST_BR

  val pcPlus4 = io.in.pc + 4.U
  val BTarget = pcPlus4 + Cat(Util.signedExtend(imm16, to = 30), 0.U(2.W))
  val JTarget = Cat(pcPlus4(31, 28), imm26, 0.U(2.W))
  io.predict.valid := predictor.io.prediction && !isJR && control.io.out.instType === INST_BR
  io.predict.bits  := Mux(isJ, JTarget, BTarget)

  io.stallReq := hazard.io.stallReq

  io.nextInstInDelaySlot := decode.io.nextInstInDelaySlot

}
