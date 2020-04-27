// See README.md for license details.

package cpu.core.decode

import chisel3._
import cpu.core.Constants._
import cpu.core.bundles.WriteBundle

/**
 * Handle hazard
 */
class Hazard extends Module {
  val io = IO(new Bundle {
    val exeWR = Input(new WriteBundle)
    val memWR = Input(new WriteBundle)

    val rs = Input(UInt(regAddrLen.W))  // inst[25:21]
    val rt = Input(UInt(regAddrLen.W))  // inst[20:16]
    val rsData = Input(UInt(dataLen.W)) // from register-file
    val rtData = Input(UInt(dataLen.W)) // from register-file
    val op1Type = Input(UInt(1.W))      // from Control
    val op2Type = Input(UInt(1.W))      // from Control

    val outRsData = Output(UInt(dataLen.W))
    val outRtData = Output(UInt(dataLen.W))
    val stallReq = Output(Bool())
  })

  val rsStall = Wire(Bool())
  when(io.rs === 0.U) {
    io.outRsData := 0.U
    rsStall := false.B
  }.elsewhen(io.exeWR.enable && io.exeWR.address === io.rs) {
    io.outRsData := io.exeWR.data
    rsStall := !io.exeWR.valid && io.op1Type === OPn_RF
  }.elsewhen(io.memWR.enable && io.memWR.address === io.rs) {
    io.outRsData := io.memWR.data
    rsStall := !io.memWR.valid && io.op1Type === OPn_RF
  }.otherwise{
    io.outRsData := io.rsData
    rsStall := false.B
  }

  val rtStall = Wire(Bool())
  when(io.rt === 0.U) {
    io.outRtData := 0.U
    rtStall := false.B
  }.elsewhen(io.exeWR.enable && io.exeWR.address === io.rt) {
    io.outRtData := io.exeWR.data
    rtStall := !io.exeWR.valid && io.op2Type === OPn_RF
  }.elsewhen(io.memWR.enable && io.memWR.address === io.rt) {
    io.outRtData := io.memWR.data
    rtStall := !io.memWR.valid && io.op2Type === OPn_RF
  }.otherwise{
    io.outRtData := io.rtData
    rtStall := false.B
  }

  io.stallReq := rsStall || rtStall

}
