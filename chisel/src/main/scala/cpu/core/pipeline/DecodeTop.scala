// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.WriteBundle
import cpu.core.bundles.stage5.{IdExeBundle, IfIdBundle}
import cpu.core.execute.components.Branch
import shared.ValidBundle

class DecodeTop extends Module {
  val io = IO(new Bundle {
    val in = Input(new IfIdBundle)
    val inst = Input(UInt(dataLen.W))
    val exeWR = Input(new WriteBundle)
    val memWR = Input(new WriteBundle)
    val rsData = Input(UInt(dataLen.W)) // from register-file
    val rtData = Input(UInt(dataLen.W)) // ^
    val inDelaySlot = Input(Bool()) // get from `IDEX`

    val out = Output(new IdExeBundle)
    val nextInstInDelaySlot = Output(Bool())
    val branch = Output(new ValidBundle) // back to `Fetch`
    val stallReq = Output(Bool()) // to pipeLine control
  })

  val inst = Mux(io.in.instFetchExcept || !io.in.instValid, 0.U, io.inst)

  val hazard = Module(new cpu.core.decode.Hazard)
  val control = Module(new cpu.core.decode.Control)
  val decode = Module(new cpu.core.decode.Decode)
  val branch = Module(new Branch)

  val rs = inst(25, 21)
  val rt = inst(20, 16)
  val rd = inst(15, 11)
  val sa = inst(10, 6)
  val imm16 = inst(15, 0)
  val imm26 = inst(25, 0)

  hazard.io.exeWR <> io.exeWR
  hazard.io.memWR <> io.memWR
  hazard.io.rs := rs
  hazard.io.rt := rt
  hazard.io.rsData := io.rsData
  hazard.io.rtData := io.rtData
  hazard.io.op1Type := control.io.out.op1Type
  hazard.io.op2Type := control.io.out.op2Type

  /** 根据指令解码获取控制信号 */
  control.io.inst := inst

  decode.io.signal <> control.io.out
  decode.io.inst := inst
  decode.io.instFetchExc := io.in.instFetchExcept
  decode.io.rsData := hazard.io.outRsData
  decode.io.rtData := hazard.io.outRtData

  branch.io.op1 := decode.io.op1
  branch.io.op2 := decode.io.op2
  branch.io.operation := control.io.out.operation
  branch.io.imm26 := imm26
  branch.io.pc := io.in.pc

  io.out.instType := control.io.out.instType
  io.out.operation := control.io.out.operation

  io.out.op1 := decode.io.op1
  io.out.op2 := decode.io.op2
  io.out.write <> decode.io.write
  io.out.cp0 <> decode.io.cp0
  io.out.except := decode.io.except

  io.out.pc := io.in.pc
  io.out.imm26 := imm26
  io.out.inDelaySlot := io.inDelaySlot
  io.nextInstInDelaySlot := decode.io.nextInstInDelaySlot

  io.branch <> branch.io.branch
  io.stallReq := hazard.io.stallReq

}
