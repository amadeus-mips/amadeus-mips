// See README.md for license details.

package cpu.core.decode

import chisel3._
import chisel3.util.MuxCase
import cpu.core.Constants._
import cpu.core.bundles.WriteBundle


class LoadUse extends Module {
  val io = IO(new Bundle {
    val exeOp = Input(UInt(opLen.W))
    val exeWR = Input(new WriteBundle)
    val op1Type = Input(UInt(2.W))
    val op2Type = Input(UInt(2.W))
    val rs = Input(UInt(regAddrLen.W))  // inst[25:21]
    val rt = Input(UInt(regAddrLen.W))  // inst[20:16]
    val stallReq = Output(Bool())
  })

  val preInstIsLoad = opIsLoad(io.exeOp)
  io.stallReq := preInstIsLoad &&
    Map(io.op1Type -> io.rs, io.op2Type -> io.rt).foldLeft(true.B)((r, e) => {
      r || (e._1 === OP1_RS && e._2 === io.exeWR.control.address)
    })
}

/**
 * 处理数据前递和Load-use数据冒险
 */
class Forward extends Module {
  val io = IO(new Bundle {
    val exeWR = Input(new WriteBundle)
    val memWR = Input(new WriteBundle)
    val rs = Input(UInt(regAddrLen.W))  // inst[25:21]
    val rt = Input(UInt(regAddrLen.W))  // inst[20:16]
    val rsData = Input(UInt(dataLen.W)) // from register-file
    val rtData = Input(UInt(dataLen.W)) // from register-file

    val outRsData = Output(UInt(dataLen.W))
    val outRtData = Output(UInt(dataLen.W))
  })
  io.outRsData := MuxCase(io.rsData,
    Array(
      (io.rs === 0.U) -> 0.U,
      (io.exeWR.control.enable && io.exeWR.control.address === io.rs) -> io.exeWR.data,
      (io.memWR.control.enable && io.memWR.control.address === io.rs) -> io.memWR.data
    )
  )
  io.outRtData := MuxCase(io.rtData,
    Array(
      (io.rt === 0.U) -> 0.U,
      (io.exeWR.control.enable && io.exeWR.control.address === io.rt) -> io.exeWR.data,
      (io.memWR.control.enable && io.memWR.control.address === io.rt) -> io.memWR.data
    )
  )


}
