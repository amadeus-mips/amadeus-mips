// See README.md for license details.

package cpu.core.pipeline

import chisel3._
import chisel3.util.ValidIO
import common.ValidBundle
import cpu.core.Constants._
import cpu.core.bundles.WriteBundle
import cpu.core.bundles.stage.{IDEXEBundle, IFIDBundle}
import cpu.core.decode._
import cpu.core.execute.components.Branch

class DecodeTop extends Module {
  val io = IO(new Bundle {
    val in = Input(new IFIDBundle)
    val inst = Input(UInt(dataLen.W))
    val exeOp = Input(UInt(opLen.W))
    val exeWR = Input(new WriteBundle)
    val memWR = Input(new WriteBundle)
    val rsData = Input(UInt(dataLen.W)) // from register-file
    val rtData = Input(UInt(dataLen.W)) // ^
    val inDelaySlot = Input(Bool())     // get from `IDEX`

    val out = Output(new IDEXEBundle)
    val nextInstInDelaySlot = Output(Bool())
    val branch = Output(new ValidBundle) // back to `Fetch`
    val stallReq = Output(Bool())       // to pipeLine control
  })

  val inst = Mux(io.in.instFetchExcept || io.in.pc === 0.U, 0.U, io.inst)

  val forward = Module(new Forward)
  val loadUse = Module(new LoadUse)
  val control = Module(new Control)
  val decode = Module(new Decode)
  val branch = Module(new Branch)

  val rs = inst(25, 21)
  val rt = inst(20, 16)
  val rd = inst(15, 11)
  val sa = inst(10, 6)
  val imm16 = inst(15, 0)
  val imm26 = inst(25, 0)

  forward.io.exeWR <> io.exeWR
  forward.io.memWR <> io.memWR
  forward.io.rs := rs
  forward.io.rt := rt
  forward.io.rsData := io.rsData
  forward.io.rtData := io.rtData

  loadUse.io.exeOp := io.exeOp
  loadUse.io.exeWR <> io.exeWR
  loadUse.io.op1Type := control.io.out.op1Type
  loadUse.io.op2Type := control.io.out.op2Type
  loadUse.io.rs := rs
  loadUse.io.rt := rt

  /** 根据指令解码获取控制信号 */
  control.io.inst := inst

  decode.io.signal <> control.io.out
  decode.io.inst := inst
  decode.io.instFetchExc := io.in.instFetchExcept
  decode.io.rsData := forward.io.outRsData
  decode.io.rtData := forward.io.outRtData

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
  io.out.cp0Control <> decode.io.cp0Control
  io.out.except := decode.io.except

  io.out.pc := io.in.pc
  io.out.imm26 := imm26
  io.out.inDelaySlot := io.inDelaySlot
  io.nextInstInDelaySlot := decode.io.nextInstInDelaySlot

  io.branch <> branch.io.branch
  io.stallReq := loadUse.io.stallReq

}
